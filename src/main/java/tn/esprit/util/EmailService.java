package tn.esprit.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

public class EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    
    // Configuration SMTP Mailtrap
    private static final String SMTP_HOST = "sandbox.smtp.mailtrap.io";
    private static final int SMTP_PORT = 2525;
    private static final String SMTP_USERNAME = "45a7fa53e85f61";
    private static final String SMTP_PASSWORD = "175433684337fa";
    private static final String FROM_EMAIL = "cabinet.medical@example.com";
    private static final String FROM_NAME = "Cabinet Médical";
    
    private static Session getMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });
    }
    
    private static void sendEmail(String to, String subject, String content) throws MessagingException, UnsupportedEncodingException {
        LOGGER.info("Préparation de l'envoi d'email via SMTP à : " + to);
        
        Session session = getMailSession();
        Message message = new MimeMessage(session);
        
        message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(content);
        
        LOGGER.info("Envoi de l'email...");
        Transport.send(message);
        LOGGER.info("Email envoyé avec succès à : " + to);
    }
    
    public static void sendAppointmentConfirmation(String to, String patientName, String doctorName, String date) {
        try {
            String subject = "Confirmation de rendez-vous";
            String content = String.format("""
                Cher(e) %s,
                
                Votre rendez-vous avec Dr. %s le %s a été confirmé.
                
                Cordialement,
                L'équipe du Cabinet Médical""", patientName, doctorName, date);
            
            sendEmail(to, subject, content);
        } catch (MessagingException | UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi de l'email de confirmation", e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email de confirmation", e);
        }
    }
    
    public static void sendAppointmentReminder(String to, String patientName, String doctorName, String date) {
        try {
            String subject = "Rappel de rendez-vous";
            String content = String.format("""
                Cher(e) %s,
                
                Nous vous rappelons votre rendez-vous avec Dr. %s le %s.
                
                Cordialement,
                L'équipe du Cabinet Médical""", patientName, doctorName, date);
            
            sendEmail(to, subject, content);
        } catch (MessagingException | UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi de l'email de rappel", e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email de rappel", e);
        }
    }
    
    public static void sendPasswordResetCode(String to, String resetCode) {
        try {
            String subject = "Code de réinitialisation de mot de passe";
            String content = String.format("""
                Bonjour,
                
                Votre code de réinitialisation de mot de passe est : %s
                
                Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.
                
                Cordialement,
                L'équipe du Cabinet Médical""", resetCode);
            
            sendEmail(to, subject, content);
            LOGGER.info("Code de réinitialisation envoyé : " + resetCode);
        } catch (MessagingException | UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi du code de réinitialisation", e);
            throw new RuntimeException("Erreur lors de l'envoi du code de réinitialisation", e);
        }
    }
} 