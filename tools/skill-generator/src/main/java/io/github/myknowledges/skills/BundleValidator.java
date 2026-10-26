package io.github.myknowledges.skills;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class BundleValidator {

    void validate(Path bundle) throws IOException {
        if (!Files.isDirectory(bundle) || Files.isSymbolicLink(bundle)) {
            throw new IllegalArgumentException("Generate skills before validation: " + bundle);
        }
        if (!Files.isRegularFile(bundle.resolve("manifest.json"))) {
            throw new IllegalArgumentException("Missing manifest.json in " + bundle);
        }
        SelectionConfig config = SelectionConfig.builder().ignore(List.of()).build();
        MarkdownSupport markdown = new MarkdownSupport();
        try (var files = Files.walk(bundle)) {
            for (Path file : files.sorted().toList()) {
                if (Files.isSymbolicLink(file)) {
                    throw new IllegalArgumentException("Symbolic link in generated bundle: " + file);
                }
                if (!file.getFileName().toString().equals("SKILL.md") ||
                        !file.getParent().getParent().equals(bundle)) {
                    continue;
                }
                validateLinks(bundle, file, markdown, config);
            }
        }
    }


    private void validateLinks(Path bundle, Path file, MarkdownSupport markdown,
            SelectionConfig config) throws IOException {

        for (LinkDestination link : markdown.destinations(markdown.parse(Files.readString(file)))) {
            var local = FileSupport.localAddress(FileSupport.relative(bundle, file), link);
            if (local.isPresent()) {
                FileSupport.resolveTarget(bundle, file, local.get(), link, config);
            }
        }
    }
}
