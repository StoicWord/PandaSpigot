plugins {
    id("pandaspigot.conventions")
    id("paperclip")
    id("com.gradleup.shadow") version "9.6.1"
}

dependencies {
    implementation("io.sigpipe:jbsdiff:1.0")
}

sourceSets {
    create("java9")
}

sourceSets.named("java9") {
    compileClasspath += sourceSets["main"].output
}

tasks {
    jar {
        archiveClassifier.set("original")
        manifest {
            val agentClass = "com.hpfxd.pandaspigot.paperclip.Agent"
            attributes(
                "Main-Class" to "com.hpfxd.pandaspigot.paperclip.Paperclip",
                "Multi-Release" to true,
                "Launcher-Agent-Class" to agentClass,
                "Premain-Class" to agentClass,
            )
        }
    }

    shadowJar {
        val prefix = "com.hpfxd.pandaspigot.paperclip.libs"
        arrayOf("org.apache", "org.tukaani", "io.sigpipe").forEach { pack ->
            relocate(pack, "$prefix.$pack")
        }

        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/NOTICE.txt")

        into("META-INF/versions/9") {
            from(sourceSets["java9"].output)
        }

        from(file("license.txt")) {
            into("META-INF/license")
            rename { "paperclip-LICENSE.txt" }
        }
        from(rootProject.file("third_party/slime-world-manager/slimeworldmanager-classmodifier.jar")) {
            into("META-INF/titanium")
        }
        from(rootProject.file("third_party/slime-world-manager/LICENSE.txt")) {
            into("META-INF/license/slime-world-manager")
            rename { "LICENSE.txt" }
        }
        from(rootProject.file("third_party/slime-world-manager/source.tar.gz")) {
            into("META-INF/license/slime-world-manager")
        }
    }
}

val titaniumDistribution by tasks.registering(Zip::class) {
    dependsOn(tasks.named("paperclipJar"))
    archiveFileName.set("Titanium-1.0.zip")
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("distributions"))

    from(rootProject.file("run.sh")) {
        filePermissions {
            unix("755")
        }
    }
    from(rootProject.file("run.bat"))
    from(layout.buildDirectory.file("libs/paperclip-1.8.8-R0.1-SNAPSHOT.jar")) {
        rename { "paperclip.jar" }
    }
    from(rootProject.file("third_party/slime-world-manager/LICENSE.txt")) {
        into("licenses/slime-world-manager")
    }
    from(rootProject.file("third_party/slime-world-manager/README.md")) {
        into("licenses/slime-world-manager")
    }
    from(rootProject.file("third_party/slime-world-manager/source.tar.gz")) {
        into("licenses/slime-world-manager")
    }
    from(rootProject.file("third_party/spark/spark-1.10.196-bukkit.jar")) {
        into("plugins")
        rename { "spark.jar" }
    }
    from(rootProject.file("third_party/spark/LICENSE.txt")) {
        into("licenses/spark")
    }
    from(rootProject.file("third_party/spark/README.md")) {
        into("licenses/spark")
    }
}

tasks.named("build") {
    dependsOn(titaniumDistribution)
}
