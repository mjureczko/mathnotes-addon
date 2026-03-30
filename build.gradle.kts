import org.gradle.api.tasks.bundling.ZipEntryCompression

plugins {
    kotlin("jvm") version "2.0.21"
}

group = "pl.marianjureczko.mathnotes"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.libreoffice:libreoffice:24.8.3")

    implementation(kotlin("stdlib"))

    testImplementation("org.libreoffice:libreoffice:24.8.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("com.ocadotechnology.gembus:test-arranger:1.7.1")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

// Fat JAR: bundle kotlin-stdlib so LibreOffice classloader finds it
tasks.jar {
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "Implementation-Title" to "MathNotes LibreOffice Addon",
            "Implementation-Version" to project.version,
            "RegistrationClassName" to "pl.marianjureczko.mathnotes.MathNotesHandler"
        )
    }
}

// ── Extension packaging ─────────────────────────────────────────────────────

val extensionName = "mathnotes-addon"

tasks.register<Zip>("buildExtension") {
    group = "build"
    description = "Assembles the .oxt LibreOffice extension package"
    dependsOn(tasks.jar)

    archiveFileName.set("$extensionName-${project.version}.oxt")
    destinationDirectory.set(layout.buildDirectory.dir("dist"))

    // Resources go in at the root of the ZIP (manifest, XCU files, etc.)
    from("src/main/resources")

    // The fat JAR is placed at the root and renamed
    from(tasks.jar.get().archiveFile) {
        rename { "mathnotes.jar" }
    }

    // mimetype entry must be first and stored uncompressed
    from(layout.buildDirectory.file("tmp/mimetype")) {
        entryCompression = ZipEntryCompression.STORED
    }
}

tasks.register("prepareMimetype") {
    val mimetypeFile = layout.buildDirectory.file("tmp/mimetype")
    outputs.file(mimetypeFile)
    doLast {
        mimetypeFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText("application/vnd.sun.star.package-bundle")
        }
    }
}

tasks.named("buildExtension") {
    dependsOn("prepareMimetype")
}

tasks.build {
    dependsOn("buildExtension")
}

// ── Install into local LibreOffice ───────────────────────────────────────────

tasks.register("installExtension") {
    group = "build"
    description = "Installs the .oxt into the local LibreOffice installation"
    dependsOn("buildExtension")
    doLast {
        val oxtFile = layout.buildDirectory.file("dist/$extensionName-${project.version}.oxt")
            .get().asFile
        exec {
            commandLine("unopkg", "add", "--force", oxtFile.absolutePath)
        }
    }
}
