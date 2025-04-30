package com.example.telegrambot.component;

import com.example.telegrambot.keyboard.KeyboardFactory;
import com.example.telegrambot.service.UserStateService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Objects;

@Component
public class MessageHandler {

    private final ChatClient chatClient;
    private final UserStateService userStateService;

    public MessageHandler(ChatClient.Builder chatClientBuilder, UserStateService userStateService) {
        this.chatClient = chatClientBuilder.build();
        this.userStateService = userStateService;
    }

    public SendMessage handleTextMessage(String chatId, String messageText) {
        return switch (messageText) {

            case "📸 Редагування Фото" -> new SendMessage(chatId,
                    "Надішліть фото, яке потрібно відредагувати 📷");

            case "🎯 Ідеї для фотосесії" -> new SendMessage(chatId,
                    "Напишіть тему або побажання для фотосесії 📝");

            case "🧠 AI-аналіз Фото" -> new SendMessage(chatId,
                    "Надішліть фото для аналізу якості 🧐");

            case "🏷️ Хештеги та Опис" -> new SendMessage(chatId,
                    "Надішліть ключові слова або фото для генерації опису та хештегів 📄");

            case "💰 Прайс-калькулятор" -> new SendMessage(chatId,
                    "Введіть тип зйомки та тривалість (наприклад: 'весілля 3 години') 💵");

            case "📷 Підказки по Обладнанню" -> {
                userStateService.setUserState(chatId, "GEAR_CHAT_MODE");
                SendMessage msg = new SendMessage(chatId,
                        "💬 Ви увійшли в режим консультації по техніці. Напишіть своє питання:");
                msg.setReplyMarkup(KeyboardFactory.exitKeyboard());
                yield msg;
            }

            case "↩ Вийти з режиму" -> {
                userStateService.clearUserState(chatId);
                SendMessage msg = new SendMessage(chatId, "✅ Ви вийшли з режиму консультації.");
                msg.setReplyMarkup(KeyboardFactory.mainKeyboard());
                yield msg;
            }

            default -> {
                String state = userStateService.getUserState(chatId);

                if ("GEAR_CHAT_MODE".equals(state)) {
                    // Крок 1: запит на перевірку тематики
                    String checkPrompt = """
            Коротко: це питання стосується фототехніки (наприклад, камери, об'єктиви, освітлення)?
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
                        yield msg;
                    }

                    // Крок 2: нормальний запит до AI
                    String advicePrompt = """
            Ти — професійний фото-консультант. Відповідай українською.
            Порадь фототехніку (камеру, об'єктив, світло) для зйомки типу: %s.
            Відповідь чітко структурована, не більше 150 слів.
            """.formatted(messageText);

                    String aiReply = chatClient.prompt()
                            .user(advicePrompt)
                            .call()
                            .content();

                    assert aiReply != null;
                    SendMessage msg = new SendMessage(chatId, aiReply);
                    msg.setReplyMarkup(KeyboardFactory.exitKeyboard());
                    yield msg;
                }

                SendMessage msg = new SendMessage(chatId,
                        "🤖 Я поки що розумію тільки команди або натиснення кнопок.");
                msg.setReplyMarkup(KeyboardFactory.mainKeyboard());
                yield msg;
            }
        };
    }
}
