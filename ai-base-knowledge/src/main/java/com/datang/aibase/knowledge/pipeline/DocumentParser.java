package com.datang.aibase.knowledge.pipeline;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
public class DocumentParser {

    private static final Logger log = LoggerFactory.getLogger(DocumentParser.class);
    private static final Tika tika = new Tika();
    private static final int MAX_CONTENT_LENGTH = 5_000_000;

    public record ParsedDocument(String title, String content, String mimeType) {}

    public ParsedDocument parse(byte[] bytes, String fileName) {
        String mimeType = tika.detect(bytes, fileName);
        String title = fileName != null ? fileName : "untitled";
        String content;

        try (InputStream in = new ByteArrayInputStream(bytes)) {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(MAX_CONTENT_LENGTH);
            Metadata meta = new Metadata();
            if (fileName != null) {
                meta.set("resourceName", fileName);
            }
            parser.parse(in, handler, meta);
            content = handler.toString();

            String metaTitle = meta.get("title");
            if (metaTitle != null && !metaTitle.isBlank()) {
                title = metaTitle;
            } else if (fileName != null) {
                int dot = fileName.lastIndexOf('.');
                title = dot > 0 ? fileName.substring(0, dot) : fileName;
            }

            log.info("Parsed document: {} ({} bytes, type={})", title, content.length(), mimeType);
        } catch (Exception e) {
            log.error("Failed to parse document: {}", fileName, e);
            try {
                content = tika.parseToString(new ByteArrayInputStream(bytes));
            } catch (Exception ex) {
                throw new RuntimeException("Cannot parse document: " + fileName, ex);
            }
        }

        return new ParsedDocument(title, content, mimeType);
    }
}
