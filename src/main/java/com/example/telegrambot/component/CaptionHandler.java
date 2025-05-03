package com.example.telegrambot.component;

import com.example.telegrambot.interfaces.PhotoInputHandler;
import com.example.telegrambot.keyboard.KeyboardFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Objects;

@Component
public class CaptionHandler implements PhotoInputHandler {

    private static final Logger logger = LoggerFactory.getLogger(CaptionHandler.class);

    private final ChatClient chatClient;

    public CaptionHandler(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public boolean supports(String state) {
        return "CAPTION_MODE".equals(state);
    }

    @Override
    public SendMessage handle(String chatId, String messageText) {
        String prompt = "Згенеруй українською короткий, креативний опис до фотосесії і додай релевантні хештеги. Опис: " + messageText;

        String aiReply = Objects.requireNonNull(chatClient.prompt()
                .user(prompt)
                .call()
                .content());

        SendMessage response = new SendMessage(chatId, aiReply);
        response.setReplyMarkup(KeyboardFactory.exitKeyboard());
        return response;
    }

    @Override
    public SendMessage handlePhoto(String chatId, Message message) {
        logger.info("Handling photo input from [{}]", chatId);

        String prompt = "Це фото з фотосесії. Згенеруй короткий опис до фото українською мовою і додай релевантні хештеги." +
                "Опис повинний бути сформований на основі того що зображено на фотографії таким чином, " +
                "щоб користувачам соціальних мереж було цікаво прочитати це.";

        String aiReply = Objects.requireNonNull(chatClient.prompt()
                .user(prompt)
                .call()
                .content());

        SendMessage response = new SendMessage(chatId, aiReply);
        response.setReplyMarkup(KeyboardFactory.exitKeyboard());
        return response;
    }
}


