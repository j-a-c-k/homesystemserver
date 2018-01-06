package com.rudyii.hsw.actions;

import com.google.gson.JsonObject;
import com.rudyii.hsw.actions.base.Action;
import com.rudyii.hsw.enums.FcmMessageEnum;
import com.rudyii.hsw.helpers.FCMSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.rudyii.hsw.helpers.FCMSender.TYPE_TO;

@Component
@Scope(value = "prototype")
public class FcmMessageSendAction implements Action {
    private static Logger LOG = LogManager.getLogger(FcmMessageSendAction.class);

    private String recipientToken, name;
    private JsonObject messageData;
    private FCMSender fcmSender;

    @Autowired
    public FcmMessageSendAction(FCMSender fcmSender) {
        this.fcmSender = fcmSender;
    }

    public FcmMessageSendAction withData(String name, String recipientToken, JsonObject messageData) {
        this.recipientToken = recipientToken;
        this.messageData = messageData;
        this.name = name;
        return this;
    }

    @Override
    public boolean fireAction() {
        FcmMessageEnum result;
        try {
            result = fcmSender.sendData(TYPE_TO, recipientToken, messageData);
        } catch (IOException e) {
            LOG.error("Failed to send message to: " + name, e);
            return false;
        }

        switch (result) {
            case SUCCESS:
                LOG.info("FCMessage successfully sent to: " + name);
                return true;
            case WARNING:
                LOG.warn("FCMessage was not sent to: " + name + " due to some internal Google issue.");
                return true;
            default:
                return true;
        }
    }
}