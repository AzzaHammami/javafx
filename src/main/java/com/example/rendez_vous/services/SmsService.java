package com.example.rendez_vous.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SmsService {
    private static final Logger logger = Logger.getLogger(SmsService.class.getName());
    // Clés Twilio réelles (ne pas partager publiquement)
    private static final String ACCOUNT_SID = "AC852a83dfe035b3d9bb3dde7519ce85cb";
    private static final String AUTH_TOKEN = "46e3a4b4c483e7d6e452c49efd84c6d6";
    private static final String FROM_PHONE = "+16203171921"; // Ton numéro Twilio trial
    private static final String MESSAGING_SERVICE_SID = "MGc36e1515ce8652399594b5a10780d2d2"; // Vérifie que c'est bien ton Messaging Service SID
    private static boolean initialized = false;

    public SmsService() {
        if (!initialized) {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            initialized = true;
        }
    }

    public void sendSms(String to, String body) {
        try {
            Message.creator(
                new com.twilio.type.PhoneNumber(to),
                new com.twilio.type.PhoneNumber(FROM_PHONE), // Utilisation du numéro Twilio
                body
            ).create();
            logger.info("SMS envoyé à : " + to + " via le numéro Twilio");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de l'envoi du SMS à : " + to, e);
        }
    }

    public void sendSmsWithServiceSid(String to, String body) {
        try {
            Message.creator(
                new com.twilio.type.PhoneNumber(to),
                MESSAGING_SERVICE_SID, // Utilisation du Messaging Service SID
                body
            ).create();
            logger.info("SMS sent to: " + to + " via MessagingServiceSid");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending SMS to: " + to, e);
        }
    }

    public void sendSmsAndLog(int userId, String to, String body) {
        logger.info("Sending SMS to userId=" + userId + ", phone=" + to);
        sendSms(to, body);
    }
}
