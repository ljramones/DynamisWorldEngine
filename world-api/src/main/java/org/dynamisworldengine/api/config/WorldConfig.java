package org.dynamisworldengine.api.config;

import java.util.Objects;

public record WorldConfig(
        String buildVersion,
        int saveFormatVersion,
        long initialTick
) {
    public WorldConfig {
        Objects.requireNonNull(buildVersion, "buildVersion");
        if (buildVersion.isBlank()) {
            throw new IllegalArgumentException("buildVersion must not be blank");
        }
        if (saveFormatVersion < 0) {
            throw new IllegalArgumentException("saveFormatVersion must be >= 0");
        }
        if (initialTick < 0) {
            throw new IllegalArgumentException("initialTick must be >= 0");
        }
    }
}
