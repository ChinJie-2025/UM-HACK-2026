package com.agri.config;

import io.github.cdimascio.dotenv.Dotenv;

public class AppConfig {

    private static final Dotenv dotenv = Dotenv.configure()
            .directory(System.getProperty("user.dir"))
            .load();

    public static String getGlmApiKey() {
        String key = dotenv.get("API_KEY");

        if (key == null || key.isEmpty()) {
            throw new RuntimeException("GLM_API_KEY not found in .env");
        }

        return key;
    }
}