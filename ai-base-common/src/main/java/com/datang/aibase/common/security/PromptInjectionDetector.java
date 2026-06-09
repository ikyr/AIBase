package com.datang.aibase.common.security;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class PromptInjectionDetector {

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("(?i)ignore\\s+(all\\s+)?(previous|above)\\s+(instructions?|prompts?)"),
            Pattern.compile("(?i)you\\s+are\\s+now\\s+.*\\s+(instead|not)"),
            Pattern.compile("(?i)pretend\\s+(you\\s+are|to\\s+be)"),
            Pattern.compile("(?i)forget\\s+(all\\s+)?(your\\s+)?(training|instructions?|rules?)"),
            Pattern.compile("(?i)system\\s*:\\s*.*new\\s+instructions?"),
            Pattern.compile("(?i)\\{output\\s*=\\s*.*\\}")
    );

    public boolean detectInjection(String input) {
        if (input == null || input.isBlank()) return false;
        return INJECTION_PATTERNS.stream().anyMatch(p -> p.matcher(input).find());
    }

    public String sanitize(String input) {
        if (input == null) return null;
        String sanitized = input;
        for (Pattern pattern : INJECTION_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("[FILTERED]");
        }
        return sanitized;
    }
}
