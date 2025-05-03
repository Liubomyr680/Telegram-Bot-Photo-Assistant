package com.example.telegrambot.service;

import com.example.telegrambot.component.PhotoAssistantBot;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramFileService {

    private final PhotoAssistantBot telegramBot;

    public TelegramFileService(@Lazy PhotoAssistantBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public String getFileUrl(String fileId) throws TelegramApiException {
        GetFile getFile = new GetFile(fileId);
        File file = telegramBot.execute(getFile);
        return "https://api.telegram.org/file/bot" + telegramBot.getBotToken() + "/" + file.getFilePath();
    }
}
