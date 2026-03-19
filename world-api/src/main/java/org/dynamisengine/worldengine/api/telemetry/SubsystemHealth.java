package org.dynamisengine.worldengine.api.telemetry;

/**
 * Health state of a single engine subsystem.
 *
 * @param name          subsystem name (e.g., "Audio", "Input", "Renderer")
 * @param state         current health state
 * @param lastError     most recent error message, or null if healthy
 * @param lastUpdateTick tick number of last successful update (-1 if never updated)
 */
public record SubsystemHealth(
        String name,
        HealthState state,
        String lastError,
        long lastUpdateTick) {

    public enum HealthState {
        /** Subsystem is operating normally. */
        HEALTHY,
        /** Subsystem is running but with reduced capability. */
        DEGRADED,
        /** Subsystem has failed and is not operational. */
        FAULTED,
        /** Subsystem is not initialized or not present. */
        ABSENT
    }

    /** Create a healthy subsystem snapshot. */
    public static SubsystemHealth healthy(String name, long lastUpdateTick) {
        return new SubsystemHealth(name, HealthState.HEALTHY, null, lastUpdateTick);
    }

    /** Create a degraded subsystem snapshot. */
    public static SubsystemHealth degraded(String name, String reason, long lastUpdateTick) {
        return new SubsystemHealth(name, HealthState.DEGRADED, reason, lastUpdateTick);
    }

    /** Create a faulted subsystem snapshot. */
    public static SubsystemHealth faulted(String name, String error) {
        return new SubsystemHealth(name, HealthState.FAULTED, error, -1);
    }

    /** Create an absent subsystem marker. */
    public static SubsystemHealth absent(String name) {
        return new SubsystemHealth(name, HealthState.ABSENT, null, -1);
    }
}
