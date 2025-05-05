package com.example.telegrambot.service;

import com.example.telegrambot.dto.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.springframework.util.StringUtils.truncate;

@Service
public class GearChatMemoryService {
    private static final Logger logger = LoggerFactory.getLogger(GearChatMemoryService.class);

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
            logger.debug("History limit exceeded. Attempting to remove oldest non-critical message.");
            removeOldestNonCritical(chatHistory);
        }

        chatHistory.add(message);
        logger.debug("[{}] Added new message: role={}, content='{}'", chatId, message.role(), truncate(message.content()));
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
                    logger.debug("Removing oldest non-critical message: role={}, content='{}'", m.role(), truncate(m.content()));
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
        logger.debug("[{}] Chat history cleared.", chatId);
    }
}
