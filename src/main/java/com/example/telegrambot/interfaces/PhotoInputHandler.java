package com.example.telegrambot.interfaces;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface PhotoInputHandler extends UserStateHandler {
    SendMessage handlePhoto(String chatId, Message message);
}