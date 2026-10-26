package io.github.myknowledges.skills;

final class DescriptionNormalizer {

    String normalize(String text) {
        String normalized = text.replaceAll("\\s+", " ").strip();
        int length = normalized.codePointCount(0, normalized.length());
        return normalized.substring(0, normalized.offsetByCodePoints(0, Math.min(1024, length)));
    }

}
