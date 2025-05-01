package com.example.telegrambot.component;

import com.example.telegrambot.interfaces.UserStateHandler;
import com.example.telegrambot.keyboard.KeyboardFactory;
import com.example.telegrambot.service.UserStateService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
public class MessageHandler {


    private final List<UserStateHandler> stateHandlers;
    private final UserStateService userStateService;
    private final GearChatMemoryService gearChatMemoryService;

    public MessageHandler(
            List<UserStateHandler> stateHandlers,
            UserStateService userStateService,
            GearChatMemoryService gearChatMemoryService
    ) {
        this.stateHandlers = stateHandlers;
        this.userStateService = userStateService;
        this.gearChatMemoryService = gearChatMemoryService;
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
                gearChatMemoryService.clearMemory(chatId); // clear chat memory  after Exit

                SendMessage msg = new SendMessage(chatId, "✅ Ви вийшли з режиму консультації.");
                msg.setReplyMarkup(KeyboardFactory.mainKeyboard());
                yield msg;
            }

            default -> {
                String state = userStateService.getUserState(chatId);

                for (UserStateHandler handler : stateHandlers) {
                    if (handler.supports(state)) {
                        yield handler.handle(chatId, messageText);
                    }
                }

                SendMessage msg = new SendMessage(chatId,
                        "🤖 Я поки що розумію тільки команди або натиснення кнопок.");
                msg.setReplyMarkup(KeyboardFactory.mainKeyboard());
                yield msg;
            }
        };
    }
}
