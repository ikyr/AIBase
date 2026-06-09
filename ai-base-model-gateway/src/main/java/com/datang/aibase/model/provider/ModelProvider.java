package com.datang.aibase.model.provider;

import java.util.Map;

public interface ModelProvider {

    String getProviderName();

    Map<String, Object> chat(String endpoint, String apiKey, String model, Map<String, Object> request);

    Map<String, Object> embed(String endpoint, String apiKey, String model, String text);
}
