package com.example.telegrambot.service.handler;

import com.example.telegrambot.enums.UserState;
import com.example.telegrambot.interfaces.InputHandler;
import com.example.telegrambot.keyboard.KeyboardFactory;
import com.example.telegrambot.prompts.PromptTemplates;
import com.example.telegrambot.service.OpenAiVisionService;
import com.example.telegrambot.service.TelegramFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Comparator;

@Service
public class AiAnalysisHandler implements InputHandler {

    private static final Logger logger = LoggerFactory.getLogger(AiAnalysisHandler.class);

    private final TelegramFileService telegramFileService;
    private final OpenAiVisionService openAiVisionService;

    public AiAnalysisHandler(TelegramFileService telegramFileService,
                             OpenAiVisionService openAiVisionService) {
        this.telegramFileService = telegramFileService;
        this.openAiVisionService = openAiVisionService;
    }

    @Override
    public boolean supports(String state) {
        return state.equals(UserState.AI_ANALYSIS_MODE.name());
    }

    @Override
    public SendMessage handle(String chatId, Message message) {
        logger.info("Handling photo input from [{}]", chatId);

        try {
            String fileId = message.getPhoto()
                    .stream()
                    .max(Comparator.comparingInt(PhotoSize::getFileSize))
                    .orElseThrow(() -> new RuntimeException("No photo found"))
                    .getFileId();

            String fileUrl = telegramFileService.getFileUrl(fileId);
            String aiReply = openAiVisionService.generateCaptionFromImageUrl(fileUrl, PromptTemplates.AI_ANALYSE_PROMPT);

            SendMessage response = new SendMessage(chatId, aiReply);
            response.setReplyMarkup(KeyboardFactory.exitKeyboard());
            return response;

        } catch (TelegramApiException e) {
            logger.error("Failed to retrieve Telegram photo URL", e);
            return new SendMessage(chatId, "Не вдалося обробити фото. Спробуйте ще раз.");
        }
    }
}

