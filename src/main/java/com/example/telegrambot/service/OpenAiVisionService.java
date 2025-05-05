package com.example.telegrambot.service;


import com.example.telegrambot.dto.OpenAiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiVisionService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiVisionService.class);

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    public String generateCaptionFromImageUrl(String imageUrl, String prompt) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> payload = Map.of(
                    "model", "gpt-4o",
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", List.of(
                                            Map.of("type", "image_url", "image_url", Map.of("url", imageUrl)),
                                            Map.of("type", "text", "text", prompt)
                                    )
                            )
                    ),
                    "max_tokens", 500,
                    "temperature", 0.7
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(OPENAI_API_URL, request, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            OpenAiResponse aiResponse = objectMapper.readValue(response.getBody(), OpenAiResponse.class);

            return aiResponse.choices().get(0).message().content();

        } catch (Exception e) {
            logger.error("❌ Error from OpenAI Vision API", e);
            return "На жаль, сталася помилка під час обробки зображення.";
        }
    }
}
