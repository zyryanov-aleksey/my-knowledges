package io.github.myknowledges.skills;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.zip.ZipFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillGeneratorTest {


    @TempDir
    private Path root;

    private final ObjectMapper json = new ObjectMapper();


    @Test
    void classifiesFilesUsingOnlyIgnoreAndCopiesResourcesToEveryConsumer() throws IOException {
        config("hidden/", "private.md", "SKILL.md");
        write("hidden/nested/secret.md", "# Hidden");
        write("private.md", "# Private");
        write("SKILL.md", "# Root skill");
        write("AGENTS.md", "Agent instructions");
        write(".gitignore", "visible.md\n");
        write("visible.md", "# Visible\n[asset](media/raw.bin)\n[again](media/raw.bin)");
        write("nested/other.md", "# Other\n[asset](/media/raw.bin)");
        byte[] bytes = new byte[] {0, 1, 2, -1, -128, 10, 13};
        Files.createDirectories(root.resolve("media"));
        Files.write(root.resolve("media/raw.bin"), bytes);
        write("unused.txt", "unused");
        Map<String, byte[]> before = snapshotSources();

        generate();

        assertEquals(3, manifest().size());
        assertEquals(Map.ofEntries(Map.entry("hidden/", "ignored"),
                Map.entry("private.md", "ignored"), Map.entry("SKILL.md", "ignored"),
                Map.entry("AGENTS.md", "skill"), Map.entry("visible.md", "skill"),
                Map.entry("nested/other.md", "skill"), Map.entry("media/raw.bin", "resource"),
                Map.entry("unused.txt", "unused"), Map.entry(".gitignore", "unused"),
                Map.entry("skills-config.yaml", "unused")), states());
        for (String source : List.of("visible.md", "nested/other.md")) {
            assertArrayEquals(bytes, Files.readAllBytes(skillDirectory(source)
                    .resolve("assets/media/raw.bin")));
        }
        assertEquals("Agent instructions", metadata("AGENTS.md").get("description").asText());
        assertEquals(before.keySet(), snapshotSources().keySet());
        for (var entry : before.entrySet()) {
            assertArrayEquals(entry.getValue(), Files.readAllBytes(root.resolve(entry.getKey())));
        }
        assertEquals(Hashes.sha256(before.get("visible.md")),
                manifestEntry("visible.md").get("sha256").asText());
    }

    @Test
    void rewritesLinksImagesReferencesAndCyclesWithoutReserializingMarkdown() throws IOException {
        config();
        String source = "# A\r\n\r\n" +
                "[inline](<../b.md?mode=1&amp;x=2#part> \"title\")  \r\n" +
                "![image](/media/picture.bin#image)\r\n" +
                "[explicit][target] [target][] [target] ![target]\r\n" +
                "\r\n[target]: ../b.md#ref \"reference title\"\r\n" +
                "\r\n```md\r\n[code](missing.md)\r\n```\r\n" +
                "\r\n    [indented](missing.md)\r\n\r\n" +
                "`[inline code](missing.md)`\r\n" +
                "[external](https://example.com) [mail](mailto:a@example.com)\r\n" +
                "[network](//example.com/a) [anchor](#local) [query](?key=value)";
        write("notes/a.md", source);
        write("b.md", "# B\n[cycle](notes/a.md#cycle)\n[self](b.md)");
        write("media/picture.bin", "picture");

        generate();

        String expected = source.replace("../b.md", "../" + SkillNames.fromPath("b.md") + "/SKILL.md")
                .replace("/media/picture.bin", "assets/media/picture.bin");
        assertEquals(expected, body("notes/a.md"));
        assertTrue(body("b.md").contains("../" + SkillNames.fromPath("notes/a.md") +
                "/SKILL.md#cycle"));
        new BundleValidator().validate(root.resolve("build/skills"));
    }

    @Test
    void handlesEncodedUnicodeSpacesPlusSignsAndEscapedParentheses() throws IOException {
        config();
        write("a.md", "# A\n[space](file%20name.md?q=a+b#x)\n" +
                "[unicode](%D0%B7%D0%B0%D0%BC%D0%B5%D1%82%D0%BA%D0%B0.md)\n" +
                "[asset](media/a+b%20%23%3F\\(x\\).bin?download=1#raw)");
        write("file name.md", "# Space");
        write("заметка.md", "# Заметка");
        write("media/a+b #?(x).bin", "bytes");

        generate();

        assertEquals("# A\n[space](../" + SkillNames.fromPath("file name.md") +
                "/SKILL.md?q=a+b#x)\n[unicode](../" + SkillNames.fromPath("заметка.md") +
                "/SKILL.md)\n[asset](assets/media/a+b%20%23%3F%28x%29.bin?download=1#raw)",
                body("a.md"));
        new BundleValidator().validate(root.resolve("build/skills"));
    }

    @Test
    void resolvesHtmlEntitiesAndKeepsTheOriginalQueryAndFragmentSpelling() throws IOException {
        config();
        write("a.md", "# A\n[local](b.md&#63;q=1&amp;x=2&#35;part)\n" +
                "[asset](data&amp;more.bin#raw)\n" +
                "[external](https&#58;//example.com/a)\n[anchor](&#35;part)");
        write("b.md", "# B");
        write("data&more.bin", "data");

        generate();

        assertEquals("# A\n[local](../" + SkillNames.fromPath("b.md") +
                "/SKILL.md&#63;q=1&amp;x=2&#35;part)\n" +
                "[asset](assets/data%26more.bin#raw)\n" +
                "[external](https&#58;//example.com/a)\n[anchor](&#35;part)", body("a.md"));
        new BundleValidator().validate(root.resolve("build/skills"));
    }

    @ParameterizedTest
    @CsvSource({
            "../private.md,IGNORED_TARGET", "../hidden/deep.md,IGNORED_TARGET",
            "../private.bin,IGNORED_TARGET", "missing.md,MISSING_TARGET",
            "../media/,UNSUPPORTED_TARGET", "../../outside.md,OUTSIDE_ROOT",
            "/../outside.md,OUTSIDE_ROOT", "%2e%2e/%2e%2e/outside.md,OUTSIDE_ROOT",
            "invalid%00.md,UNSUPPORTED_TARGET", "invalid%ZZ.md,UNSUPPORTED_TARGET"
    })
    void reportsSourceLineAndAddressForEachLinkError(String address, String code) throws IOException {
        config("private.md", "private.bin", "hidden/");
        write("private.md", "# Private");
        write("private.bin", "private");
        write("hidden/deep.md", "# Hidden");
        write("media/data.bin", "data");
        write("notes/a.md", "# A\n\n[bad](" + address + ")");

        LinkException error = assertThrows(LinkException.class, this::generate);

        assertEquals(code + ": notes/a.md:3: " + address, error.getMessage());
        assertFalse(Files.exists(root.resolve("build/skills")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"alias.md", "alias/deep.md", "broken.md"})
    void neverFollowsSymbolicLinks(String target) throws IOException {
        config();
        write("real/deep.md", "# Real");
        Files.createSymbolicLink(root.resolve("alias.md"), root.resolve("real/deep.md"));
        Files.createSymbolicLink(root.resolve("alias"), root.resolve("real"));
        Files.createSymbolicLink(root.resolve("broken.md"), root.resolve("missing.md"));
        write("a.md", "# A\n[symlink](" + target + ")");

        LinkException error = assertThrows(LinkException.class, this::generate);

        assertTrue(error.getMessage().startsWith("UNSUPPORTED_TARGET: a.md:2:"));
    }

    @Test
    void skipsUnreferencedSymbolicLinksDuringIndexing() throws IOException {
        config();
        write("real/a.md", "# A");
        Files.createSymbolicLink(root.resolve("alias"), root.resolve("real"));
        Files.createSymbolicLink(root.resolve("alias.md"), root.resolve("real/a.md"));

        generate();

        assertEquals(1, manifest().size());
        assertEquals("unused", states().get("alias.md"));
    }

    @Test
    void regeneratesFromScratchAndRemovesPreviousReleaseOnFailure() throws IOException {
        config();
        write("a.md", "# A");
        write("b.md", "# B");
        generate();
        Path oldSkill = skillDirectory("b.md");
        Files.delete(root.resolve("b.md"));

        generate();

        assertEquals(1, manifest().size());
        assertFalse(Files.exists(oldSkill));
        write("build/distributions/skills.zip", "stale archive");
        write("build/distributions/skills.zip.sha256", "stale checksum");
        write("build/distributions/skills-v1.2.3.zip", "stale tagged archive");
        write("build/distributions/skills-v1.2.3.zip.sha256", "stale tagged checksum");
        write("build/distributions/skills-012345abcdef.zip", "stale hashed archive");
        write("build/distributions/skills-012345abcdef.zip.sha256", "stale hashed checksum");
        write("build/distributions/other.zip", "keep");
        write("a.md", "# A\n[missing](missing.md)");
        assertThrows(LinkException.class, this::generate);
        assertFalse(Files.exists(root.resolve("build/skills")));
        assertFalse(Files.exists(root.resolve("build/skills-report.json")));
        assertFalse(Files.exists(root.resolve("build/distributions/skills.zip")));
        assertFalse(Files.exists(root.resolve("build/distributions/skills.zip.sha256")));
        assertFalse(Files.exists(root.resolve("build/distributions/skills-v1.2.3.zip")));
        assertFalse(Files.exists(root.resolve("build/distributions/skills-v1.2.3.zip.sha256")));
        assertFalse(Files.exists(root.resolve("build/distributions/skills-012345abcdef.zip")));
        assertFalse(Files.exists(root.resolve("build/distributions/skills-012345abcdef.zip.sha256")));
        assertEquals("keep", Files.readString(root.resolve("build/distributions/other.zip")));
    }

    @Test
    void validatesTargetsInGeneratedBundleIndependentlyOfSources() throws IOException {
        config();
        write("a.md", "# A\n[b](b.md) [asset](data.bin)");
        write("b.md", "# B");
        write("data.bin", "data");
        generate();
        Path bundle = root.resolve("build/skills");
        BundleValidator validator = new BundleValidator();
        validator.validate(bundle);

        Files.delete(skillDirectory("a.md").resolve("assets/data.bin"));
        LinkException error = assertThrows(LinkException.class, () -> validator.validate(bundle));

        assertTrue(error.getMessage().startsWith("MISSING_TARGET:"));
        assertTrue(error.getMessage().endsWith("assets/data.bin"));
    }

    @Test
    void createsIdenticalSortedArchivesWithFixedTimestampsAndChecksum() throws IOException {
        config();
        write("z.md", "# Z\n[asset](data.bin)");
        write("a.md", "# A");
        write("data.bin", "binary\u0000data");
        generate();
        Path bundle = root.resolve("build/skills");
        Path distributions = root.resolve("build/distributions");
        ReproducibleArchive packager = new ReproducibleArchive();
        TimeZone original = TimeZone.getDefault();
        byte[] first;
        Path archive;
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Honolulu"));
            archive = packager.write(bundle, distributions);
            first = Files.readAllBytes(archive);
            Files.setLastModifiedTime(skillDirectory("z.md").resolve("SKILL.md"),
                    FileTime.from(Instant.parse("2026-01-01T00:00:00Z")));
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
            generate();
            assertEquals(archive, packager.write(bundle, distributions));
        } finally {
            TimeZone.setDefault(original);
        }

        assertArrayEquals(first, Files.readAllBytes(archive));
        assertEquals("skills-" + Hashes.sha256(first).substring(0, 12) + ".zip",
                archive.getFileName().toString());
        assertEquals(Hashes.sha256(first) + "  " + archive.getFileName() + "\n",
                Files.readString(archive.resolveSibling(archive.getFileName() + ".sha256")));
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            List<String> names = new ArrayList<>();
            zip.stream().forEach(entry -> {
                names.add(entry.getName());
                assertEquals(java.time.LocalDateTime.of(2000, 1, 1, 0, 0), entry.getTimeLocal());
            });
            assertEquals(names.stream().sorted().toList(), names);
            assertTrue(names.contains("manifest.json"));
            assertArrayEquals(Files.readAllBytes(root.resolve("data.bin")),
                    zip.getInputStream(zip.getEntry(SkillNames.fromPath("z.md") +
                            "/assets/data.bin")).readAllBytes());
        }
    }

    @Test
    void warnsAboutLargeInstructionsWithoutShorteningThem() throws IOException {
        config();
        String source = "# Large\n" + "instruction\n".repeat(510);
        write("large.md", source);

        generate();

        assertEquals(source, body("large.md"));
        assertEquals(1, json.readTree(root.resolve("build/skills-report.json").toFile())
                .get("warnings").size());
    }

    @Test
    void refusesSymlinkedBuildDirectory() throws IOException {
        config();
        Path outside = Files.createTempDirectory(root.getParent(), "outside-build");
        Files.createSymbolicLink(root.resolve("build"), outside);
        assertThrows(IllegalArgumentException.class, this::generate);
        assertTrue(Files.isDirectory(outside));
    }

    @Test
    void refusesSymlinkedDistributionDirectoryBeforeDeletingOldArtifacts() throws IOException {
        config();
        Path outside = Files.createTempDirectory(root.getParent(), "outside-distributions");
        Path outsideArchive = Files.writeString(outside.resolve("skills.zip"), "keep");
        Files.createDirectory(root.resolve("build"));
        Files.createSymbolicLink(root.resolve("build/distributions"), outside);

        assertThrows(IllegalArgumentException.class, this::generate);

        assertEquals("keep", Files.readString(outsideArchive));
    }

    @Test
    void rejectsMalformedUtf8RatherThanChangingSourceText() throws IOException {
        config();
        Files.write(root.resolve("a.md"), new byte[] {'#', ' ', (byte) 0xC3, (byte) 0x28});
        assertThrows(java.nio.charset.CharacterCodingException.class, this::generate);
    }

    private void config(String... ignored) throws IOException {
        StringBuilder config = new StringBuilder("ignore:\n  - build/\n");
        for (String path : ignored) {
            config.append("  - ").append(path).append('\n');
        }
        write("skills-config.yaml", config.toString());
    }

    private void write(String path, String body) throws IOException {
        Path file = root.resolve(path);
        Files.createDirectories(file.getParent());
        Files.writeString(file, body, StandardCharsets.UTF_8);
    }

    private void generate() throws IOException {
        new SkillGenerator(new HeadingDescriptionProvider()).generate(root);
    }

    private JsonNode manifest() throws IOException {
        return json.readTree(root.resolve("build/skills/manifest.json").toFile());
    }

    private JsonNode manifestEntry(String source) throws IOException {
        for (JsonNode entry : manifest()) {
            if (entry.get("source").asText().equals(source)) {
                return entry;
            }
        }
        throw new AssertionError("Missing manifest entry: " + source);
    }

    private Path skillDirectory(String source) {
        return root.resolve("build/skills").resolve(SkillNames.fromPath(source));
    }

    private String body(String source) throws IOException {
        String generated = Files.readString(skillDirectory(source).resolve("SKILL.md"));
        return generated.substring(generated.indexOf("\n---\n", 4) + 5);
    }

    private JsonNode metadata(String source) throws IOException {
        String generated = Files.readString(skillDirectory(source).resolve("SKILL.md"));
        return new ObjectMapper(new YAMLFactory()).readTree(
                generated.substring(4, generated.indexOf("\n---\n", 4)));
    }

    private Map<String, String> states() throws IOException {
        Map<String, String> states = new TreeMap<>();
        for (JsonNode entry : json.readTree(root.resolve("build/skills-report.json").toFile())
                .get("files")) {
            states.put(entry.get("path").asText(), entry.get("state").asText());
        }
        return states;
    }

    private Map<String, byte[]> snapshotSources() throws IOException {
        Map<String, byte[]> snapshot = new TreeMap<>();
        try (var paths = Files.walk(root)) {
            for (Path file : paths.filter(Files::isRegularFile)
                    .filter(path -> !path.startsWith(root.resolve("build"))).toList()) {
                snapshot.put(FileSupport.relative(root, file), Files.readAllBytes(file));
            }
        }
        return snapshot;
    }
}
