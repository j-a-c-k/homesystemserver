package com.rudyii.hsw.providers;

import com.google.gson.JsonObject;
import com.rudyii.hsw.actions.base.ActionsFactory;
import com.rudyii.hsw.objects.Attachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class NotificationsService {
    private final ActionsFactory actionsFactory;

    @Lazy
    @Autowired
    public NotificationsService(ActionsFactory actionsFactory) {
        this.actionsFactory = actionsFactory;
    }

    public void sendEmail(String subject, ArrayList<String> body, ArrayList<Attachment> attachments) {
        actionsFactory.orderMailSenderAction(subject, body, attachments);
    }

    public void sendFcmMessage(String name, String recipientToken, JsonObject messageData) {
        actionsFactory.orderMessageSendAction(name, recipientToken, messageData);
    }
}
