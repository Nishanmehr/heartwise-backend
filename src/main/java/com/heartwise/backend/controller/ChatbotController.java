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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class ChatbotController {

    @Value("${GROQ_API_KEY}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String SYSTEM_PROMPT =
            "You are Nova, a warm, empathetic and professional AI relationship & friendship advisor on HeartWise platform. "
                    + "You help people with all kinds of relationship and friendship problems — breakups, communication issues, trust problems, "
                    + "long distance relationships, toxic relationships, expressing feelings, making friends, friendship conflicts, and more. "
                    + "Your personality: caring, non-judgmental, supportive, wise, and friendly. "
                    + "You give practical, actionable advice in a conversational tone. "
                    + "Keep responses concise — 3 to 5 sentences max unless the user needs more. "
                    + "If the user writes in Hindi or Hinglish, respond in the same language. "
                    + "Never give medical or legal advice. If someone seems in crisis, suggest professional help. "
                    + "Always end with a follow-up question to keep the conversation going.";

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> body) {
        try {
            List<Map<String, String>> history = (List<Map<String, String>>) body.get("messages");
            if (history == null || history.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("message", "No messages provided"));

            // Build messages array with system prompt
            var messages = new java.util.ArrayList<Map<String, String>>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            messages.addAll(history);

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "max_tokens", 300,
                    "messages", messages
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
                System.err.println("Groq error: " + response.body());
                return ResponseEntity.status(500).body(Map.of("message", "AI service error"));
            }

            JsonNode root = objectMapper.readTree(response.body());
            String reply = root.path("choices").get(0)
                    .path("message").path("content").asText();

            return ResponseEntity.ok(Map.of("reply", reply));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Failed to get response"));
        }
    }
}