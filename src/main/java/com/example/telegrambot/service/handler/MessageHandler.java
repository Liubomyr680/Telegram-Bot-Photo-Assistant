package com.example.telegrambot.service.handler;

import com.example.telegrambot.enums.UserState;
import com.example.telegrambot.interfaces.PhotoInputHandler;
import com.example.telegrambot.interfaces.UserStateHandler;
import com.example.telegrambot.keyboard.KeyboardFactory;
import com.example.telegrambot.service.GearChatMemoryService;
import com.example.telegrambot.service.UserStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Service
public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    private final List<UserStateHandler> stateHandlers;
    private final List<PhotoInputHandler> photoHandlers;
    private final UserStateService userStateService;
    private final GearChatMemoryService gearChatMemoryService;

    public MessageHandler(
            List<UserStateHandler> stateHandlers,
            List<PhotoInputHandler> photoHandlers,
            UserStateService userStateService,
            GearChatMemoryService gearChatMemoryService
    ) {
        this.stateHandlers = stateHandlers;
        this.photoHandlers = photoHandlers;
        this.userStateService = userStateService;
        this.gearChatMemoryService = gearChatMemoryService;
    }

    public SendMessage handleTextMessage(String chatId, String messageText) {
        logger.info("Received message from [{}]: {}", chatId, messageText);

        return switch (messageText) {
            case "📸 Редагування Фото" -> new SendMessage(chatId, "Надішліть фото, яке потрібно відредагувати 📷");

            case "🎯 Ідеї для фотосесії" -> new SendMessage(chatId, "Напишіть тему або побажання для фотосесії 📝");

            case "🧠 AI-аналіз Фото" -> handleAiAnalysisMode(chatId);

            case "🏷️ Хештеги та Опис" -> handleCaptionMode(chatId);

            case "💰 Прайс-калькулятор" ->
                    new SendMessage(chatId, "Введіть тип зйомки та тривалість (наприклад: 'весілля 3 години') 💵");

            case "📷 Підказки по Обладнанню" -> handleGearChatMode(chatId);

            case "↩ Вийти з режиму" -> handleExitMode(chatId);

            default -> handleFallback(chatId, messageText);
        };
    }

    public SendMessage handlePhotoMessage(String chatId, Message message) {
        logger.info("Received photo from [{}]", chatId);

        UserState state = userStateService.getUserState(chatId);
        for (PhotoInputHandler handler : photoHandlers) {
            if (handler.supports(state.name())) {
                logger.debug("Delegating photo to handler: {}", handler.getClass().getSimpleName());
                return handler.handlePhoto(chatId, message);
            }
        }

        logger.warn("No handler matched for photo from [{}] in state [{}]", chatId, state);
        SendMessage msg = new SendMessage(chatId,
                "📷 Надіслане фото не оброблено. Будь ласка, оберіть режим у меню.");
        msg.setReplyMarkup(KeyboardFactory.mainKeyboard());
        return msg;
    }

    private SendMessage handleCaptionMode(String chatId) {
        userStateService.setUserState(chatId, UserState.CAPTION_MODE);
        SendMessage msg = new SendMessage(chatId, """
                ✍️ Надішліть фото, а я згенерую опис + релевантні хештеги.
                """);
        msg.setReplyMarkup(KeyboardFactory.exitKeyboard());
        return msg;
    }

    private SendMessage handleGearChatMode(String chatId) {
        userStateService.setUserState(chatId, UserState.GEAR_CHAT_MODE);
        SendMessage msg = new SendMessage(chatId, """
                💬 Ви увійшли в режим консультації по техніці.
                Напишіть тип зйомки (наприклад: весілля, портрет, зйомка в студії і т.д.).
                А я підкажу, яке фотообладнання вам найкраще підійде (камера, об’єктив, освітлення, фон).
                """);
        msg.setReplyMarkup(KeyboardFactory.exitKeyboard());
        return msg;
    }

    private SendMessage handleAiAnalysisMode(String chatId) {
        userStateService.setUserState(chatId, UserState.AI_ANALYSIS_MODE);
        SendMessage msg = new SendMessage(chatId,
                "🧠 Надішліть фото, і я проаналізую його якість, композицію, світло і настрій 📸");
        msg.setReplyMarkup(KeyboardFactory.exitKeyboard());
        return msg;
    }

    private SendMessage handleExitMode(String chatId) {
        userStateService.clearUserState(chatId);
        gearChatMemoryService.clearMemory(chatId);
        SendMessage msg = new SendMessage(chatId, "✅ Ви вийшли з режиму консультації.");
        msg.setReplyMarkup(KeyboardFactory.mainKeyboard());
        return msg;
    }

    private SendMessage handleFallback(String chatId, String messageText) {
        UserState state = userStateService.getUserState(chatId);

        if (state == UserState.CAPTION_MODE) {
            return new SendMessage(chatId,
                    "📷 Я можу створити опис тільки для фото. Будь ласка, надішліть зображення.");
        }

        if (state == UserState.AI_ANALYSIS_MODE) {
            return new SendMessage(chatId,
                    "🧠 Я можу проаналізувати лише фото. Будь ласка, завантажте зображення.");
        }

        for (UserStateHandler handler : stateHandlers) {
            if (handler.supports(state.name())) {
                return handler.handle(chatId, messageText);
            }
        }

        SendMessage msg = new SendMessage(chatId,
                "🤖 Я поки що розумію тільки команди або натиснення кнопок.");
        msg.setReplyMarkup(KeyboardFactory.mainKeyboard());
        return msg;
    }

}
