package com.example.telegrambot.service.handler;

import com.example.telegrambot.dto.ChatMessage;
import com.example.telegrambot.enums.UserState;
import com.example.telegrambot.interfaces.UserStateHandler;
import com.example.telegrambot.keyboard.KeyboardFactory;
import com.example.telegrambot.prompts.PromptTemplates;
import com.example.telegrambot.service.GearChatMemoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GearChatHandler implements UserStateHandler {

    private static final Logger log = LoggerFactory.getLogger(GearChatHandler.class);

    private final ChatClient chatClient;
    private final GearChatMemoryService memoryService;

    public GearChatHandler(ChatClient.Builder builder, GearChatMemoryService memoryService) {
        this.chatClient = builder.build();
        this.memoryService = memoryService;
    }

    @Override
    public boolean supports(String state) {
        return state.equals(UserState.GEAR_CHAT_MODE.name());
    }

    @Override
    public SendMessage handle(String chatId, String messageText) {
        String checkPrompt = PromptTemplates.buildRelevancePrompt(messageText);

        String relevanceAnswer = Objects.requireNonNull(chatClient.prompt()
                        .user(checkPrompt)
                        .call()
                        .content())
                .trim()
                .toLowerCase();

        log.debug("Relevance answer from GPT: {}", relevanceAnswer);

        if (!relevanceAnswer.contains("так")) {
            log.info("⚠Message deemed irrelevant to gear context.");

            SendMessage msg = new SendMessage(chatId,
                    "📌 Це питання не стосується фототехніки.\nЯ можу допомогти лише з технічними порадами 📷");
            msg.setReplyMarkup(KeyboardFactory.exitKeyboard());
            return msg;
        }

        List<ChatMessage> history = memoryService.getMessages(chatId);

        // Перевірка, чи system-повідомлення вже є. Якщо ні — додаємо.
        if (history.stream().noneMatch(m -> m.role().equals("system"))) {
            log.info("System prompt not found in memory. Adding...");

            memoryService.addMessage(chatId, SYSTEM_PROMPT);
            history.add(0, SYSTEM_PROMPT);
        }

        // Додаємо нове питання користувача
        history.add(new ChatMessage("user", messageText));

        String fullPrompt = history.stream()
                .map(m -> m.role() + ": " + m.content())
                .collect(Collectors.joining("\n"));

        log.debug("Sending prompt to GPT:\n{}", fullPrompt);

        String aiReply = Objects.requireNonNull(chatClient.prompt()
                .user(fullPrompt)
                .call()
                .content());

        log.info("AI reply: {}", aiReply);

        memoryService.addMessage(chatId, new ChatMessage("user", messageText));
        memoryService.addMessage(chatId, new ChatMessage("assistant", aiReply));

        SendMessage msg = new SendMessage(chatId, aiReply);
        msg.setReplyMarkup(KeyboardFactory.exitKeyboard());
        return msg;
    }

    private static final ChatMessage SYSTEM_PROMPT =
            new ChatMessage("system", PromptTemplates.SYSTEM_PROMPT);
}