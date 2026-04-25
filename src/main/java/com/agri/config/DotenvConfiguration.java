package com.agri.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * Loads environment variables from .env file into System properties
 * This allows Spring @Value annotations to access them
 */
@Configuration
public class DotenvConfiguration {

    @PostConstruct
    public void loadDotenv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(System.getProperty("user.dir"))
                    .ignoreIfMissing()
                    .load();

            
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = GeminiApiKeyResolver.sanitize(entry.getValue());
                if (!key.isEmpty() && !value.isEmpty()) {
                    System.setProperty(key, value);
                }
            });

            String resolvedKey = GeminiApiKeyResolver.resolve("");
            if (GeminiApiKeyResolver.isUsable(resolvedKey)) {
                System.setProperty("GEMINI_API_KEY", resolvedKey);
                System.out.println("[INFO] Gemini key loaded successfully: "
                        + resolvedKey.substring(0, Math.min(10, resolvedKey.length())) + "...");
            } else {
                System.err.println("[ERROR] No valid Gemini key found (.env/env/system). Expected GEMINI_API_KEY, GOOGLE_API_KEY, or API_KEY.");
            }
        } catch (Exception e) {
            System.err.println("[WARN] Failed to load .env file: " + e.getMessage());
        }
    }
}
