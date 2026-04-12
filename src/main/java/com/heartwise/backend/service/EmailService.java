package com.heartwise.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send OTP email to user
     */
    public void sendOtp(String toEmail, String name, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("💞 HeartWise — Your Verification Code");
        message.setText(
                "Hello " + name + "!\n\n" +
                        "Welcome to HeartWise 💞\n\n" +
                        "Your verification code is:\n\n" +
                        "   " + otp + "\n\n" +
                        "This code will expire in 10 minutes.\n\n" +
                        "If you didn't create an account, please ignore this email.\n\n" +
                        "— HeartWise Team"
        );
        mailSender.send(message);
    }
}