package io.github.myknowledges.skills;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReproducibleArchiveTest {


    @TempDir
    private Path root;

    private final ReproducibleArchive packager = new ReproducibleArchive();


    @ParameterizedTest
    @CsvSource({"skills-v1.2.3,skills-v1.2.3.zip", "v1.2.3,skills-v1.2.3.zip",
            "skills-v1.2.3/rc1,skills-v1.2.3-rc1.zip", "../outside,skills-..-outside.zip"})
    void namesArchivesByTagAndWritesTheFinalNameInChecksum(String tag, String name) throws IOException {
        Path bundle = bundle();

        Path archive = packager.write(bundle, root.resolve("distributions"), Optional.of(tag));

        assertEquals(root.resolve("distributions").resolve(name), archive);
        assertEquals(Hashes.sha256(Files.readAllBytes(archive)) + "  " + name + "\n",
                Files.readString(archive.resolveSibling(name + ".sha256")));
        try (var files = Files.list(archive.getParent())) {
            assertEquals(2, files.count());
        }
    }

    @Test
    void changesHashNameOnlyWhenArchiveContentChanges() throws IOException {
        Path bundle = bundle();
        Path directory = root.resolve("distributions");

        Path first = packager.write(bundle, directory);
        Path repeated = packager.write(bundle, directory);
        Files.writeString(bundle.resolve("SKILL.md"), "# Changed");
        Path changed = packager.write(bundle, directory);

        assertEquals(first, repeated);
        assertNotEquals(first.getFileName(), changed.getFileName());
        assertEquals("skills-" + Hashes.sha256(Files.readAllBytes(changed)).substring(0, 12) + ".zip",
                changed.getFileName().toString());
    }

    @Test
    void tagChangesOnlyTheOutputNameAndNotZipBytes() throws IOException {
        Path bundle = bundle();
        Path directory = root.resolve("distributions");

        Path hashed = packager.write(bundle, directory);
        Path tagged = packager.write(bundle, directory, Optional.of("skills-v1.2.3"));
        Path otherTag = packager.write(bundle, directory, Optional.of("skills-v1.2.4"));

        assertArrayEquals(Files.readAllBytes(hashed), Files.readAllBytes(tagged));
        assertArrayEquals(Files.readAllBytes(tagged), Files.readAllBytes(otherTag));
    }

    @Test
    void rejectsEmptyExplicitTag() throws IOException {
        Path bundle = bundle();

        assertThrows(IllegalArgumentException.class,
                () -> packager.write(bundle, root.resolve("distributions"), Optional.of("  ")));

        assertFalse(Files.exists(root.resolve("distributions")));
    }

    @Test
    void rejectsSymlinkedChecksumAndRemovesTemporaryArchive() throws IOException {
        Path bundle = bundle();
        Path directory = Files.createDirectory(root.resolve("distributions"));
        Path original = Files.writeString(root.resolve("original"), "keep");
        Files.createSymbolicLink(directory.resolve("skills-v1.zip.sha256"), original);

        assertThrows(IllegalArgumentException.class,
                () -> packager.write(bundle, directory, Optional.of("skills-v1")));

        assertEquals("keep", Files.readString(original));
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    private Path bundle() throws IOException {
        Path bundle = Files.createDirectory(root.resolve("bundle"));
        Files.writeString(bundle.resolve("SKILL.md"), "# Example\nBody\n");
        return bundle;
    }
}
