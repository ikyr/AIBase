package com.datang.aibase.common.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataMaskerTest {

    private final SensitiveDataMasker masker = new SensitiveDataMasker();

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("mask returns input as-is for null or blank")
    void mask_nullOrBlank_returnsInput(String input) {
        assertThat(masker.mask(input)).isEqualTo(input);
    }

    @Test
    @DisplayName("mask redacts Chinese mobile phone numbers")
    void mask_phoneNumber_redacted() {
        String result = masker.mask("请联系 13812345678 获取详情");

        assertThat(result).doesNotContain("13812345678");
        assertThat(result).contains("138****5678");
    }

    @Test
    @DisplayName("mask redacts Chinese ID card numbers")
    void mask_idCard_redacted() {
        String result = masker.mask("身份证号 110101200501011234 请核实");

        assertThat(result).doesNotContain("200501011234");
        assertThat(result).contains("1101**********1234");
    }

    @Test
    @DisplayName("mask redacts API keys")
    void mask_apiKey_redacted() {
        String result = masker.mask("Authorization: sk-abcdefghijklmnopqrstuvwxyz123456");

        assertThat(result).contains("[API_KEY_REDACTED]");
        assertThat(result).doesNotContain("sk-abcdef");
    }

    @Test
    @DisplayName("mask handles text without sensitive data unchanged")
    void mask_cleanText_unchanged() {
        String text = "今天天气很好，适合出去散步。";

        assertThat(masker.mask(text)).isEqualTo(text);
    }

    @Test
    @DisplayName("mask handles multiple sensitive items in one text")
    void mask_multipleItems_allRedacted() {
        String result = masker.mask("电话 13900139000，身份证 320101200501011234");

        assertThat(result).contains("139****9000");
        assertThat(result).contains("3201**********1234");
        assertThat(result).doesNotContain("13900139000");
        assertThat(result).doesNotContain("200501011234");
    }
}
