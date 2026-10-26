package io.github.myknowledges.skills;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;

/**
 * Резервный источник описания из первого H1 или первой строки текста вне кода.
 */
public final class HeadingDescriptionProvider implements DescriptionProvider {


    private final DescriptionNormalizer normalizer = new DescriptionNormalizer();


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
        String normalized = normalizer.normalize(text);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("EMPTY_DESCRIPTION: first H1 has no text");
        }
        return normalized;
    }
}
