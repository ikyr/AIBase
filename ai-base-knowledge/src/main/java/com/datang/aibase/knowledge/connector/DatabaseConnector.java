package com.datang.aibase.knowledge.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.Map;

@Component
public class DatabaseConnector implements DataSourceConnector {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnector.class);

    @Override
    public String getName() {
        return "database";
    }

    @Override
    public String getType() {
        return "DATABASE";
    }

    @Override
    public boolean testConnection(Map<String, String> config) {
        String url = config.get("url");
        String user = config.get("user");
        String password = config.get("password");
        if (url == null) return false;
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            return conn.isValid(5);
        } catch (SQLException e) {
            log.warn("Database connection test failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void sync(String kbId, Map<String, String> config, SyncCallback callback) {
        String url = config.get("url");
        String user = config.get("user");
        String password = config.get("password");
        String query = config.get("query");
        String titleColumn = config.getOrDefault("title_column", "title");
        String contentColumn = config.getOrDefault("content_column", "content");

        if (url == null) {
            callback.onError("Database URL not configured");
            return;
        }
        if (query == null) {
            callback.onError("Query not configured");
            return;
        }

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData meta = rs.getMetaData();
            boolean hasTitle = hasColumn(meta, titleColumn);
            boolean hasContent = hasColumn(meta, contentColumn);

            if (!hasContent) {
                callback.onError("Content column '" + contentColumn + "' not found in result set");
                return;
            }

            int count = 0;
            while (rs.next()) {
                String title = hasTitle ? rs.getString(titleColumn) : "Row " + (count + 1);
                String content = rs.getString(contentColumn);
                if (content != null && !content.isBlank()) {
                    callback.onDocument(title, content, "text");
                    count++;
                }
            }
            log.info("Database connector synced {} rows for kb {}", count, kbId);
        } catch (SQLException e) {
            callback.onError("Database query failed: " + e.getMessage());
        }
    }

    private boolean hasColumn(ResultSetMetaData meta, String name) throws SQLException {
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (meta.getColumnName(i).equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
