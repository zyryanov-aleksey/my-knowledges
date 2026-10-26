package io.github.myknowledges.skills;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public final class SkillGeneratorApplication {

    public static void main(String[] args) throws IOException {
        if (args.length < 2 || args.length > 3 ||
                args.length == 3 && !args[0].equals("package")) {
            throw new IllegalArgumentException(
                    "Usage: <generate|validate|package> <repository-root> [package-tag]");
        }
        Path root = Path.of(args[1]).toRealPath();
        FileSupport.checkBuildDirectory(root);
        Path bundle = root.resolve("build/skills");
        switch (args[0]) {
            case "generate" -> new SkillGenerator(new HeadingDescriptionProvider()).generate(root);
            case "validate" -> new BundleValidator().validate(bundle);
            case "package" -> {
                new BundleValidator().validate(bundle);
                Optional<String> tag = (args.length == 3 ? Optional.of(args[2]) : Optional.empty());
                Path archive = new ReproducibleArchive().write(bundle,
                        root.resolve("build/distributions"), tag);
                System.out.println("Packaged skills: " + archive);
            }
            default -> throw new IllegalArgumentException("Unknown command: " + args[0]);
        }
    }
}
