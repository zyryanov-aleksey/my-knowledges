package io.github.myknowledges.skills;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

final class FileSupport {

    static String relative(Path root, Path path) {
        return root.relativize(path).toString().replace('\\', '/');
    }

    static boolean hasSymbolicLink(Path root, Path target) {
        Path current = root;
        for (Path component : root.relativize(target)) {
            current = current.resolve(component);
            if (Files.isSymbolicLink(current)) {
                return true;
            }
        }
        return false;
    }

    static void checkBuildDirectory(Path root) {
        for (String name : List.of("build", "build/distributions")) {
            Path directory = root.resolve(name);
            if (Files.isSymbolicLink(directory) ||
                    Files.exists(directory, LinkOption.NOFOLLOW_LINKS) &&
                            !Files.isDirectory(directory)) {
                throw new IllegalArgumentException(name + " must be a real repository directory");
            }
        }
    }

    static Optional<LocalAddress> localAddress(String source, LinkDestination link) {
        try {
            return LocalAddress.parse(link.address());
        } catch (IllegalArgumentException exception) {
            throw new LinkException("UNSUPPORTED_TARGET", source, link);
        }
    }

    static void deleteTree(Path path) throws IOException {
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            try (var paths = Files.walk(path)) {
                for (Path entry : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(entry);
                }
            }
        }
    }

    static Path resolveTarget(Path root, Path source, LocalAddress address,
            LinkDestination link, SelectionConfig config) {

        String origin = relative(root, source);
        Path target;
        try {
            target = (address.path().startsWith("/") ?
                    root.resolve(address.path().substring(1)) :
                    source.getParent().resolve(address.path())).normalize();
        } catch (InvalidPathException exception) {
            throw new LinkException("UNSUPPORTED_TARGET", origin, link);
        }
        if (!target.startsWith(root)) {
            throw new LinkException("OUTSIDE_ROOT", origin, link);
        }
        if (config.ignores(relative(root, target))) {
            throw new LinkException("IGNORED_TARGET", origin, link);
        }
        if (hasSymbolicLink(root, target)) {
            throw new LinkException("UNSUPPORTED_TARGET", origin, link);
        }
        if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            throw new LinkException("MISSING_TARGET", origin, link);
        }
        if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
            throw new LinkException("UNSUPPORTED_TARGET", origin, link);
        }
        return target;
    }
}
