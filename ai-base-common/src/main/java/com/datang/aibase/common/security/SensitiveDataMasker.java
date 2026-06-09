package com.datang.aibase.common.security;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class SensitiveDataMasker {

    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("\\d{17}[\\dXx]");
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(sk-|api[_-]?key[=:]\\s*)[A-Za-z0-9_\\-]{20,}");

    public String mask(String text) {
        if (text == null || text.isBlank()) return text;
        String result = PHONE_PATTERN.matcher(text).replaceAll(m -> maskPhone(m.group()));
        result = ID_CARD_PATTERN.matcher(result).replaceAll(m -> maskIdCard(m.group()));
        result = API_KEY_PATTERN.matcher(result).replaceAll("[API_KEY_REDACTED]");
        return result;
    }

    private String maskPhone(String phone) {
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private String maskIdCard(String idCard) {
        return idCard.substring(0, 4) + "**********" + idCard.substring(14);
    }
}
