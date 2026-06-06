package dev.danvega.blog_agent;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonUtils() {}

    // Extracts the first top-level JSON object from raw text, ignoring braces inside quoted strings.
    public static Optional<String> extractTopLevelJson(String raw) {
        if (raw == null) return Optional.empty();
        int start = raw.indexOf('{');
        if (start < 0) return Optional.empty();

        boolean inString = false;
        boolean escape = false;
        int depth = 0;
        for (int i = start; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '"' ) {
                inString = !inString;
                continue;
            }
            if (!inString) {
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        String candidate = raw.substring(start, i + 1);
                        return Optional.of(candidate);
                    }
                }
            }
        }
        return Optional.empty();
    }

    // Attempts to parse a DraftPost by first extracting a top-level JSON object then deserializing.
    public static Optional<DraftPost> parseDraftPost(String raw) {
        Optional<String> jsonOpt = extractTopLevelJson(raw);
        if (jsonOpt.isEmpty()) {
            log.warn("No top-level JSON object found in LLM output.");
            return Optional.empty();
        }
        String json = jsonOpt.get();
        try {
            DraftPost draft = MAPPER.readValue(json, DraftPost.class);
            return Optional.of(draft);
        } catch (Exception e) {
            log.warn("Failed to parse extracted JSON into DraftPost: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // Example usage:
    // Optional<DraftPost> dp = JsonUtils.parseDraftPost(llmRawOutput);
}