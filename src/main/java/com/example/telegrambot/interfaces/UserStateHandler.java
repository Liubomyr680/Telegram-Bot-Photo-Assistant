package com.example.telegrambot.interfaces;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface UserStateHandler {
    boolean supports(String state);
    SendMessage handle(String chatId, String messageText);
}
