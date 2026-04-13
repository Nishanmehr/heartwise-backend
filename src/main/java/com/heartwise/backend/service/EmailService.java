package com.heartwise.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EmailService {

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    private static final String FROM_EMAIL = "nishantmehra172@gmail.com";
    private static final String FROM_NAME  = "HeartWise";

    @Async
    public void sendOtp(String toEmail, String name, String otp) {
        try {
            String body = "{"
                    + "\"sender\":{\"name\":\"" + FROM_NAME + "\",\"email\":\"" + FROM_EMAIL + "\"},"
                    + "\"to\":[{\"email\":\"" + toEmail + "\",\"name\":\"" + name + "\"}],"
                    + "\"subject\":\"HeartWise — Your Verification Code\","
                    + "\"textContent\":\"Hello " + name + "!\\n\\nYour HeartWise verification code is:\\n\\n   " + otp + "\\n\\nThis code expires in 10 minutes.\\n\\n— HeartWise Team\""
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("accept", "application/json")
                    .header("api-key", apiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Brevo API response: " + response.statusCode() + " - " + response.body());
        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
        }
    }
}