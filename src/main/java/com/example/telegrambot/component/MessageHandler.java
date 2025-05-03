package com.example.telegrambot.component;

import com.example.telegrambot.interfaces.PhotoInputHandler;
import com.example.telegrambot.interfaces.UserStateHandler;
import com.example.telegrambot.keyboard.KeyboardFactory;
import com.example.telegrambot.service.UserStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

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
        logger.info("Received message from [{}]: {}", chatId, messageText);

        return switch (messageText) {

            case "📸 Редагування Фото" -> {
                logger.debug("User [{}] selected: Редагування Фото", chatId);

                yield new SendMessage(chatId,
                        "Надішліть фото, яке потрібно відредагувати 📷");
            }

            case "🎯 Ідеї для фотосесії" -> {
                logger.debug("User [{}] selected: Ідеї для фотосесії", chatId);

                yield new SendMessage(chatId,
                        "Напишіть тему або побажання для фотосесії 📝");
            }

            case "🧠 AI-аналіз Фото" -> {
                logger.debug("User [{}] selected: AI-аналіз Фото", chatId);

                yield new SendMessage(chatId,
                        "Надішліть фото для аналізу якості 🤔");
            }

            case "🏷️ Хештеги та Опис" -> {
                userStateService.setUserState(chatId, "CAPTION_MODE");
                SendMessage msg = new SendMessage(chatId, """
            ✍️ Надішліть текст або фото (або і те, і те), і я згенерую опис + релевантні хештеги.
            """);
                msg.setReplyMarkup(KeyboardFactory.exitKeyboard());
                yield msg;
            }

            case "💰 Прайс-калькулятор" -> {
                logger.debug("User [{}] selected: Прайс-калькулятор", chatId);

                yield new SendMessage(chatId,
                        "Введіть тип зйомки та тривалість (наприклад: 'весілля 3 години') 💵");
            }

            case "📷 Підказки по Обладнанню" -> {
                logger.debug("User [{}] entered GEAR_CHAT_MODE", chatId);

                userStateService.setUserState(chatId, "GEAR_CHAT_MODE");
                SendMessage msg = new SendMessage(chatId,
                        "💬 Ви увійшли в режим консультації по техніці. " +
                                "Напишіть тип зйомки (наприклад: весілля, портрет, зйомка в студії і т.д.).\n" +
                                "А я підкажу, яке фотообладнання вам найкраще підійде (камера, об’єктив, освітлення, фон).");
                msg.setReplyMarkup(KeyboardFactory.exitKeyboard());
                yield msg;
            }

            case "↩ Вийти з режиму" -> {
                logger.debug("User [{}] exited GEAR_CHAT_MODE", chatId);

                userStateService.clearUserState(chatId);
                gearChatMemoryService.clearMemory(chatId);

                SendMessage msg = new SendMessage(chatId, "✅ Ви вийшли з режиму консультації.");
                msg.setReplyMarkup(KeyboardFactory.mainKeyboard());
                yield msg;
            }

            default -> {
                String state = userStateService.getUserState(chatId);
                logger.debug("User [{}] in state: {}", chatId, state);

                for (UserStateHandler handler : stateHandlers) {
                    if (handler.supports(state)) {
                        logger.debug("Delegating to handler: {}", handler.getClass().getSimpleName());
                        yield handler.handle(chatId, messageText);
                    }
                }

                logger.warn("User [{}] sent unrecognized message outside any handler: {}", chatId, messageText);

                SendMessage msg = new SendMessage(chatId,
                        "🤖 Я поки що розумію тільки команди або натиснення кнопок.");
                msg.setReplyMarkup(KeyboardFactory.mainKeyboard());
                yield msg;
            }
        };
    }

    public SendMessage handlePhotoMessage(String chatId, Message message) {
        logger.info("Received photo from [{}]", chatId);

        String state = userStateService.getUserState(chatId);
        logger.debug("Current user state for [{}]: {}", chatId, state);

        for (UserStateHandler handler : stateHandlers) {
            if (handler.supports(state) && handler instanceof PhotoInputHandler photoHandler) {
                logger.debug("Delegating photo to handler: {}", handler.getClass().getSimpleName());
                return photoHandler.handlePhoto(chatId, message);
            }
        }

        logger.warn("Received photo from [{}] but no handler matched.", chatId);
        SendMessage msg = new SendMessage(chatId,
                "📷 Надіслане фото не оброблено. Будь ласка, оберіть режим у меню.");
        msg.setReplyMarkup(KeyboardFactory.mainKeyboard());
        return msg;
    }
}
