package com.example.telegrambot.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

public class KeyboardFactory {

    public static ReplyKeyboardMarkup mainKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add("📸 Редагування Фото (функція недостпна)");
        row1.add("🎯 Ідеї для фотосесії (функція недостпна)");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("🧠 AI-аналіз Фото");
        row2.add("🏷️ Хештеги та Опис");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("📷 Підказки по Обладнанню");
        row3.add("💰 Прайс-калькулятор (функція недостпна)");

        List<KeyboardRow> rows = List.of(row1, row2, row3);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(rows);
        markup.setResizeKeyboard(true);
        return markup;
    }

    public static ReplyKeyboardMarkup exitKeyboard() {
        KeyboardRow row = new KeyboardRow();
        row.add("↩ Вийти з режиму");

        List<KeyboardRow> rows = List.of(row);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(rows);
        markup.setResizeKeyboard(true);
        return markup;
    }
}
