package io.github.myknowledges.skills;

import java.util.ArrayList;
import java.util.List;

import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

final class MarkdownSupport {


    private final Parser parser = Parser.builder()
            .extensions(List.of(YamlFrontMatterExtension.create())).build();


    Node parse(String source) {
        return parser.parse(source);
    }

    List<LinkDestination> destinations(Node document) {
        List<LinkDestination> destinations = new ArrayList<>();
        for (Node node : document.getDescendants()) {
            BasedSequence url = switch (node) {
                case Link link -> link.getUrl();
                case Image image -> image.getUrl();
                case Reference reference -> reference.getUrl();
                default -> BasedSequence.NULL;
            };
            if (url.isNotNull() && !url.isEmpty()) {
                destinations.add(LinkDestination.builder()
                        .start(url.getStartOffset()).end(url.getEndOffset())
                        .line(url.baseLineColumnAtStart().getFirst() + 1)
                        .address(url.toString()).build());
            }
        }
        return destinations;
    }
}
