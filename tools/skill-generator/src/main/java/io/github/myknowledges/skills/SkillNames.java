package io.github.myknowledges.skills;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;

final class SkillNames {

    static String fromPath(String relativePath) {
        String stem = relativePath.substring(0, relativePath.length() - 3);
        String prefix = Normalizer.normalize(stem, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        prefix = prefix.substring(0, Math.min(51, prefix.length())).replaceAll("-$", "");
        if (prefix.isEmpty()) {
            prefix = "skill";
        }
        return prefix + "-" + Hashes.sha256(relativePath.getBytes(StandardCharsets.UTF_8))
                .substring(0, 12);
    }
}
