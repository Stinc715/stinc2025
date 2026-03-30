package com.clubportal.service;

import java.util.regex.Pattern;

public class ChatLanguageDetector {

    private static final Pattern HAN_PATTERN = Pattern.compile("[\\p{IsHan}]");

    public ChatLanguage detect(String message) {
        String text = message == null ? "" : message.trim();
        if (text.isEmpty()) {
            return ChatLanguage.EN;
        }
        return HAN_PATTERN.matcher(text).find() ? ChatLanguage.ZH : ChatLanguage.EN;
    }
}
