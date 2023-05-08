package org.gluu.oxtrust.service;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxtrust.service.cdi.event.TranscodingRulesUpdateEvent;
import org.gluu.service.cdi.async.Asynchronous;
import org.gluu.service.cdi.event.Scheduled;

import org.gluu.service.timer.event.TimerEvent;
import org.gluu.service.timer.schedule.TimerSchedule;

import org.slf4j.Logger;


@ApplicationScoped
public class TranscodingRulesUpdater implements Serializable {
    
    private static final int DEFAULT_UPDATE_DELAY = 60 ; // Start after 5 minutes
    private static final int DEFAULT_UPDATE_INTERVAL = 60 * 30 ; // 30 minutes refresh interval 


    @Inject
    private Logger log;

    @Inject
    private AppConfiguration appConfiguration;

    @Inject
    private Event<TimerEvent> timerEvent;

    @Inject
    private Shibboleth3ConfService shibbolethConfService;

    @Inject
    private ShibbolethReloadService shibbolethReloadService;
    
    private AtomicBoolean isActive;
    
    @PostConstruct
    public void init() {

        this.isActive = new AtomicBoolean();
        
    }

    public void initTimer() {
        log.debug("Initializing Shibboleth Transcoding Rules Updater");
        final int delay = DEFAULT_UPDATE_DELAY;
        final int interval = DEFAULT_UPDATE_INTERVAL;
        TimerSchedule timerSchedule = new TimerSchedule(delay,interval);
        timerEvent.fire(new TimerEvent(timerSchedule,new TranscodingRulesUpdateEvent(),Scheduled.Literal.INSTANCE));
    }

    @Asynchronous
    public void processTranscodingRulesUpdateEvent(@Observes @Scheduled TranscodingRulesUpdateEvent event) {

        if(this.isActive.get()) {
            return;
        }

        if(!this.isActive.compareAndSet(false,true)) {
            return;
        }

        processTranscodingRulesUpdate();
        this.isActive.set(false);
    }

    private void processTranscodingRulesUpdate() {

        if(appConfiguration.isConfigGeneration() == false) {
            log.debug("Shibboleth configuration generation is disabled");
            return;
        }
        
        log.debug("Start shibboleth transcoding rules update");
        if(!shibbolethConfService.generateGluuAttributeRulesFile()) {
            log.error("Shibboleth transcoding rules update failed. (Please restart service manually)");
            return;
        }

        if(!shibbolethReloadService.reloadAttributeRegistryService()) {
            log.error("Shibboleth transcoding rules update failed. Attribute registry reload failed. (Please restart service manually)");
            return;
        }
        log.debug("Finished shibboleth transcoding rules update");
    }
}
