package com.heartwise.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class LoveLetterController {

    @Value("${GROQ_API_KEY}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @PostMapping("/love-letter")
    public ResponseEntity<?> generateLoveLetter(@RequestBody Map<String, String> body) {

        String senderName   = sanitize(body.getOrDefault("senderName", ""));
        String receiverName = sanitize(body.getOrDefault("receiverName", ""));
        String reason       = body.getOrDefault("reason", "birthday");
        String tone         = body.getOrDefault("tone", "romantic");
        String extra        = sanitize(body.getOrDefault("extra", ""));
        String language     = body.getOrDefault("language", "english");
        String length       = body.getOrDefault("length", "medium");

        String prompt = buildPrompt(senderName, receiverName, reason, tone, extra, language, length);

        try {
            int maxTokens = switch (length) {
                case "short" -> 200;
                case "long"  -> 800;
                default      -> 400;
            };

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", "llama-3.1-8b-instant",
                    "max_tokens", maxTokens,
                    "messages", new Object[]{
                            Map.of("role", "user", "content", prompt)
                    }
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("ERROR: " + response.body());
                return ResponseEntity.status(500)
                        .body(Map.of("message", "AI service error"));
            }

            JsonNode root = objectMapper.readTree(response.body());
            String letter = root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            return ResponseEntity.ok(Map.of("letter", letter));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Failed to generate letter"));
        }
    }

    private String buildPrompt(String sender, String receiver, String reason, String tone,
                               String extra, String language, String length) {

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

        String wordCount = switch (length) {
            case "short" -> "80-100 words";
            case "long"  -> "350-400 words";
            default      -> "180-220 words";
        };

        String langInstruction = switch (language) {
            case "hindi" -> "Write ENTIRELY in Hindi using simple natural language.";
            case "hinglish" -> "Write in Hinglish (mix of Hindi and English, casual texting style).";
            default -> "Write in simple natural English.";
        };

        return "You are a real human writing a personal love letter. Use simple words, real emotions, and natural tone. Avoid AI-like or overly poetic phrases."
                + " Write a " + toneText + " love letter from " + sender + " to " + receiver
                + " for " + reasonText + "."
                + (extra.isEmpty() ? "" : " Include these personal details: " + extra + ".")
                + " Length: " + wordCount + ". "
                + langInstruction
                + " Start with a warm greeting and end with " + sender + "'s name."
                + " Only return the letter.";
    }

    private String sanitize(String input) {
        if (input == null) return "";
        return input.replaceAll("[\\n\\r\\t]", " ")
                .replaceAll("[^a-zA-Z0-9 ,.?!']", "")
                .trim();
    }
}