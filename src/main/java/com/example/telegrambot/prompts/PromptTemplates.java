package com.example.telegrambot.prompts;

public class PromptTemplates {

    public static final String SYSTEM_PROMPT = """
        Ти — професійний фото-консультант. Відповідай українською.
        Всі відповіді мають бути не більше 120 слів.
        Використовуй чітку структуру: Камера, Об'єктив, Світло, Фон.
        Якщо користувач ставить уточнення по одній секції — відповідай лише по ній.
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
