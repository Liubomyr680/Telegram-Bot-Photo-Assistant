package com.example.telegrambot.component;

import com.example.telegrambot.keyboard.KeyboardFactory;
import com.example.telegrambot.service.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class PhotoAssistantBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(PhotoAssistantBot.class);

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
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String chatId = message.getChatId().toString();

            if (message.hasText()) {
                String userText = message.getText();
                logger.debug("User [{}] sent message: {}", chatId, userText);

                SendMessage response;
                switch (userText) {
                    case "/start" -> {
                        logger.debug("User [{}] triggered /start command", chatId);

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
                        logger.debug("User [{}] triggered /help command", chatId);
                        response = new SendMessage(chatId, "📋 Доступні команди:\n/start - Почати роботу\n/help - Допомога");
                    }
                    default -> {
                        logger.debug("User [{}] sent message for processing: {}", chatId, userText);
                        response = messageHandler.handleTextMessage(chatId, userText);
                    }
                }

                executeSafe(response);

            } else if (message.hasPhoto()) {
                logger.debug("User [{}] sent a photo", chatId);
                SendMessage response = messageHandler.handlePhotoMessage(chatId, message);
                executeSafe(response);
            }
        }
    }

    private void executeSafe(SendMessage message) {
        try {
            execute(message);
        } catch (Exception e) {
            logger.error("Failed to send message: {}", message.getText(), e);
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

