package com.heartwise.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Brevo verified sender email
    private static final String FROM_EMAIL = "nishantmehra172@gmail.com";
    private static final String FROM_NAME  = "HeartWise";

    @Async
    public void sendOtp(String toEmail, String name, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_NAME + " <" + FROM_EMAIL + ">");
            message.setTo(toEmail);
            message.setSubject("HeartWise — Your Verification Code");
            message.setText(
                    "Hello " + name + "!\n\n" +
                            "Welcome to HeartWise\n\n" +
                            "Your verification code is:\n\n" +
                            "   " + otp + "\n\n" +
                            "This code will expire in 10 minutes.\n\n" +
                            "If you didn't create an account, please ignore this email.\n\n" +
                            "— HeartWise Team"
            );
            mailSender.send(message);
            System.out.println("OTP email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("OTP email failed to " + toEmail + ": " + e.getMessage());
        }
    }
}