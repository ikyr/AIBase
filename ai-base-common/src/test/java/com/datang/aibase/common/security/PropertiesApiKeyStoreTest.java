package com.datang.aibase.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertiesApiKeyStoreTest {

    private PropertiesApiKeyStore store;

    @BeforeEach
    void setUp() {
        store = new PropertiesApiKeyStore();
    }

    @Test
    @DisplayName("isValid returns true for existing key")
    void isValid_existingKey_returnsTrue() {
        store.setKeys(java.util.Set.of("key-1", "key-2"));

        assertThat(store.isValid("key-1")).isTrue();
    }

    @Test
    @DisplayName("isValid returns false for unknown key")
    void isValid_unknownKey_returnsFalse() {
        store.setKeys(java.util.Set.of("key-1"));

        assertThat(store.isValid("unknown")).isFalse();
    }

    @Test
    @DisplayName("isValid returns false for null key")
    void isValid_nullKey_returnsFalse() {
        store.setKeys(java.util.Set.of("key-1"));

        assertThat(store.isValid(null)).isFalse();
    }

    @Test
    @DisplayName("addKey makes key valid")
    void addKey_newKey_becomesValid() {
        store.addKey("new-key");

        assertThat(store.isValid("new-key")).isTrue();
    }

    @Test
    @DisplayName("removeKey makes key invalid")
    void removeKey_existingKey_becomesInvalid() {
        store.setKeys(new java.util.LinkedHashSet<>(java.util.Set.of("key-1", "key-2")));
        store.removeKey("key-1");

        assertThat(store.isValid("key-1")).isFalse();
        assertThat(store.isValid("key-2")).isTrue();
    }

    @Test
    @DisplayName("getKeys returns unmodifiable set")
    void getKeys_returnsUnmodifiableSet() {
        store.setKeys(java.util.Set.of("key-1"));

        var keys = store.getKeys();
        assertThat(keys).contains("key-1");
        assertThatThrownBy(() -> keys.add("new")).isInstanceOf(UnsupportedOperationException.class);
    }
}
