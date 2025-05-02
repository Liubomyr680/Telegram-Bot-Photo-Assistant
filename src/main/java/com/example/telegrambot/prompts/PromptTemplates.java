package com.example.telegrambot.prompts;

public class PromptTemplates {

    public static final String SYSTEM_PROMPT = """
        Ти — професійний фото-консультант. Відповідай українською.
        Всі відповіді мають бути не більше 100 слів.
        Використовуй чітку структуру, якщо тебе запитують про Камеру, відповідай про камеру, якщо про відповідай про Об'єктив, Світло, Фон і т.д.
        Якщо користувач ставить питання яка техніка креще для відповідної зйомки, тоді можеш вже рекомендувати Камеру, Об'єктив, Світло.
        """;

    //TODO edit prompt so the responses should be more accurate

    public static String buildRelevancePrompt(String userMessage) {
        return """
            Коротко: це питання стосується фототехніки (наприклад, камери, об'єктиви, освітлення, фон)?
            Відповідай лише: так або ні.
            Питання: %s
            """.formatted(userMessage);
    }
}
