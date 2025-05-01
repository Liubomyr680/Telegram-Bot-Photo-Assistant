package com.example.telegrambot.component;

import com.example.telegrambot.interfaces.UserStateHandler;
import com.example.telegrambot.keyboard.KeyboardFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Objects;

@Component
public class GearChatHandler implements UserStateHandler {

    private final ChatClient chatClient;

    public GearChatHandler(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public boolean supports(String state) {
        return "GEAR_CHAT_MODE".equals(state);
    }

    @Override
    public SendMessage handle(String chatId, String messageText) {
        String checkPrompt = """
            Коротко: це питання стосується фототехніки (наприклад, камери, об'єктиви, освітлення, фон)?
            Відповідай лише: так або ні.
            Питання: %s
            """.formatted(messageText);

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

        // Крок 2: нормальний запит до AI
        String advicePrompt = """
            Ти — професійний фото-консультант. Відповідай українською.
            Порадь фототехніку (камеру, об'єктив, світло) для зйомки типу: %s.
            Відповідь чітко структурована, Якщо користувач задає додаткове птиання по якійсь із секцій,
            (наприклад, камери, об'єктиви, освітлення, фон) то давай відповідь тільки по цій секції, не потрібно знов розписувати про кожну з них.
            Всі відповіді повинні бути не більше 120 слів.
            """.formatted(messageText);

        String aiReply = chatClient.prompt()
                .user(advicePrompt)
                .call()
                .content();

        assert aiReply != null;
        SendMessage msg = new SendMessage(chatId, aiReply);
        msg.setReplyMarkup(KeyboardFactory.exitKeyboard());
        return msg;
    }
}
