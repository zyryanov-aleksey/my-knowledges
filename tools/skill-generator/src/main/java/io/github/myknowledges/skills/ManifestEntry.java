package io.github.myknowledges.skills;

import lombok.Builder;

@Builder
record ManifestEntry(String source, String name, String description, String sha256) {
}
