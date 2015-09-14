/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.AuthenticationChartDto;
import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.model.ApplicationType;
import org.xdi.model.metric.MetricType;
import org.xdi.model.metric.ldap.MetricEntry;
import org.xdi.service.CacheService;

/**
 * Store and retrieve metric
 *
 * @author Rahat Ali Date: 07/30/2015
 * @author Yuriy Movchan Date: 08/28/2015
 */
@Scope(ScopeType.STATELESS)
@Name(MetricService.METRIC_SERVICE_COMPONENT_NAME)
@AutoCreate
public class MetricService extends org.xdi.service.metric.MetricService {

	private static final long serialVersionUID = 7875838160379126796L;
	
	public static final String METRIC_SERVICE_COMPONENT_NAME = "metricService";
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	@Logger
	private Log log;
	
	@In
	private CacheService cacheService;
	
	@In
	private ApplianceService applianceService;
	
	@In
	private OrganizationService organizationService;

	@In
	private OxTrustConfiguration oxTrustConfiguration;

	public AuthenticationChartDto genereateAuthenticationChartDto(int countDays) {
		AuthenticationChartDto authenticationChartDto = new AuthenticationChartDto();
		String key = OxTrustConstants.CACHE_METRICS_KEY;
		Map<MetricType, List<MetricEntry>> entries = (Map<MetricType, List<MetricEntry>>) cacheService.get(OxTrustConstants.CACHE_METRICS_NAME, key);
		if (entries== null) {
			entries = findAuthenticationMetrics(-countDays);
			cacheService.put(OxTrustConstants.CACHE_METRICS_NAME, key, entries);
		}
			
		String []labels = new String[countDays];
		Map<String, Integer> successStats = calculateStatistics(countDays, entries.get(MetricType.OXAUTH_USER_AUTHENTICATION_SUCCESS));
		labels = successStats.keySet().toArray(labels);

		Integer []values = new Integer[countDays];
		values = successStats.values().toArray(values);

		authenticationChartDto.setLabels(labels);
		authenticationChartDto.setSuccess(values);

		Map<String, Integer> failureStats = calculateStatistics(countDays, entries.get(MetricType.OXAUTH_USER_AUTHENTICATION_FAILURES));
		values = new Integer[countDays];
		values = failureStats.values().toArray(values);
		authenticationChartDto.setFailure(values);			

		return authenticationChartDto;
	}

	private Map<MetricType, List<MetricEntry>> findAuthenticationMetrics(int countDays) {
		List<MetricType> metricTypes = new ArrayList<MetricType>();
		metricTypes.add(MetricType.OXAUTH_USER_AUTHENTICATION_FAILURES);
		metricTypes.add(MetricType.OXAUTH_USER_AUTHENTICATION_SUCCESS);
		metricTypes.add(MetricType.OXAUTH_USER_AUTHENTICATION_RATE);

		Date endDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, countDays);

		Date startDate = calendar.getTime();

		Map<MetricType, List<MetricEntry>> entries = findMetricEntry(ApplicationType.OX_AUTH, oxTrustConfiguration.getApplicationConfiguration()
				.getApplianceInum(), metricTypes, startDate, endDate, null);

		return entries;
	}

	private Map<String, Integer> calculateStatistics(int countDays, List<MetricEntry> success) {
		Map<String, Integer> stats = new TreeMap<String, Integer>();
		Calendar calendar = Calendar.getInstance();
		for (int i = 0; i < countDays; i++) {
			String dateString = df.format(calendar.getTime());
			stats.put(dateString, 0);
			calendar.add(Calendar.DATE, -1);

		}
		if (success != null)
			for (MetricEntry metricEntry : success) {
				Date date = metricEntry.getCreationDate();
				String dateString = df.format(date);
				Integer count = stats.get(dateString);
				if (count != null)
					stats.put(dateString, (count + 1));
				else
					stats.put(dateString, 1);
			}
		return stats;
	}
	
	@Override
	public String applianceInum() {
		return applianceService.getApplianceInum();
	}

	@Override
	public String getComponentName() {
		return METRIC_SERVICE_COMPONENT_NAME;
	}

	public static MetricService instance() {
		return (MetricService) Component.getInstance(MetricService.class);
	}

	@Override
	public String baseDn() {
		String orgDn = OrganizationService.instance().getDnForOrganization();
		String baseDn = String.format("ou=metric,%s", orgDn);
		return baseDn;
	}

}
