/*
 * Paperclip - Paper Minecraft launcher
 *
 * Copyright (c) 2019 Kyle Wood (DemonWav)
 * https://github.com/PaperMC/Paperclip
 *
 * MIT License
 */

package com.hpfxd.pandaspigot.paperclip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class SwmClassModifier {
    private static final String ENABLED_PROPERTY = "titanium.swm.classmodifier.enabled";
    private static final String MODIFIER_CLASS = "com.grinderwolf.swm.clsm.NMSTransformer";
    private static JarFile modifierJarFile;

    private SwmClassModifier() {}

    static synchronized void configure(Instrumentation instrumentation) {
        if (Boolean.getBoolean(ENABLED_PROPERTY)) {
            return;
        }

        try {
            if (!hasSwmPlugin()) {
                return;
            }

            Path modifierJar = getModifierJar();
            JarFile jarFile = new JarFile(modifierJar.toFile());
            instrumentation.appendToSystemClassLoaderSearch(jarFile);
            Class<?> transformer = Class.forName(MODIFIER_CLASS, true, ClassLoader.getSystemClassLoader());
            Method premain = transformer.getMethod("premain", String.class, Instrumentation.class);
            premain.invoke(null, "", instrumentation);

            modifierJarFile = jarFile;
            System.setProperty(ENABLED_PROPERTY, "true");
            System.out.println("[Titanium] Activated the SlimeWorldManager class modifier for v1_8_R3.");
        } catch (IOException | ReflectiveOperationException | LinkageError e) {
            throw new IllegalStateException("Could not activate the SlimeWorldManager class modifier.", e);
        }
    }

    private static Path getModifierJar() throws IOException {
        Path externalJar = Paths.get("slimeworldmanager-classmodifier.jar").toAbsolutePath().normalize();
        if (Files.isRegularFile(externalJar)) {
            return externalJar;
        }

        InputStream resource = SwmClassModifier.class.getResourceAsStream(
                "/META-INF/titanium/slimeworldmanager-classmodifier.jar");
        if (resource == null) {
            throw new IOException("The bundled SlimeWorldManager class modifier is missing.");
        }

        Path temporaryJar = Files.createTempFile("titanium-swm-classmodifier-", ".jar");
        try (InputStream input = resource) {
            Files.copy(input, temporaryJar, StandardCopyOption.REPLACE_EXISTING);
        }
        temporaryJar.toFile().deleteOnExit();
        return temporaryJar;
    }

    private static boolean hasSwmPlugin() throws IOException {
        Path pluginsFolder = Paths.get("plugins");
        if (!Files.isDirectory(pluginsFolder)) {
            return false;
        }

        try (DirectoryStream<Path> jars = Files.newDirectoryStream(pluginsFolder, "*.jar")) {
            for (Path jarPath : jars) {
                try {
                    if (isSwmPlugin(jarPath)) {
                        return true;
                    }
                } catch (IOException e) {
                    System.err.println("[Titanium] Could not inspect plugin JAR " + jarPath + ": " + e.getMessage());
                }
            }
        }
        return false;
    }

    private static boolean isSwmPlugin(Path jarPath) throws IOException {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            JarEntry descriptor = jar.getJarEntry("plugin.yml");
            if (descriptor == null) {
                return false;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    jar.getInputStream(descriptor), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String field = line.trim();
                    if (!field.startsWith("name:")) {
                        continue;
                    }

                    String name = field.substring("name:".length()).trim();
                    if (name.length() >= 2 && (name.startsWith("\"") || name.startsWith("'"))) {
                        name = name.substring(1, name.length() - 1);
                    }
                    return "SlimeWorldManager".equalsIgnoreCase(name);
                }
            }
        }
        return false;
    }
}
