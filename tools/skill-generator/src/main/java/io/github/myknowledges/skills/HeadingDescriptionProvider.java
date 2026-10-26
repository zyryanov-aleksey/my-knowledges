package io.github.myknowledges.skills;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;

public final class HeadingDescriptionProvider implements DescriptionProvider {

    @Override
    public String describe(Node document) {
        for (Node node : document.getDescendants()) {
            if (node instanceof Heading heading && heading.getLevel() == 1) {
                return limit(plainText(heading));
            }
        }
        for (Node node : document.getDescendants()) {
            if (node instanceof Paragraph || node instanceof Heading) {
                String text = plainText(node).lines().filter(line -> !line.isBlank())
                        .findFirst().orElse("");
                if (!text.isBlank()) {
                    return limit(text);
                }
            }
        }
        throw new IllegalArgumentException("EMPTY_DESCRIPTION: no text outside code blocks");
    }

    private String plainText(Node node) {
        return new TextCollectingVisitor().collectAndGetText(node).strip();
    }

    private String limit(String text) {
        String normalized = text.replaceAll("\\s+", " ").strip();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("EMPTY_DESCRIPTION: first H1 has no text");
        }
        int length = normalized.codePointCount(0, normalized.length());
        return normalized.substring(0, normalized.offsetByCodePoints(0, Math.min(1024, length)));
    }
}
