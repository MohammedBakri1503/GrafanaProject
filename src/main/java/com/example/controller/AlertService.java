package com.example.controller;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

    private final JavaMailSender mailSender;

    public AlertService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAlert(String message) {
        String toEmail = "recipient_email@example.com"; // Change this to recipient's email
        String subject = "⚠️ Stock Anomaly Detected!";

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(toEmail);
        email.setSubject(subject);
        email.setText(message);
        email.setFrom("your_email@gmail.com"); // Must match your SMTP user

        mailSender.send(email);
        System.out.println("📧 Alert sent to " + toEmail);
    }
}

