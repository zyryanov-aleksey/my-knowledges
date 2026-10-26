package io.github.myknowledges.skills;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Builder;

@Builder
record SelectionConfig(List<String> ignore) {

    static SelectionConfig load(Path path) throws IOException {
        if (Files.isSymbolicLink(path)) {
            throw new IllegalArgumentException("Configuration must not be a symbolic link");
        }
        JsonNode tree = new ObjectMapper(new YAMLFactory()).readTree(path.toFile());
        if (tree == null || !tree.isObject() || tree.size() != 1 ||
                !tree.has("ignore") || !tree.get("ignore").isArray()) {
            throw new IllegalArgumentException("Configuration must contain only an ignore list");
        }
        List<String> rules = new ArrayList<>();
        for (JsonNode item : tree.get("ignore")) {
            String rule = item.asText();
            if (!item.isTextual() || rule.isBlank() || rule.startsWith("/") ||
                    rule.contains("\\") || rule.contains(":") ||
                    !Path.of(rule).normalize().toString().replace('\\', '/').equals(
                            rule.replaceAll("/$", "")) || rule.equals("..") ||
                    rule.startsWith("../") || rule.equals(".") || rule.equals("./")) {
                throw new IllegalArgumentException("Invalid repository-relative ignore path: " + rule);
            }
            rules.add(rule);
        }
        return SelectionConfig.builder().ignore(List.copyOf(rules)).build();
    }

    boolean ignores(String path) {
        return ignore.stream().anyMatch(rule -> rule.endsWith("/") ?
                path.startsWith(rule) || path.equals(rule.substring(0, rule.length() - 1)) :
                path.equals(rule));
    }

    boolean ignoresDirectory(String path) {
        return ignores(path + "/");
    }
}
