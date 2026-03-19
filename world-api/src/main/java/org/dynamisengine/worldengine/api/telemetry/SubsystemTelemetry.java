package org.dynamisengine.worldengine.api.telemetry;

/**
 * A subsystem's telemetry contribution to the engine-wide snapshot.
 *
 * Combines the subsystem's {@link SubsystemHealth} with its detailed
 * telemetry data. The detail object is subsystem-specific (e.g.,
 * {@code AudioTelemetry}, {@code InputTelemetry}) and accessed via
 * typed cast by consumers who know the subsystem type.
 *
 * <h2>Design Rule</h2>
 * Presence of a SubsystemTelemetry does NOT imply the subsystem is active.
 * Check {@link #health()} state before reading detail.
 *
 * @param health  subsystem health state
 * @param detail  subsystem-specific telemetry record, or null if unavailable.
 *                Cast to the expected type (e.g., AudioTelemetry) based on
 *                the subsystem name from health().name().
 */
public record SubsystemTelemetry(SubsystemHealth health, Object detail) {

    /** Create with health and typed detail. */
    public static SubsystemTelemetry of(SubsystemHealth health, Object detail) {
        return new SubsystemTelemetry(health, detail);
    }

    /** Create health-only (no detailed telemetry). */
    public static SubsystemTelemetry healthOnly(SubsystemHealth health) {
        return new SubsystemTelemetry(health, null);
    }

    /** True if detailed telemetry is available. */
    public boolean hasDetail() {
        return detail != null;
    }

    /**
     * Get the detail as a specific type. Returns null if not available or wrong type.
     *
     * Usage:
     * <pre>{@code
     * AudioTelemetry audio = slot.detailAs(AudioTelemetry.class);
     * }</pre>
     */
    @SuppressWarnings("unchecked")
    public <T> T detailAs(Class<T> type) {
        if (detail != null && type.isInstance(detail)) {
            return (T) detail;
        }
        return null;
    }
}
