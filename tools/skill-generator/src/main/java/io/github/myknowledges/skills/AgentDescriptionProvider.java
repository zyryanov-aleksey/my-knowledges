package io.github.myknowledges.skills;

import java.util.Optional;

import com.vladsch.flexmark.util.ast.Node;
import lombok.RequiredArgsConstructor;

/**
 * Получает описание из agent-desc карточки или использует резервный источник.
 */
@RequiredArgsConstructor
public final class AgentDescriptionProvider implements DescriptionProvider {


    private final DescriptionProvider fallback;

    private final KnowledgeCardFrontMatter frontMatter = new KnowledgeCardFrontMatter();

    private final DescriptionNormalizer normalizer = new DescriptionNormalizer();


    @Override
    public String describe(Node document) {
        Optional<String> description = frontMatter.agentDescription(document)
                .map(normalizer::normalize).filter(value -> !value.isEmpty());
        return description.orElseGet(() -> fallback.describe(document));
    }

}
