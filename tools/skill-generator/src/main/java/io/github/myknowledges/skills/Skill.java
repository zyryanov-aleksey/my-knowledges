package io.github.myknowledges.skills;

import java.util.List;

import lombok.Builder;

@Builder
record Skill(String source, String name, String description, String sha256,
        String body, List<LinkDestination> links) {

    ManifestEntry manifestEntry() {
        return ManifestEntry.builder().source(source).name(name)
                .description(description).sha256(sha256).build();
    }
}
