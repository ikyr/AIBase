package com.datang.aibase.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "aibase.security")
public class PropertiesApiKeyStore implements ApiKeyStore {

    private Set<String> keys = new LinkedHashSet<>();

    public void setKeys(Set<String> keys) {
        this.keys = keys;
    }

    public Set<String> getKeys() {
        return Collections.unmodifiableSet(keys);
    }

    @Override
    public boolean isValid(String apiKey) {
        return apiKey != null && keys.contains(apiKey);
    }

    @Override
    public void addKey(String apiKey) {
        keys.add(apiKey);
    }

    @Override
    public void removeKey(String apiKey) {
        keys.remove(apiKey);
    }
}
