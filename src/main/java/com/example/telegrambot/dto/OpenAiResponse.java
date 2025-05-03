package com.example.telegrambot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiResponse {

    private List<Choice> choices;

    public List<Choice> getChoices() {
        return choices;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private Message message;

        public Message getMessage() {
            return message;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String role;
        private String content;

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}