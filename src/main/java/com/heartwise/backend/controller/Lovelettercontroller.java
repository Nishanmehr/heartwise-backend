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
public class Lovelettercontroller {

    @Value("${GROQ_API_KEY}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @PostMapping("/love-letter")
    public ResponseEntity<?> generateLoveLetter(@RequestBody Map<String, String> body) {

        String senderName   = sanitize(body.getOrDefault("senderName",   ""));
        String receiverName = sanitize(body.getOrDefault("receiverName", ""));
        String reason       = body.getOrDefault("reason",   "birthday");
        String tone         = body.getOrDefault("tone",     "romantic");
        String extra        = sanitize(body.getOrDefault("extra", ""));
        String language     = body.getOrDefault("language", "english");
        String length       = body.getOrDefault("length",   "medium");

        String prompt = buildPrompt(senderName, receiverName, reason, tone, extra, language, length);

        try {
            int maxTokens = switch (length) {
                case "short" -> 200;
                case "long"  -> 900;
                default      -> 450;
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
                System.out.println("Groq ERROR: " + response.body());
                return ResponseEntity.status(500).body(Map.of("message", "AI service error"));
            }

            JsonNode root = objectMapper.readTree(response.body());
            String letter = root.path("choices").get(0)
                    .path("message").path("content").asText();

            return ResponseEntity.ok(Map.of("letter", letter));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Failed to generate letter"));
        }
    }

    private String buildPrompt(String sender, String receiver, String reason, String tone,
                               String extra, String language, String length) {

        String reasonText = switch (reason) {
            case "birthday"     -> "birthday";
            case "apology"      -> "apology — saying sorry for a mistake";
            case "missing"      -> "missing them";
            case "anniversary"  -> "anniversary";
            case "fight"        -> "making up after a fight";
            case "firstlove"    -> "expressing love for the first time";
            case "longdistance" -> "long distance relationship";
            case "goodmorning"  -> "good morning";
            case "goodnight"    -> "good night";
            case "propose"      -> "marriage proposal";
            case "cheer"        -> "cheering them up";
            default             -> "expressing love";
        };

        String toneText = switch (tone) {
            case "cute"      -> "cute and sweet";
            case "funny"     -> "funny and playful";
            case "emotional" -> "emotional and heartfelt";
            case "poetic"    -> "poetic and beautiful";
            default          -> "romantic and loving";
        };

        String wordCount = switch (length) {
            case "short" -> "80 to 100 words";
            case "long"  -> "350 to 400 words";
            default      -> "180 to 220 words";
        };

        String extraLine = extra.isEmpty() ? "" : "Mention these personal details naturally: " + extra + ". ";

        if (language.equals("hindi")) {
            return sender + " naam ka ek insaan " + receiver + " ko " + reasonText + " par ek dil se letter likh raha hai.\n"
                    + "Tone: " + toneText + ".\n"
                    + "Length: " + wordCount + ".\n"
                    + extraLine + "\n"
                    + "RULES:\n"
                    + "1. Seedhi simple Hindi mein likho jaise ek normal Indian insaan likhta hai\n"
                    + "2. Koi bakwaas ya ulte seedhe words mat use karo\n"
                    + "3. Natural aur real emotions rakho\n"
                    + "4. Letter '" + receiver + ",' se shuru karo\n"
                    + "5. '" + sender + "' se khatam karo\n"
                    + "6. Sirf letter likho, koi explanation mat do\n"
                    + "7. IMPORTANT: Correct Hindi grammar use karo, koi made-up words mat use karo";

        } else if (language.equals("hinglish")) {
            return "Write a love letter from " + sender + " to " + receiver + " for " + reasonText + ".\n"
                    + "Tone: " + toneText + ".\n"
                    + "Length: " + wordCount + ".\n"
                    + extraLine + "\n"
                    + "RULES:\n"
                    + "1. Write in Hinglish — natural mix like Indians text each other\n"
                    + "2. Example style: 'Yaar, tujhe pata hai tu meri life mein kitna important hai...'\n"
                    + "3. Use simple words, real feelings, casual tone\n"
                    + "4. DO NOT write fully in Hindi or fully in English\n"
                    + "5. Start with '" + receiver + ",' and end with '" + sender + "'\n"
                    + "6. Write ONLY the letter, nothing else";

        } else {
            return "Write a " + toneText + " love letter from " + sender + " to " + receiver
                    + " for " + reasonText + ".\n"
                    + "Length: " + wordCount + ".\n"
                    + extraLine + "\n"
                    + "RULES:\n"
                    + "1. Write like a real human, NOT like an AI\n"
                    + "2. Use simple everyday words and natural sentences\n"
                    + "3. Include genuine emotions — not overly dramatic\n"
                    + "4. Make it personal and specific, not generic\n"
                    + "5. Start with 'My dearest " + receiver + ",' and end with '" + sender + "'\n"
                    + "6. Write ONLY the letter, nothing else";
        }
    }

    private String sanitize(String input) {
        if (input == null) return "";
        return input.replaceAll("[\\n\\r\\t]", " ")
                .replaceAll("[^a-zA-Z0-9\\u0900-\\u097F ,.?!']", "")
                .trim();
    }
}