package com.datang.aibase.knowledge.connector;

import java.util.Map;

public interface DataSourceConnector {

    String getName();

    String getType();

    boolean testConnection(Map<String, String> config);

    void sync(String kbId, Map<String, String> config, SyncCallback callback);

    interface SyncCallback {
        void onDocument(String title, String content, String fileType);
        void onError(String message);
    }
}
