package com.datang.aibase.knowledge.connector;

import com.datang.aibase.knowledge.pipeline.DocumentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

@Component
public class FileSystemConnector implements DataSourceConnector {

    private static final Logger log = LoggerFactory.getLogger(FileSystemConnector.class);
    private final DocumentParser documentParser;

    public FileSystemConnector(DocumentParser documentParser) {
        this.documentParser = documentParser;
    }

    @Override
    public String getName() {
        return "filesystem";
    }

    @Override
    public String getType() {
        return "LOCAL_FILE";
    }

    @Override
    public boolean testConnection(Map<String, String> config) {
        String basePath = config.get("base_path");
        if (basePath == null) return false;
        return Files.isDirectory(Path.of(basePath));
    }

    @Override
    public void sync(String kbId, Map<String, String> config, SyncCallback callback) {
        String basePath = config.get("base_path");
        if (basePath == null) {
            callback.onError("base_path not configured");
            return;
        }
        Path dir = Path.of(basePath);
        if (!Files.isDirectory(dir)) {
            callback.onError("Directory not found: " + basePath);
            return;
        }
        try (var stream = Files.list(dir)) {
            stream.filter(Files::isRegularFile).forEach(file -> {
                try {
                    String fileName = file.getFileName().toString();
                    byte[] bytes = Files.readAllBytes(file);
                    var doc = documentParser.parse(bytes, fileName);
                    String fileType = doc.mimeType().contains("/") ? doc.mimeType().split("/")[1] : "txt";
                    callback.onDocument(doc.title(), doc.content(), fileType);
                } catch (Exception e) {
                    log.warn("Failed to read file {}: {}", file, e.getMessage());
                }
            });
        } catch (IOException e) {
            callback.onError("Failed to list directory: " + e.getMessage());
        }
    }
}
