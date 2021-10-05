/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.model.ApplicationType;
import org.gluu.model.metric.MetricType;
import org.gluu.model.metric.counter.CounterMetricEntry;
import org.gluu.model.metric.ldap.MetricEntry;
import org.gluu.oxtrust.model.AuthenticationChartDto;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.service.CacheService;
import org.gluu.service.metric.inject.ReportMetric;
import org.gluu.service.net.NetworkService;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Store and retrieve metric
 *
 * @author Yuriy Movchan Date: 06/28/2015
 * @author Rahat Ali Date: 07/30/2015
 */
@Stateless
@Named(MetricService.METRIC_SERVICE_COMPONENT_NAME)
public class MetricService extends org.gluu.service.metric.MetricService {

    private static final int YEARLY = 365;

    private static final long serialVersionUID = 7875838160379126796L;

    public static final String METRIC_SERVICE_COMPONENT_NAME = "metricService";
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    @Inject
    private Logger log;

    @Inject
    private Instance<MetricService> instance;

    @Inject
    private CacheService cacheService;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private OrganizationService organizationService;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private NetworkService networkService;

    @Inject
    @Named(ApplicationFactory.PERSISTENCE_METRIC_ENTRY_MANAGER_NAME)
    @ReportMetric
    private PersistenceEntryManager ldapEntryManager;

    public void initTimer() {
        initTimer(this.appConfiguration.getMetricReporterInterval(),
                this.appConfiguration.getMetricReporterKeepDataDays());
    }

    @Override
    public String baseDn() {
        return String.format("ou=statistic,o=metric");
    }

    @Override
    public org.gluu.service.metric.MetricService getMetricServiceInstance() {
        return instance.get();
    }

    public AuthenticationChartDto genereateAuthenticationChartDto(int countDays) {
        String key = OxTrustConstants.CACHE_METRICS_KEY + "#home";
        AuthenticationChartDto authenticationChartDto = (AuthenticationChartDto) cacheService
                .get(OxTrustConstants.CACHE_METRICS_NAME, key);
        if (authenticationChartDto != null) {
            return authenticationChartDto;
        }
        Map<MetricType, List<? extends MetricEntry>> entries = findAuthenticationMetrics(ApplicationType.OX_AUTH,
                -countDays);
        Map<MetricType, List<? extends MetricEntry>> yearlyEntris = findAuthenticationMetrics(ApplicationType.OX_AUTH,
                -YEARLY);
        Map<String, Long> yearlySuccessStats = calculateCounterStatistics(YEARLY,
                (List<CounterMetricEntry>) yearlyEntris.get(MetricType.OXAUTH_USER_AUTHENTICATION_SUCCESS));
        Long yearlySuccessfullRequest = yearlySuccessStats.values().stream().mapToLong(Long::valueOf).sum();
        Map<String, Long> yearlyFailureStats = calculateCounterStatistics(YEARLY,
                (List<CounterMetricEntry>) yearlyEntris.get(MetricType.OXAUTH_USER_AUTHENTICATION_FAILURES));
        Long yearlyFailsRequest = yearlyFailureStats.values().stream().mapToLong(Long::valueOf).sum();
        String[] labels = new String[countDays+1];
        Long[] values = new Long[countDays+1];
        Map<String, Long> successStats = calculateCounterStatistics(countDays,
                (List<CounterMetricEntry>) entries.get(MetricType.OXAUTH_USER_AUTHENTICATION_SUCCESS));
        labels = successStats.keySet().toArray(labels);
        values = successStats.values().toArray(values);
        authenticationChartDto = new AuthenticationChartDto();
        authenticationChartDto.setLabels(labels);
        authenticationChartDto.setSuccess(values);
        Map<String, Long> failureStats = calculateCounterStatistics(countDays,
                (List<CounterMetricEntry>) entries.get(MetricType.OXAUTH_USER_AUTHENTICATION_FAILURES));
        values = new Long[countDays];
        authenticationChartDto.setFailure(failureStats.values().toArray(values));
        authenticationChartDto.setYearlyRequest(yearlySuccessfullRequest + yearlyFailsRequest);
        cacheService.put(key, authenticationChartDto);
        return authenticationChartDto;
    }

