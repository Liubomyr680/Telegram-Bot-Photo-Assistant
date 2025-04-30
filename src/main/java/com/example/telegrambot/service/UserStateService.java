package com.example.telegrambot.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserStateService {

    private final Map<String, String> userStates = new HashMap<>();

    public void setUserState(String chatId, String state) {
        userStates.put(chatId, state);
    }

    public String getUserState(String chatId) {
        return userStates.get(chatId);
    }

    public void clearUserState(String chatId) {
        userStates.remove(chatId);
    }
}
