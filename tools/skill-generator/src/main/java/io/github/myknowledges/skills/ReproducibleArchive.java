package io.github.myknowledges.skills;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class ReproducibleArchive {

    void write(Path bundle, Path archive) throws IOException {
        Path checksumPath = archive.resolveSibling(archive.getFileName() + ".sha256");
        Files.createDirectories(archive.getParent());
        if (Files.isSymbolicLink(archive) || Files.isSymbolicLink(archive.getParent()) ||
                Files.isSymbolicLink(checksumPath)) {
            throw new IllegalArgumentException("Archive output must not be a symbolic link");
        }
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
        String checksum = Hashes.sha256(Files.readAllBytes(archive));
        Files.writeString(checksumPath,
                checksum + "  " + archive.getFileName() + "\n", StandardCharsets.UTF_8);
    }
}
