package com.example.telegrambot.component;

import com.example.telegrambot.interfaces.PhotoInputHandler;
import com.example.telegrambot.keyboard.KeyboardFactory;
import com.example.telegrambot.service.OpenAiVisionService;
import com.example.telegrambot.service.TelegramFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Comparator;

@Component
public class CaptionHandler implements PhotoInputHandler {

    private static final Logger logger = LoggerFactory.getLogger(CaptionHandler.class);

    private final TelegramFileService telegramFileService;
    private final OpenAiVisionService openAiVisionService;

    public CaptionHandler(TelegramFileService telegramFileService,
                          OpenAiVisionService openAiVisionService) {
        this.telegramFileService = telegramFileService;
        this.openAiVisionService = openAiVisionService;
    }

    @Override
    public boolean supports(String state) {
        return "CAPTION_MODE".equals(state);
    }

    @Override
    public SendMessage handlePhoto(String chatId, Message message) {
        logger.info("Handling photo input from [{}]", chatId);

        try {
            String fileId = message.getPhoto()
                    .stream()
                    .max(Comparator.comparingInt(PhotoSize::getFileSize))
                    .orElseThrow(() -> new RuntimeException("No photo found"))
                    .getFileId();

            String fileUrl = telegramFileService.getFileUrl(fileId);
            String aiReply = openAiVisionService.generateCaptionFromImageUrl(fileUrl);

            SendMessage response = new SendMessage(chatId, aiReply);
            response.setReplyMarkup(KeyboardFactory.exitKeyboard());
            return response;

        } catch (TelegramApiException e) {
            logger.error("Failed to retrieve Telegram photo URL", e);
            return new SendMessage(chatId, "Не вдалося обробити фото. Спробуйте ще раз.");
        }
    }
}



