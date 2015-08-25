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
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.log.Log;
import org.xdi.model.ApplicationType;
import org.xdi.model.metric.MetricType;
import org.xdi.model.metric.ldap.MetricEntry;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Store and retrieve metric
 *
 * @author Rahat Ali Date: 07/30/2015
 */
@Scope(ScopeType.APPLICATION)
@Name(MetricService.METRIC_SERVICE_COMPONENT_NAME)
@Startup
public class MetricService extends org.xdi.service.metric.MetricService {

	public static final String METRIC_SERVICE_COMPONENT_NAME = "metricService";

	private static final long serialVersionUID = 7875838160379126796L;

	@Logger
	private Log log;

	@In
	private ApplianceService applianceService;
	
	@In
	private OrganizationService organizationService;

	@In
	private OxTrustConfiguration oxTrustConfiguration;

	Map<MetricType, List<MetricEntry>> entries;
	String authenticationChartJson;
	AuthenticationChartDto authenticationChartDto = new AuthenticationChartDto();
	ObjectMapper mapper = new ObjectMapper();
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	@Create
	public void create() {
//		init(3000);
		List<MetricType> metricTypes = new ArrayList<MetricType>();
		metricTypes.add(MetricType.OXAUTH_USER_AUTHENTICATION_FAILURES);
		metricTypes.add(MetricType.OXAUTH_USER_AUTHENTICATION_SUCCESS);
		metricTypes.add(MetricType.OXAUTH_USER_AUTHENTICATION_RATE);

		Date endDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.add( Calendar.DATE ,  -7 );

		Date startDate = calendar.getTime();
		try {
			entries = findMetricEntry(ApplicationType.OX_AUTH,
					oxTrustConfiguration.getApplicationConfiguration()
							.getApplianceInum(), metricTypes, startDate,
					endDate, null);
			System.out.println(entries);
			
			String []labels = new String[7];
			Map<String, Integer> successStats = calculateStatistics(entries.get(MetricType.OXAUTH_USER_AUTHENTICATION_SUCCESS));
			labels = successStats.keySet().toArray(labels);
			Integer []values = new Integer[7];
			values = successStats.values().toArray(values);
			authenticationChartDto.setLabels(labels);
			authenticationChartDto.setSuccess(values);
			Map<String, Integer> failureStats = calculateStatistics(entries.get(MetricType.OXAUTH_USER_AUTHENTICATION_FAILURES));
			values = failureStats.values().toArray(values);
			authenticationChartDto.setFailure(values);			
			authenticationChartJson = mapper.writeValueAsString(authenticationChartDto);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	private Map<String, Integer> calculateStatistics(
			List<MetricEntry> success) {
		Map<String, Integer> stats = new TreeMap<String, Integer>();
		Calendar calendar = Calendar.getInstance();
		for(int i=0;i<7;i++){
			String dateString =df.format(calendar.getTime());
			stats.put(dateString,0);
			calendar.add(Calendar.DATE ,  -1);	
			
		}
		for (MetricEntry metricEntry : success) {
			Date date = metricEntry.getCreationDate();
			String dateString =df.format(date);
			Integer count = stats.get(dateString);
			if(count!=null)
				stats.put(dateString,(count+1));
			else
				stats.put(dateString,1);
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

	public Map<MetricType, List<MetricEntry>> getEntries() {
		return entries;
	}

	public void setEntries(Map<MetricType, List<MetricEntry>> entries) {
		this.entries = entries;
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

	public String getAuthenticationChartJson() {
		return authenticationChartJson;
	}

	public void setAuthenticationChartJson(String authenticationChartJson) {
		this.authenticationChartJson = authenticationChartJson;
	}

	public AuthenticationChartDto getAuthenticationChartDto() {
		return authenticationChartDto;
	}

	public void setAuthenticationChartDto(
			AuthenticationChartDto authenticationChartDto) {
		this.authenticationChartDto = authenticationChartDto;
	}
}
