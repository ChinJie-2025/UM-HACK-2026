package com.agri.Controller;

import com.agri.config.GeminiApiKeyResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HealthController {

    /**
     * Health check endpoint - verify API key is loaded
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
            String apiKey = GeminiApiKeyResolver.resolve("");
        boolean isApiKeyLoaded = GeminiApiKeyResolver.isUsable(apiKey);
        
        response.put("status", "ok");
        response.put("service", "Agriwise Backend");
        response.put("timestamp", System.currentTimeMillis());
        response.put("apiKeyLoaded", isApiKeyLoaded);
        
        if (isApiKeyLoaded) {
            response.put("message", "✅ Gemini API is properly configured and ready");
            response.put("apiKeyPreview", apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
        } else {
            response.put("message", "❌ ERROR: No valid Gemini key found. Set GEMINI_API_KEY, GOOGLE_API_KEY, or API_KEY.");
            response.put("apiKeyStatus", "NOT CONFIGURED");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * System status endpoint - check environment variables
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        
        String fromGeminiProperty = GeminiApiKeyResolver.sanitize(System.getProperty("GEMINI_API_KEY"));
        String fromGoogleProperty = GeminiApiKeyResolver.sanitize(System.getProperty("GOOGLE_API_KEY"));
        String fromLegacyProperty = GeminiApiKeyResolver.sanitize(System.getProperty("API_KEY"));

        String fromGeminiEnv = GeminiApiKeyResolver.sanitize(System.getenv("GEMINI_API_KEY"));
        String fromGoogleEnv = GeminiApiKeyResolver.sanitize(System.getenv("GOOGLE_API_KEY"));
        String fromLegacyEnv = GeminiApiKeyResolver.sanitize(System.getenv("API_KEY"));

        String resolved = GeminiApiKeyResolver.resolve("");
        
        response.put("timestamp", System.currentTimeMillis());
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("userDir", System.getProperty("user.dir"));
                response.put("geminiApiKeyFromProperty", GeminiApiKeyResolver.isUsable(fromGeminiProperty) ? "SET" : "NOT SET");
        response.put("googleApiKeyFromProperty", GeminiApiKeyResolver.isUsable(fromGoogleProperty) ? "SET" : "NOT SET");
        response.put("apiKeyFromProperty", GeminiApiKeyResolver.isUsable(fromLegacyProperty) ? "SET" : "NOT SET");

        response.put("geminiApiKeyFromEnvironment", GeminiApiKeyResolver.isUsable(fromGeminiEnv) ? "SET" : "NOT SET");
        response.put("googleApiKeyFromEnvironment", GeminiApiKeyResolver.isUsable(fromGoogleEnv) ? "SET" : "NOT SET");
        response.put("apiKeyFromEnvironment", GeminiApiKeyResolver.isUsable(fromLegacyEnv) ? "SET" : "NOT SET");

        response.put("resolvedGeminiKey", GeminiApiKeyResolver.isUsable(resolved) ? "SET" : "NOT SET");
        
        return ResponseEntity.ok(response);
    }
}
