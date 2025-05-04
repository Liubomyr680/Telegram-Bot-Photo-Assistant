package com.example.telegrambot.service;


import com.example.telegrambot.dto.OpenAiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static com.example.telegrambot.prompts.PromptTemplates.PHOTO_DESCRIPTION_PROMPT;

@Service
public class OpenAiVisionService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiVisionService.class);

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    public String generateCaptionFromImageUrl(String imageUrl) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> payload = Map.of(
                    "model", "gpt-4o",
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", List.of(
                                            Map.of("type", "image_url", "image_url", Map.of("url", imageUrl)),
                                            Map.of("type", "text", "text", PHOTO_DESCRIPTION_PROMPT)
                                    )
                            )
                    ),
                    "max_tokens", 300,
                    "temperature", 0.7
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(OPENAI_API_URL, request, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            OpenAiResponse aiResponse = objectMapper.readValue(response.getBody(), OpenAiResponse.class);

            return aiResponse.getChoices().get(0).getMessage().getContent();

        } catch (Exception e) {
            logger.error("Error generating caption from OpenAI", e);
            return "На жаль, сталася помилка під час обробки зображення.";
        }
    }
}
