package com.rudyii.hsw.helpers;

import com.rudyii.hsw.providers.IPStateProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

import static com.rudyii.hsw.enums.IPStateEnum.*;

@Component
public class IpMonitor {

    private final Map<String, String> ipResolver;
    private final IPStateProvider ipStateProvider;

    @Autowired
    public IpMonitor(Map ipResolver, IPStateProvider ipStateProvider) {
        this.ipResolver = ipResolver;
        this.ipStateProvider = ipStateProvider;
    }

    public ArrayList<String> getStates() {
        try {
            Thread.sleep(6000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<String> states = new ArrayList<>();

        ipResolver.forEach((ip, name) -> states.add(name + " is <b>" + getIpStateWithColor(ip) + "</b>"));

        return states;
    }

    private String getIpStateWithColor(String ip) {
        switch (ipStateProvider.getIPState(ip)) {
            case ONLINE:
                return "<font color=\"green\">" + ONLINE + "</font>";
            case OFFLINE:
                return "<font color=\"red\">" + OFFLINE + "</font>";
            default:
                return "<font color=\"yellow\">" + ERROR + "</font>";
        }
    }
}
