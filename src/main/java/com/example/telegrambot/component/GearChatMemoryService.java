package com.example.telegrambot.component;

import com.example.telegrambot.record.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GearChatMemoryService {

    private static final int MAX_HISTORY = 5;
    private final Map<String, Deque<ChatMessage>> memory = new HashMap<>();

    public void addMessage(String chatId, ChatMessage message) {
        memory.computeIfAbsent(chatId, k -> new ArrayDeque<>());

        Deque<ChatMessage> chatHistory = memory.get(chatId);
        if (chatHistory.size() >= MAX_HISTORY) {
            chatHistory.pollFirst(); // remove oldest
        }
        chatHistory.addLast(message);
    }

    public List<ChatMessage> getMessages(String chatId) {
        return new ArrayList<>(memory.getOrDefault(chatId, new ArrayDeque<>()));
    }

    public void clearMemory(String chatId) {
        memory.remove(chatId);
    }
}
