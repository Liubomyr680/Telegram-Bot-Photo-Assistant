package com.example.telegrambot.component;

import com.example.telegrambot.interfaces.UserStateHandler;
import com.example.telegrambot.keyboard.KeyboardFactory;
import com.example.telegrambot.prompts.PromptTemplates;
import com.example.telegrambot.record.ChatMessage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class GearChatHandler implements UserStateHandler {

    private final ChatClient chatClient;
    private final GearChatMemoryService memoryService;

    public GearChatHandler(ChatClient.Builder builder, GearChatMemoryService memoryService) {
        this.chatClient = builder.build();
        this.memoryService = memoryService;
    }

    @Override
    public boolean supports(String state) {
        return "GEAR_CHAT_MODE".equals(state);
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

        if (!relevanceAnswer.contains("так")) {
            SendMessage msg = new SendMessage(chatId,
                    "📌 Це питання не стосується фототехніки.\nЯ можу допомогти лише з технічними порадами 📷");
            msg.setReplyMarkup(KeyboardFactory.exitKeyboard());
            return msg;
        }

        List<ChatMessage> history = memoryService.getMessages(chatId);

        // Перевірка, чи system-повідомлення вже є. Якщо ні — додаємо.
        if (history.stream().noneMatch(m -> m.role().equals("system"))) {
            memoryService.addMessage(chatId, SYSTEM_PROMPT);
            history.add(0, SYSTEM_PROMPT);
        }

        // Додаємо нове питання користувача
        history.add(new ChatMessage("user", messageText));

        String fullPrompt = history.stream()
                .map(m -> m.role() + ": " + m.content())
                .collect(Collectors.joining("\n"));

        String aiReply = Objects.requireNonNull(chatClient.prompt()
                .user(fullPrompt)
                .call()
                .content());

        memoryService.addMessage(chatId, new ChatMessage("user", messageText));
        memoryService.addMessage(chatId, new ChatMessage("assistant", aiReply));

        SendMessage msg = new SendMessage(chatId, aiReply);
        msg.setReplyMarkup(KeyboardFactory.exitKeyboard());
        return msg;
    }

    private static final ChatMessage SYSTEM_PROMPT =
            new ChatMessage("system", PromptTemplates.SYSTEM_PROMPT);
}


//        // Крок 2: нормальний запит до AI
//        String advicePrompt = """
//            Ти — професійний фото-консультант. Відповідай українською.
//            Порадь фототехніку (камеру, об'єктив, світло) для зйомки типу: %s.
//            Відповідь чітко структурована, Якщо користувач задає додаткове птиання по якійсь із секцій,
//            (наприклад, камери, об'єктиви, освітлення, фон) то давай відповідь тільки по цій секції, не потрібно знов розписувати про кожну з них.
//            Всі відповіді повинні бути не більше 120 слів.
//            """.formatted(messageText);
//
//        String aiReply = chatClient.prompt()
//                .user(advicePrompt)
//                .call()
//                .content();
//
//        assert aiReply != null;
//        SendMessage msg = new SendMessage(chatId, aiReply);
//        msg.setReplyMarkup(KeyboardFactory.exitKeyboard());
//        return msg;