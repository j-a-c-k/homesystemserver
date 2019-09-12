package com.rudyii.hsw.objects.events;

import java.util.concurrent.ConcurrentHashMap;

import static com.rudyii.hsw.configuration.OptionsService.CAMERAS;

public class OptionsChangedEvent extends EventBase {
    private ConcurrentHashMap<String, Object> options;

    public OptionsChangedEvent(ConcurrentHashMap<String, Object> options) {
        this.options = options;
    }

    public Object getOption(String option) {
        return options.get(option);
    }

    public ConcurrentHashMap<String, Object> getCameraOptions(String cameraName) {
        return (ConcurrentHashMap<String, Object>) ((ConcurrentHashMap<String, Object>) options.get(CAMERAS)).get(cameraName);
    }

}
