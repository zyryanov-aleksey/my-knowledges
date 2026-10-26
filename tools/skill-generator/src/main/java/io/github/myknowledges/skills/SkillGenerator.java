package io.github.myknowledges.skills;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.vladsch.flexmark.util.ast.Node;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class SkillGenerator {


    private final DescriptionProvider descriptionProvider;

    private final MarkdownSupport markdown = new MarkdownSupport();

    private final ObjectMapper json = new ObjectMapper();

    private final ObjectMapper yaml = new ObjectMapper(YAMLFactory.builder()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .disable(YAMLGenerator.Feature.SPLIT_LINES).build());


    void generate(Path root) throws IOException {
        FileSupport.checkBuildDirectory(root);
        SelectionConfig config = SelectionConfig.load(root.resolve("skills-config.yaml"));
        Path output = root.resolve("build/skills");
        FileSupport.deleteTree(output);
        FileSupport.deleteTree(root.resolve("build/distributions/skills.zip"));
        FileSupport.deleteTree(root.resolve("build/distributions/skills.zip.sha256"));
        FileSupport.deleteTree(root.resolve("build/skills-report.json"));

        Map<String, String> states = scan(root, config);
        Map<String, Skill> skills = index(root, states);
        List<String> warnings = new ArrayList<>();
        try {
            Files.createDirectories(output);
            for (Skill skill : skills.values()) {
                writeSkill(root, output, config, skill, skills, states);
                if (skill.body().lines().count() >= 500 ||
                        skill.body().codePointCount(0, skill.body().length()) > 20_000) {
                    warnings.add("LARGE_INSTRUCTIONS: " + skill.source() +
                            ": recommended <500 lines / approximately <5000 tokens; text retained");
                }
            }
            json.writerWithDefaultPrettyPrinter().writeValue(output.resolve("manifest.json").toFile(),
                    skills.values().stream().map(Skill::manifestEntry).toList());
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("files", states.entrySet().stream()
                    .map(entry -> Map.of("path", entry.getKey(), "state", entry.getValue())).toList());
            report.put("skillCount", skills.size());
            report.put("warnings", warnings);
            json.writerWithDefaultPrettyPrinter().writeValue(
                    root.resolve("build/skills-report.json").toFile(), report);
            for (String warning : warnings) {
                System.err.println(warning);
            }
            System.out.println("Generated " + skills.size() + " skills in " + output);
        } catch (IOException | RuntimeException exception) {
            FileSupport.deleteTree(output);
            throw exception;
        }
    }

    private Map<String, String> scan(Path root, SelectionConfig config) throws IOException {
        Map<String, String> states = new TreeMap<>();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) {
                String path = FileSupport.relative(root, directory);
                if (!directory.equals(root) && config.ignoresDirectory(path)) {
                    states.put(path + "/", "ignored");
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                String path = FileSupport.relative(root, file);
                String state = "unused";
                if (config.ignores(path)) {
                    state = "ignored";
                } else if (attributes.isRegularFile() && path.endsWith(".md")) {
                    state = "skill";
                }
                states.put(path, state);
                return FileVisitResult.CONTINUE;
            }
        });
        return states;
    }

    private Map<String, Skill> index(Path root, Map<String, String> states) throws IOException {
        Map<String, Skill> skills = new TreeMap<>();
        Set<String> names = new HashSet<>();
        for (Map.Entry<String, String> entry : states.entrySet()) {
            if (!entry.getValue().equals("skill")) {
                continue;
            }
            String path = entry.getKey();
            byte[] bytes = Files.readAllBytes(root.resolve(path));
            String body = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
            Node document = markdown.parse(body);
            String name = SkillNames.fromPath(path);
            if (!names.add(name)) {
                throw new IllegalArgumentException("NAME_COLLISION: " + path + ": " + name);
            }
            String description;
            try {
                description = descriptionProvider.describe(document);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(path + ": " + exception.getMessage(), exception);
            }
            skills.put(path, Skill.builder().source(path).name(name).description(description)
                    .sha256(Hashes.sha256(bytes)).body(body)
                    .links(markdown.destinations(document)).build());
        }
        return skills;
    }

    private void writeSkill(Path root, Path output, SelectionConfig config, Skill skill,
            Map<String, Skill> skills, Map<String, String> states) throws IOException {

        Path directory = Files.createDirectory(output.resolve(skill.name()));
        StringBuilder body = new StringBuilder(skill.body());
        // Work backwards so offsets always refer to the original source, including CRLF.
        List<LinkDestination> links = skill.links().stream()
                .sorted(Comparator.comparingInt(LinkDestination::start).reversed()).toList();
        for (LinkDestination link : links) {
            var local = FileSupport.localAddress(skill.source(), link);
            if (local.isEmpty()) {
                continue;
            }
            LocalAddress address = local.get();
            Path target = FileSupport.resolveTarget(root, root.resolve(skill.source()),
                    address, link, config);
            String targetPath = FileSupport.relative(root, target);
            String replacement;
            Skill targetSkill = skills.get(targetPath);
            if (targetSkill != null) {
                replacement = "../" + targetSkill.name() + "/SKILL.md";
            } else {
                replacement = "assets/" + targetPath;
                Path asset = directory.resolve(replacement);
                Files.createDirectories(asset.getParent());
                if (!Files.exists(asset)) {
                    Files.copy(target, asset);
                }
                states.put(targetPath, "resource");
            }
            body.replace(link.start(), link.end(),
                    LocalAddress.encodePath(replacement) + address.suffix());
        }
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("name", skill.name());
        metadata.put("description", skill.description());
        String frontmatter = "---\n" + yaml.writeValueAsString(metadata) + "---\n";
        Files.writeString(directory.resolve("SKILL.md"), frontmatter + body,
                StandardCharsets.UTF_8);
    }
}
