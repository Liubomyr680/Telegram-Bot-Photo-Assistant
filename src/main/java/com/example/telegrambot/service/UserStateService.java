package com.example.telegrambot.service;

import com.example.telegrambot.enums.UserState;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserStateService {
    private final Map<String, UserState> userStates = new HashMap<>();

    public void setUserState(String chatId, UserState state) {
        userStates.put(chatId, state);
    }

    public UserState getUserState(String chatId) {
        return userStates.get(chatId);
    }

    public void clearUserState(String chatId) {
        userStates.remove(chatId);
    }
}
