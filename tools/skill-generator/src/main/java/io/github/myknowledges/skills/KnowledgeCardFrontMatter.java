package io.github.myknowledges.skills;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
import com.vladsch.flexmark.util.ast.Node;

final class KnowledgeCardFrontMatter {


    private static final Pattern OPENING_DELIMITER = Pattern.compile(
            "\\A---[ \\t]*(?:\\r\\n|\\r|\\n)");

    private static final Pattern CLOSING_DELIMITER = Pattern.compile("(?:---|\\.\\.\\.)[ \\t]*");

    private final ObjectMapper yaml = new ObjectMapper(YAMLFactory.builder()
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build());


    Optional<String> agentDescription(Node document) {
        Optional<YamlFrontMatterBlock> block = block(document);
        if (block.isEmpty()) {
            return Optional.empty();
        }
        String text = block.get().getChars().toString();
        Matcher opening = OPENING_DELIMITER.matcher(text);
        if (!opening.find()) {
            throw new IllegalArgumentException("INVALID_FRONTMATTER: missing opening delimiter");
        }
        int closingStart = closingStart(block.get());
        JsonNode metadata;
        try {
            metadata = yaml.readTree(text.substring(opening.end(), closingStart));
        } catch (IOException exception) {
            throw new IllegalArgumentException("INVALID_FRONTMATTER: " + exception.getMessage(),
                    exception);
        }
        if (metadata == null || metadata.isNull()) {
            return Optional.empty();
        }
        if (!metadata.isObject()) {
            throw new IllegalArgumentException("INVALID_FRONTMATTER: expected a YAML mapping");
        }
        JsonNode description = metadata.get("agent-desc");
        if (description == null || description.isNull()) {
            return Optional.empty();
        }
        if (!description.isTextual()) {
            throw new IllegalArgumentException("INVALID_AGENT_DESC: expected a string or null");
        }
        return Optional.of(description.textValue());
    }

    int endOffset(Node document) {
        Optional<YamlFrontMatterBlock> block = block(document);
        if (block.isEmpty()) {
            return 0;
        }
        closingStart(block.get());
        int end = block.get().getEndOffset();
        CharSequence source = document.getChars();
        if (end < source.length() && source.charAt(end) == '\r') {
            end++;
        }
        if (end < source.length() && source.charAt(end) == '\n') {
            end++;
        }
        return end;
    }

    private Optional<YamlFrontMatterBlock> block(Node document) {
        if (document.getFirstChild() instanceof YamlFrontMatterBlock block &&
                block.getStartOffset() == 0) {
            return Optional.of(block);
        }
        return Optional.empty();
    }

    private int closingStart(YamlFrontMatterBlock block) {
        String text = block.getChars().toString();
        int start = Math.max(text.lastIndexOf('\n'), text.lastIndexOf('\r')) + 1;
        if (start == 0 || !CLOSING_DELIMITER.matcher(text.substring(start)).matches()) {
            throw new IllegalArgumentException("INVALID_FRONTMATTER: missing closing delimiter");
        }
        return start;
    }

}
