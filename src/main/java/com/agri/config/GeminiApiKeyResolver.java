package com.agri.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;

/**
 * Centralized Gemini API key resolution for all AI integrations.
 */
public final class GeminiApiKeyResolver {

    private static final List<String> KEY_NAMES = List.of("GEMINI_API_KEY", "GOOGLE_API_KEY", "API_KEY");

    private GeminiApiKeyResolver() {
    }

    public static String resolve(String preferredValue) {
        String resolved = sanitize(preferredValue);
        if (isUsable(resolved)) {
            return resolved;
        }

        for (String keyName : KEY_NAMES) {
            resolved = sanitize(System.getProperty(keyName));
            if (isUsable(resolved)) {
                return resolved;
            }
        }
        

        for (String keyName : KEY_NAMES) {
            resolved = sanitize(System.getenv(keyName));
            if (isUsable(resolved)) {
                return resolved;
            }
        }

                resolved = resolveFromDotenv();
        if (isUsable(resolved)) {
            return resolved;
        }

        return "";
    }

    private static String resolveFromDotenv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(System.getProperty("user.dir"))
                    .ignoreIfMissing()
                    .load();

            for (String keyName : KEY_NAMES) {
                String value = sanitize(dotenv.get(keyName));
                if (isUsable(value)) {
                    return value;
                }
            }
        } catch (Exception ignored) {
            // Ignore errors here; callers handle missing key via fallback message.
        }

        return "";
    }

    public static boolean isUsable(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String normalized = value.trim().toLowerCase();
        return !normalized.startsWith("mock")
                && !normalized.contains("your_")
                && !normalized.contains("replace_me")
                && !normalized.contains("placeholder")
                && !normalized.contains("not_set");
    }

    public static String sanitize(String value) {
        if (value == null) {
            return "";
        }

        String sanitized = value.trim();

        if ((sanitized.startsWith("\"") && sanitized.endsWith("\""))
                || (sanitized.startsWith("'") && sanitized.endsWith("'"))) {
            sanitized = sanitized.substring(1, sanitized.length() - 1).trim();
        }

        return sanitized;
    }
}
