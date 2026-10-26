package io.github.myknowledges.skills;

import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataTest {


    private final MarkdownSupport markdown = new MarkdownSupport();

    private final DescriptionProvider descriptions = new HeadingDescriptionProvider();


    @Test
    void usesFirstH1AndRemovesOnlyItsMarkdownMarkup() {
        assertEquals("Title with code and a link & entity", description("Intro\n\n" +
                "# **Title** with `code` and [a link](https://example.com) &amp; entity\n" +
                "# Second"));
        assertEquals("Setext title", description("Setext *title*\n===\n"));
    }

    @Test
    void fallsBackToFirstTextLineOutsideCodeAndFrontmatter() {
        assertEquals("First line", description("---\nname: metadata\n---\n\n" +
                "```text\n# Code heading\n```\n\n    indented code\n\n" +
                "**First** line\nSecond line\n"));
        assertEquals("Subheading", description("## Subheading\n\nParagraph"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  \n\n", "```\nonly code\n```", "    code\n", "#\n", "---\n"})
    void rejectsEmptyDescriptions(String source) {
        assertThrows(IllegalArgumentException.class, () -> description(source));
    }

    @Test
    void limitsDescriptionByUnicodeCodePoints() {
        String description = description("# " + "Ж".repeat(1100));
        assertEquals("Ж".repeat(1024), description);
        String supplementary = description("# " + "\uD801\uDC00".repeat(1100));
        assertEquals(1024, supplementary.codePointCount(0, supplementary.length()));
    }

    @Test
    void escapesYamlAndPreservesOriginalText(@TempDir Path root) throws Exception {
        Files.writeString(root.resolve("skills-config.yaml"), "ignore:\n  - build/\n");
        String source = "# Value: \"quotes\", [brackets], \\slashes, 'single'\n\nBody";
        Files.writeString(root.resolve("a.md"), source);

        new SkillGenerator(descriptions).generate(root);

        String generated = Files.readString(root.resolve("build/skills")
                .resolve(SkillNames.fromPath("a.md")).resolve("SKILL.md"));
        int boundary = generated.indexOf("\n---\n", 4);
        var metadata = new ObjectMapper(new YAMLFactory()).readTree(generated.substring(4, boundary));
        assertEquals(description(source), metadata.get("description").asText());
        assertEquals(source, generated.substring(boundary + 5));
        assertEquals(source, Files.readString(root.resolve("a.md")));
    }

    @Test
    void producesStableAsciiNamesFromTheFullRelativePath() {
        assertEquals("agents-a54ff182c7e8", SkillNames.fromPath("AGENTS.md"));
        assertEquals("cafe-guide-6b6672aaa14f", SkillNames.fromPath("Café/Guide.md"));
        assertEquals("skill-df363b7129fa", SkillNames.fromPath("Заметка.md"));
        assertNotEquals(SkillNames.fromPath("a/guide.md"), SkillNames.fromPath("b/guide.md"));
        String name = SkillNames.fromPath("nested/" + "a".repeat(300) + "/guide.md");
        assertEquals(64, name.length());
        assertTrue(name.matches("[a-z0-9]+(?:-[a-z0-9]+)*"));
        assertFalse(SkillNames.fromPath("a".repeat(50) + "/long.md").contains("--"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"include: []", "ignore: anything", "ignore: [../outside/]",
            "ignore: [/absolute/]", "ignore: [a/../b]", "ignore: [a//b]", "ignore: [a/./b]",
            "ignore: [null]", "ignore: ['']", "ignore: []\nother: []"})
    void rejectsInvalidSelectionConfig(String config, @TempDir Path root) throws Exception {
        Path path = root.resolve("skills-config.yaml");
        Files.writeString(path, config);
        assertThrows(IllegalArgumentException.class, () -> SelectionConfig.load(path));
    }

    @Test
    void fileRuleDoesNotExcludeADirectorySubtree(@TempDir Path root) throws Exception {
        Path path = root.resolve("skills-config.yaml");
        Files.writeString(path, "ignore:\n  - notes\n  - ..private.md\n");
        SelectionConfig config = SelectionConfig.load(path);
        assertFalse(config.ignoresDirectory("notes"));
        assertFalse(config.ignores("notes/a.md"));
        assertTrue(config.ignores("..private.md"));
    }

    private String description(String source) {
        return descriptions.describe(markdown.parse(source));
    }
}
