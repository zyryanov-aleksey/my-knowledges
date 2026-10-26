package io.github.myknowledges.skills;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class ReproducibleArchive {

    Path write(Path bundle, Path directory) throws IOException {
        return write(bundle, directory, Optional.empty());
    }

    Path write(Path bundle, Path directory, Optional<String> tag) throws IOException {
        if (tag.isPresent() && tag.get().isBlank()) {
            throw new IllegalArgumentException("Skill tag must not be blank");
        }
        if (Files.isSymbolicLink(directory)) {
            throw new IllegalArgumentException("Archive output must not be a symbolic link");
        }
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, ".skills-", ".tmp");
        try {
            writeZip(bundle, temporary);
            String checksum = Hashes.sha256(Files.readAllBytes(temporary));
            String name = tag.map(this::tagName).orElse("skills-" + checksum.substring(0, 12));
            Path archive = directory.resolve(name + ".zip");
            Path checksumPath = directory.resolve(name + ".zip.sha256");
            if (Files.isSymbolicLink(archive) || Files.isSymbolicLink(checksumPath)) {
                throw new IllegalArgumentException("Archive output must not be a symbolic link");
            }
            Files.move(temporary, archive, StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(checksumPath,
                    checksum + "  " + archive.getFileName() + "\n", StandardCharsets.UTF_8);
            return archive;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private String tagName(String tag) {
        String name = tag.replaceAll("[^A-Za-z0-9._-]", "-");
        return (name.startsWith("skills-") ? name : "skills-" + name);
    }

    private void writeZip(Path bundle, Path archive) throws IOException {
        try (var files = Files.walk(bundle);
                ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archive),
                        StandardCharsets.UTF_8)) {

            for (Path file : files.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(path -> FileSupport.relative(bundle, path))).toList()) {
                ZipEntry entry = new ZipEntry(FileSupport.relative(bundle, file));
                entry.setTimeLocal(LocalDateTime.of(2000, 1, 1, 0, 0));
                zip.putNextEntry(entry);
                Files.copy(file, zip);
                zip.closeEntry();
            }
        }
    }

    static void deletePrevious(Path directory) throws IOException {
        if (Files.isSymbolicLink(directory)) {
            throw new IllegalArgumentException("Archive output must not be a symbolic link");
        }
        if (!Files.exists(directory)) {
            return;
        }
        try (var files = Files.list(directory)) {
            for (Path file : files.toList()) {
                String name = file.getFileName().toString();
                boolean archive = name.equals("skills.zip") || name.equals("skills.zip.sha256") ||
                        name.startsWith("skills-") &&
                                (name.endsWith(".zip") || name.endsWith(".zip.sha256"));
                if (archive && !Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)) {
                    Files.delete(file);
                }
            }
        }
    }
}
