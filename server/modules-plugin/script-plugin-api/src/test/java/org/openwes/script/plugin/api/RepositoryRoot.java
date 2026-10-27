package org.openwes.script.plugin.api;

import java.nio.file.Files;
import java.nio.file.Path;

/** Locates the repository root for tests that read docs and sample files. */
public final class RepositoryRoot {

    private RepositoryRoot() {
    }

    public static Path find() {
        Path start = Path.of("").toAbsolutePath().normalize();
        Path candidate = start;
        while (candidate != null) {
            if (Files.isRegularFile(candidate.resolve("server/settings.gradle"))) {
                return candidate;
            }
            candidate = candidate.getParent();
        }
        throw new IllegalStateException(
                "Cannot locate the repository root (server/settings.gradle) from " + start);
    }
}
