package com.example.telegrambot.interfaces;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface InputHandler {
    boolean supports(String state);
    SendMessage handle(String chatId, Message message);
}
