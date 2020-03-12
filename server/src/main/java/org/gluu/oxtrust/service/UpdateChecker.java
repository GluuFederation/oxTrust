package org.gluu.oxtrust.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxtrust.service.cdi.event.StatusCheckerTimerEvent;
import org.gluu.service.cdi.async.Asynchronous;
import org.gluu.service.cdi.event.Scheduled;
import org.gluu.service.timer.event.TimerEvent;
import org.gluu.service.timer.schedule.TimerSchedule;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
@Named
public class UpdateChecker {
	private final static int DEFAULT_INTERVAL = 60 * 60 * 8; // 60 minute
	private AtomicBoolean isActive;
	@Inject
	private Event<TimerEvent> timerEvent;
	@Inject
	private Logger log;
	private boolean hasUpdate = false;
	private boolean shouldUpdate = false;
	@Inject
	private AppConfiguration appConfiguration;

	private String updateMessage = "";

	public void initTimer() {
		this.isActive = new AtomicBoolean(false);
		final int delay = 1 * 60;
		final int interval = DEFAULT_INTERVAL;
		timerEvent.fire(new TimerEvent(new TimerSchedule(delay, interval), new StatusCheckerTimerEvent(),
				Scheduled.Literal.INSTANCE));
	}

	@Asynchronous
	public void process(@Observes @Scheduled StatusCheckerTimerEvent statusCheckerTimerEvent) {
		if (this.isActive.get()) {
			return;
		}
		if (!this.isActive.compareAndSet(false, true)) {
			return;
		}
		try {
			if (appConfiguration.isEnableUpdateNotification()) {
				processInt();
			}
		} finally {
			this.isActive.set(false);
		}
	}

	private void processInt() {
		log.debug("================================Running update checker= timer==================================");
		GluuVersionAvailability versionAvailability = new GluuVersionAvailability();
		Map<String, GluuComponent> runningComponents = new HashMap<>();
		Map<String, GluuComponent> availableComponents = new HashMap<>();
		hasUpdate = false;
		shouldUpdate = false;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			versionAvailability = objectMapper.readValue(getVersionAvailability(), GluuVersionAvailability.class);
			runningComponents = convertToMap(
					Arrays.asList(objectMapper.readValue(getRunningComponents(), GluuComponent[].class)));
			availableComponents = convertToMap(
					Arrays.asList(objectMapper.readValue(getAvailableComponents(), GluuComponent[].class)));
		} catch (JsonMappingException e) {
			log.error("JsonMappingException", e);
		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException ", e);
		} catch (Exception e) {
			log.error("Exception ", e);
		}
		Set<String> keys = runningComponents.keySet();
		for (String key : keys) {
			GluuComponent gluuComponent = availableComponents.get(key);
			if (gluuComponent != null
					&& !runningComponents.get(key).getVersion().equalsIgnoreCase(gluuComponent.getVersion())) {
				shouldUpdate = true;
			}
		}
		if (versionAvailability.isNewVersionAvailable()) {
			this.hasUpdate = true;
			this.updateMessage = "Gluu Identity Server version " + versionAvailability.getVersion().split("\\(")[0]
					+ " is available!";
		}
		if (shouldUpdate) {
			this.updateMessage += " Looks like some running service aren't up to date.";
		}
	}

	private Map<String, GluuComponent> convertToMap(List<GluuComponent> list) {
		return list.stream().collect(Collectors.toMap(GluuComponent::getKey, item -> item));
	}

	private String getVersionAvailability() {
		String command = "python /opt/gluu/bin/check_new_version.py --json";
		Process p;
		StringBuilder builder = new StringBuilder();
		try {
			p = Runtime.getRuntime().exec(command);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null) {
				builder.append(line + "\n");
			}
		} catch (IOException e) {
			log.error("Error check_new_version python script ", e);
		}
		return builder.toString();
	}

	private String getRunningComponents() {
		String command = "python /opt/gluu/bin/show_version.py --json";
		Process p;
		StringBuilder builder = new StringBuilder();
		try {
			p = Runtime.getRuntime().exec(command);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null) {
				builder.append(line + "\n");
			}
		} catch (IOException e) {
			log.error("Error running python command ", e);
		}
		return builder.toString();
	}

	private String getAvailableComponents() {
		String command = "python /opt/gluu/bin/show_version.py -target=/opt/dist/gluu/ --json";
		Process p;
		StringBuilder builder = new StringBuilder();
		try {
			p = Runtime.getRuntime().exec(command);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null) {
				builder.append(line + "\n");
			}
		} catch (IOException e) {
			log.error("Error running python command ", e);
		}
		return builder.toString();
	}

	private String getProductVersion() {
		String repoUrl = "https://repo.gluu.org/infoupdate/latest_version.json";
		TrustManager[] trustAllCerts = getTrust();
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
		}
		StringBuilder sb = new StringBuilder();
		try {
			URL url = new URL(repoUrl);
			InputStream in = url.openConnection().getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			in.close();
		} catch (UnknownHostException e) {
			log.error("No internet connexion available to check updated version is available");
		} catch (Exception e) {
			log.error("Failed to get products version json ", e);
		}
		return sb.toString();
	}

	private TrustManager[] getTrust() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };
		return trustAllCerts;
	}

	public String getUpdateMessage() {
		return updateMessage;
	}

	public void setUpdateMessage(String updateMessage) {
		this.updateMessage = updateMessage;
	}

	public boolean isHasUpdate() {
		return hasUpdate;
	}

	public void setHasUpdate(boolean hasUpdate) {
		this.hasUpdate = hasUpdate;
	}

	public boolean isShouldUpdate() {
		return shouldUpdate;
	}

	public void setShouldUpdate(boolean shouldUpdate) {
		this.shouldUpdate = shouldUpdate;
	}

}