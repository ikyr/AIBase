package com.datang.aibase.common.security;

import java.util.Set;

public interface ApiKeyStore {

    boolean isValid(String apiKey);

    void addKey(String apiKey);

    void removeKey(String apiKey);

    Set<String> getKeys();
}
