package com.example.movienight;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LLMClient {

    private final Dotenv dotenv = Dotenv.configure()
    .directory("src/main/resources")
    .ignoreIfMissing()
    .load();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String call(String prompt) throws Exception {
        System.out.println("Making HTTP request to LLM...");
        System.out.println("Prompt: " + prompt);
        return makeHttpRequestToLLM(prompt);
    }

    private String makeHttpRequestToLLM(String prompt) throws Exception {
        String apiKey = dotenv.get("LLM_API_KEY");
        String apiUrl = dotenv.get("LLM_API_URL");
        String modelName = dotenv.get("LLM_MODEL_NAME");

        if (apiKey == null || apiUrl == null || modelName == null) {
            throw new IllegalStateException("Missing required environment variables: LLM_API_KEY, LLM_API_URL, or LLM_MODEL_NAME");
        }

        Map<String, Object> payload = Map.of(
            "model", modelName,
            "messages", new Object[] {
                Map.of("role", "user", "content", prompt)
            }
        );

        String requestBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("API request failed: " + response.body());
        }

        Map<?, ?> responseJson = objectMapper.readValue(response.body(), Map.class);
        return (String) ((Map<?, ?>)((Map<?, ?>)((java.util.List<?>) responseJson.get("choices")).get(0)).get("message")).get("content");
    }
}