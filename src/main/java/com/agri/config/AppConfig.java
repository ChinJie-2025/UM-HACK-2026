package com.agri.config;

import io.github.cdimascio.dotenv.Dotenv;

public class AppConfig {

    private static final Dotenv dotenv = Dotenv.configure()
            .directory(System.getProperty("user.dir"))
            .ignoreIfMissing()
            .load();

    public static String getGlmApiKey() {
        String key = GeminiApiKeyResolver.resolve(dotenv.get("GEMINI_API_KEY"));

        if (!GeminiApiKeyResolver.isUsable(key)) {
            key = GeminiApiKeyResolver.resolve(dotenv.get("GOOGLE_API_KEY"));
        }

        if (!GeminiApiKeyResolver.isUsable(key)) {
            key = GeminiApiKeyResolver.resolve(dotenv.get("API_KEY"));
        }

        if (!GeminiApiKeyResolver.isUsable(key)) {
            throw new RuntimeException("No valid Gemini key found. Set GEMINI_API_KEY (or GOOGLE_API_KEY/API_KEY) in .env or environment.");
        }

        return key;
    }
}