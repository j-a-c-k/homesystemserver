package com.rudyii.hsw.services;

import com.rudyii.hsw.enums.ArmedModeEnum;
import com.rudyii.hsw.objects.events.ArmedEvent;
import com.rudyii.hsw.providers.IPStateProvider;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static com.rudyii.hsw.enums.ArmedModeEnum.AUTOMATIC;
import static com.rudyii.hsw.enums.ArmedModeEnum.MANUAL;
import static com.rudyii.hsw.enums.ArmedStateEnum.*;

@Slf4j
@Service
public class ArmedStateService {
    private final EventService eventService;
    private final IPStateProvider ipStateProvider;
    private long count = 1L;
    private Boolean systemArmed = false;
    private ArmedModeEnum armedMode = AUTOMATIC;

    @Autowired
    public ArmedStateService(EventService eventService, IPStateProvider ipStateProvider) {
        this.eventService = eventService;
        this.ipStateProvider = ipStateProvider;
    }

    @Scheduled(initialDelayString = "10000", fixedRateString = "60000")
    public void run() {
        this.count++;

        if (armedMode.equals(AUTOMATIC)) {
            if (!systemArmed && !ipStateProvider.mastersOnline()) {
                System.out.println(getMessage());
                arm(true);
            } else if (systemArmed && ipStateProvider.mastersOnline()) {
                System.out.println(getMessage());
                disarm(true);
            }
            System.out.println(getMessage());
        }
        System.out.println("System is UP for " + count + " minutes");
    }

    @NotNull
    private String getMessage() {
        return "System is " + (systemArmed ? ARMED : DISARMED) + "; " + (ipStateProvider.mastersOnline() ? "Any master is present." : "Any master is absent.");
    }

    private void disarm(boolean publishEvent) {
        this.systemArmed = false;
        if (publishEvent) {
            eventService.publish(new ArmedEvent(AUTOMATIC, DISARMED));
        }

        log.info("System " + DISARMED);
    }

    private void arm(boolean publishEvent) {
        this.systemArmed = true;
        if (publishEvent) {
            eventService.publish(new ArmedEvent(AUTOMATIC, ARMED));
        }

        log.info("System " + ARMED);
    }

    @Async
    @EventListener(ArmedEvent.class)
    public void onEvent(ArmedEvent event) {
        if (event.getArmedMode().equals(MANUAL) && event.getArmedState().equals(ARMED)) {
            this.armedMode = MANUAL;
            arm(false);
        } else if (event.getArmedMode().equals(MANUAL) && event.getArmedState().equals(DISARMED)) {
            this.armedMode = MANUAL;
            disarm(false);
        } else if (event.getArmedMode().equals(AUTOMATIC) && event.getArmedState().equals(AUTO)) {
            this.armedMode = AUTOMATIC;
        } else {
            log.info("Unsupported case: mode " + event.getArmedMode() + " with state: " + event.getArmedState());
        }
    }

    public boolean isSystemInAutoMode() {
        return armedMode.equals(AUTOMATIC);
    }

    public boolean isArmed() {
        return systemArmed;
    }

    public ArmedModeEnum getArmedMode() {
        return armedMode;
    }
}
