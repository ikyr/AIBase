package com.datang.aibase.knowledge.connector;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UploadConnector implements DataSourceConnector {

    @Override
    public String getName() {
        return "upload";
    }

    @Override
    public String getType() {
        return "UPLOAD";
    }

    @Override
    public boolean testConnection(Map<String, String> config) {
        return true;
    }

    @Override
    public void sync(String kbId, Map<String, String> config, SyncCallback callback) {
        // Direct upload — content is provided inline, no sync needed
    }
}
