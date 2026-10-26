package io.github.myknowledges.skills;

import java.io.IOException;
import java.nio.file.Path;

public final class SkillGeneratorApplication {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: <generate|validate|package> <repository-root>");
        }
        Path root = Path.of(args[1]).toRealPath();
        FileSupport.checkBuildDirectory(root);
        Path bundle = root.resolve("build/skills");
        switch (args[0]) {
            case "generate" -> new SkillGenerator(new HeadingDescriptionProvider()).generate(root);
            case "validate" -> new BundleValidator().validate(bundle);
            case "package" -> {
                new BundleValidator().validate(bundle);
                new ReproducibleArchive().write(bundle, root.resolve("build/distributions/skills.zip"));
            }
            default -> throw new IllegalArgumentException("Unknown command: " + args[0]);
        }
    }
}
