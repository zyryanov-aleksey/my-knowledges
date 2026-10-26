plugins {
    application
}

group = "io.github.myknowledges"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("com.vladsch.flexmark:flexmark:0.64.8")
    implementation("com.vladsch.flexmark:flexmark-ext-yaml-front-matter:0.64.8")
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.20.1"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("io.github.myknowledges.skills.SkillGeneratorApplication")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.test {
    useJUnitPlatform()
}

val repositoryRoot = layout.projectDirectory.dir("../..").asFile.absolutePath

fun registerCommand(name: String, command: String, help: String) =
    tasks.register<JavaExec>(name) {
        group = "skills"
        description = help
        dependsOn(tasks.classes)
        classpath = sourceSets.main.get().runtimeClasspath
        mainClass.set(application.mainClass)
        args(command, repositoryRoot)
    }

val generateSkills = registerCommand("generateSkills", "generate",
    "Rebuild skills and the selection report in the repository build directory.")
val validateSkills = registerCommand("validateSkills", "validate",
    "Check all local link targets in an already generated skill set.")
validateSkills.configure { mustRunAfter(generateSkills) }
registerCommand("packageSkills", "package",
    "Validate and package an already generated skill set reproducibly.").configure {
    dependsOn(validateSkills)
    mustRunAfter(generateSkills)
    providers.gradleProperty("skillsTag").orNull?.let { args(it) }
}

tasks.wrapper {
    gradleVersion = "8.14.5"
    distributionType = Wrapper.DistributionType.BIN
}
