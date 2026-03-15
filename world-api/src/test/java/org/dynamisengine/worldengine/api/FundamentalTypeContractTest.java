package org.dynamisengine.worldengine.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

class FundamentalTypeContractTest {

    private static final Set<String> BANNED_FILE_NAMES = Set.of(
            "EntityId.java",
            "Vector2f.java",
            "Vector3f.java",
            "Vector4f.java",
            "Matrix3f.java",
            "Matrix4f.java",
            "Quaternionf.java",
            "Transformf.java"
    );

    @Test
    void repositoryMustNotContainFundamentalOrMathTypeDuplicates() throws IOException {
        Path moduleDir = Paths.get("").toAbsolutePath().normalize();
        Path repositoryRoot = moduleDir.getParent() != null ? moduleDir.getParent() : moduleDir;

        try (Stream<Path> pathStream = Files.walk(repositoryRoot)) {
            pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> BANNED_FILE_NAMES.contains(path.getFileName().toString()))
                    .findFirst()
                    .ifPresent(found -> fail("Banned duplicated fundamental type found: " + found));
        }
    }
}
