package com.example.telegrambot.component;

import com.example.telegrambot.keyboard.KeyboardFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class PhotoAssistantBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final String botToken;
    private final MessageHandler messageHandler;

    public PhotoAssistantBot(
            @Value("${telegram.bot.username}") String botUsername,
            @Value("${telegram.bot.token}") String botToken,
            MessageHandler messageHandler
    ) {
        super(new DefaultBotOptions(), botToken);
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.messageHandler = messageHandler;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();

            SendMessage response;

            switch (userText) {
                case "/start" -> {
                    response = new SendMessage(chatId,
                            "👋 Вітаю у PicMentorBot!\n\n" +
                                    "Я допоможу вам у світі фотографії! 📸\n" +
                                    "Оберіть функцію нижче для початку 👇");
                    executeSafe(response);

                    SendMessage menuMessage = new SendMessage(chatId, "Оберіть функцію:");
                    menuMessage.setReplyMarkup(KeyboardFactory.mainKeyboard());
                    executeSafe(menuMessage);
                    return;
                }

                case "/help" -> {
                    response = new SendMessage(chatId, "📋 Доступні команди:\n/start - Почати роботу\n/help - Допомога");
                }

                default -> {
                    response = messageHandler.handleTextMessage(chatId, userText);
                }
            }

            executeSafe(response);
        }
    }

    private void executeSafe(SendMessage message) {
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