    private Map<MetricType, List<? extends MetricEntry>> findAuthenticationMetrics(ApplicationType applicationType,
            int countDays) {
        List<MetricType> metricTypes = new ArrayList<MetricType>();
        metricTypes.add(MetricType.OXAUTH_USER_AUTHENTICATION_FAILURES);
        metricTypes.add(MetricType.OXAUTH_USER_AUTHENTICATION_SUCCESS);
        Date endDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, countDays);
        Date startDate = calendar.getTime();
        return findMetricEntry(applicationType, metricTypes, startDate, endDate);
    }

    private Map<String, Long> calculateCounterStatistics(int countDays, List<CounterMetricEntry> metrics) {
        // Prepare map with all dates
        Map<String, Long> stats = new TreeMap<String, Long>();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i <= countDays; i++) {
            stats.put(df.format(calendar.getTime()), 0L);
            calendar.add(Calendar.DATE, -1);
        }
        if ((metrics == null) || (metrics.size() == 0)) {
            return stats;
        }

        // Detect servers restart and readjust counts
        // Server restart condition: previous entry
        // CounterMetricEntry.CounterMetricEntry.count > current entry
        // CounterMetricEntry.CounterMetricEntry.count
        CounterMetricEntry prevMetric = null;
        long prevDayCount = 0L;
        long adjust = 0;
        for (CounterMetricEntry metric : metrics) {
            Date date = metric.getCreationDate();
            calendar.setTime(date);

            // Detect server restarts
            if ((prevMetric != null)
                    && (prevMetric.getMetricData().getCount() > metric.getMetricData().getCount() + adjust)) {
                // Last count before server restart
                long count = prevMetric.getMetricData().getCount();

                // Change adjust value
                adjust = count;
            }

            long count = metric.getMetricData().getCount();
            metric.getMetricData().setCount(count + adjust);

            prevMetric = metric;
        }

        // Iterate through ordered by MetricEntry.startDate list and just make value
        // snapshot at the end of the day
        int prevDay = -1;
        prevMetric = null;
        prevDayCount = 0L;
        for (CounterMetricEntry metric : metrics) {
            Date date = metric.getCreationDate();
            calendar.setTime(date);

            int currDay = calendar.get(Calendar.DAY_OF_MONTH);
            if ((prevMetric != null) && (prevDay != currDay)) {
                long count = prevMetric.getMetricData().getCount();
                String dateString = df.format(prevMetric.getCreationDate());
                stats.put(dateString, count - prevDayCount);

                // Show only difference, not total
                prevDayCount = count;
            }

            prevMetric = metric;
            prevDay = currDay;
        }

        // Add last day statistic
        long count = prevMetric.getMetricData().getCount();
        String dateString = df.format(prevMetric.getCreationDate());
        stats.put(dateString, count - prevDayCount);
        return stats;
    }

    @Override
    public boolean isMetricReporterEnabled() {
        if (this.appConfiguration.getMetricReporterEnabled() == null) {
            return false;
        }

        return this.appConfiguration.getMetricReporterEnabled();
    }

    @Override
    public ApplicationType getApplicationType() {
        return ApplicationType.OX_TRUST;
    }

    private void dump(List<CounterMetricEntry> metrics) {
        for (CounterMetricEntry metric : metrics) {
            Date date = metric.getCreationDate();
            long count = metric.getMetricData().getCount();
            System.out.println(date + " : " + count);
        }
    }

    @Override
    public PersistenceEntryManager getEntryManager() {
        return ldapEntryManager;
    }

    @Override
    public String getNodeIndetifier() {
        return networkService.getMacAdress();
    }

}
