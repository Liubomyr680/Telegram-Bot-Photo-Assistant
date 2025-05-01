package com.example.telegrambot.component;

import com.example.telegrambot.record.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GearChatMemoryService {

    private static final int MAX_HISTORY = 5;
    private final Map<String, List<ChatMessage>> memory = new HashMap<>();

    /**
     * Додає нове повідомлення в історію чату для конкретного користувача (chatId).
     * Якщо перевищено ліміт, видаляється найстаріше повідомлення (але не system і не перше user).
     */
    public void addMessage(String chatId, ChatMessage message) {
        memory.computeIfAbsent(chatId, k -> new ArrayList<>());
        List<ChatMessage> chatHistory = memory.get(chatId);

        // Видаляємо найстаріше не-system повідомлення, якщо перевищено ліміт
        if (countNonSystemMessages(chatHistory) >= MAX_HISTORY) {
            removeOldestNonCritical(chatHistory);
        }

        chatHistory.add(message);
    }

    private int countNonSystemMessages(List<ChatMessage> history) {
        return (int) history.stream()
                .filter(m -> !m.role().equals("system"))
                .count();
    }

    private void removeOldestNonCritical(List<ChatMessage> history) {
        for (int i = 0; i < history.size(); i++) {
            ChatMessage m = history.get(i);
            if (!m.role().equals("system")) {
                boolean isFirstUser = i == 1 && history.get(0).role().equals("system");
                if (!isFirstUser) {
                    history.remove(i);
                    break;
                }
            }
        }
    }

    public List<ChatMessage> getMessages(String chatId) {
        return new ArrayList<>(memory.getOrDefault(chatId, new ArrayList<>()));
    }

    public void clearMemory(String chatId) {
        memory.remove(chatId);
    }
}
