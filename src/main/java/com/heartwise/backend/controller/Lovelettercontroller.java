package com.heartwise.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class LoveLetterController {

    @Value("${GROQ_API_KEY}")
    private String groqApiKey;

    @PostMapping("/love-letter")
    public ResponseEntity<?> generateLoveLetter(@RequestBody Map<String, String> body) {

        String senderName   = body.getOrDefault("senderName",   "");
        String receiverName = body.getOrDefault("receiverName", "");
        String reason       = body.getOrDefault("reason",       "birthday");
        String tone         = body.getOrDefault("tone",         "romantic");
        String extra        = body.getOrDefault("extra",        "");

        String prompt = buildPrompt(senderName, receiverName, reason, tone, extra);

        try {
            String requestBody = "{"
                    + "\"model\":\"claude-sonnet-4-20250514\","
                    + "\"max_tokens\":1000,"
                    + "\"messages\":[{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}]"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.anthropic.com/v1/messages"))
                    .header("Content-Type",      "application/json")
                    .header("x-api-key",         groqApiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return ResponseEntity.status(500).body(Map.of("message", "AI service error"));
            }

            // Extract text from response
            String responseBody = response.body();
            int textStart = responseBody.indexOf("\"text\":\"") + 8;
            int textEnd   = responseBody.lastIndexOf("\"");
            String letter = responseBody.substring(textStart, textEnd)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");

            return ResponseEntity.ok(Map.of("letter", letter));

        } catch (Exception e) {
            System.err.println("Love letter generation failed: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Failed to generate letter"));
        }
    }

    private String buildPrompt(String sender, String receiver, String reason, String tone, String extra) {
        String reasonText = switch (reason) {
            case "birthday"    -> "their birthday";
            case "apology"     -> "an apology after doing something wrong";
            case "missing"     -> "missing them deeply";
            case "anniversary" -> "their anniversary";
            case "fight"       -> "after a fight to make up";
            case "firstlove"   -> "expressing love for the first time";
            case "longdistance"-> "being in a long distance relationship";
            case "goodmorning" -> "wishing them a good morning";
            case "goodnight"   -> "wishing them a good night";
            case "propose"     -> "proposing marriage";
            case "cheer"       -> "cheering them up when they are sad";
            default            -> "just because they love them";
        };

        String toneText = switch (tone) {
            case "cute"      -> "cute, sweet and adorable";
            case "funny"     -> "funny, playful and lighthearted";
            case "emotional" -> "emotional, heartfelt and sincere";
            case "poetic"    -> "poetic, artistic and beautifully written";
            default          -> "deeply romantic and passionate";
        };

        return "Write a " + toneText + " love letter from " + sender + " to " + receiver
                + " for " + reasonText + "."
                + (extra.isEmpty() ? "" : " Personal details: " + extra + ".")
                + " Make it personal, genuine and about 150-200 words."
                + " Start with 'My dearest " + receiver + ",' and end with '" + sender + "'."
                + " Only write the letter, nothing else.";
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}