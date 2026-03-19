package org.dynamisengine.worldengine.api.telemetry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Complete, structured snapshot of engine state at a point in time.
 *
 * This is the central telemetry object. It answers:
 * "What is the state of the engine right now?"
 *
 * Immutable. Strongly typed. Cheap to construct. Safe to read from any thread.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * WorldTelemetrySnapshot t = context.telemetry();
 * System.out.println(t.statusLine());
 * System.out.println(t.engine().budgetPercent());
 *
 * // Access subsystem-specific telemetry
 * SubsystemTelemetry audio = t.subsystem("Audio");
 * if (audio != null && audio.hasDetail()) {
 *     AudioTelemetry at = audio.detailAs(AudioTelemetry.class);
 * }
 * }</pre>
 *
 * <h2>Well-known Subsystem Names</h2>
 * <ul>
 *   <li>{@code "Audio"} — DynamisAudio telemetry</li>
 *   <li>{@code "Input"} — DynamisInput telemetry</li>
 *   <li>{@code "SpatialInput"} — DynamisSpatialInput telemetry</li>
 *   <li>{@code "ECS"} — Entity Component System</li>
 *   <li>{@code "Session"} — Session/save management</li>
 *   <li>{@code "Content"} — Content/asset management</li>
 *   <li>{@code "SceneGraph"} — Scene graph</li>
 *   <li>{@code "Renderer"} — DynamisLightEngine</li>
 * </ul>
 *
 * @param engine     core engine timing and state
 * @param subsystems per-subsystem telemetry keyed by name
 * @param lastError  most recent engine-level error message, or null
 */
public record WorldTelemetrySnapshot(
        EngineTelemetry engine,
        Map<String, SubsystemTelemetry> subsystems,
        String lastError) {

    // Well-known subsystem names
    public static final String AUDIO = "Audio";
    public static final String INPUT = "Input";
    public static final String SPATIAL_INPUT = "SpatialInput";
    public static final String ECS = "ECS";
    public static final String SESSION = "Session";
    public static final String CONTENT = "Content";
    public static final String SCENE_GRAPH = "SceneGraph";
    public static final String RENDERER = "Renderer";

    public WorldTelemetrySnapshot {
        Objects.requireNonNull(engine, "engine telemetry must not be null");
        subsystems = subsystems != null ? Map.copyOf(subsystems) : Map.of();
    }

    /** Get telemetry for a specific subsystem by name, or null if absent. */
    public SubsystemTelemetry subsystem(String name) {
        return subsystems.get(name);
    }

    /** Count of subsystems in HEALTHY state. */
    public long healthyCount() {
        return subsystems.values().stream()
                .filter(s -> s.health().state() == SubsystemHealth.HealthState.HEALTHY).count();
    }

    /** Count of subsystems in DEGRADED state. */
    public long degradedCount() {
        return subsystems.values().stream()
                .filter(s -> s.health().state() == SubsystemHealth.HealthState.DEGRADED).count();
    }

    /** Count of subsystems in FAULTED state. */
    public long faultedCount() {
        return subsystems.values().stream()
                .filter(s -> s.health().state() == SubsystemHealth.HealthState.FAULTED).count();
    }

    /** True if any subsystem is degraded or faulted. */
    public boolean hasIssues() {
        return degradedCount() > 0 || faultedCount() > 0;
    }

    /** Compact status line for profiler overlays. */
    public String statusLine() {
        long healthy = healthyCount();
        long degraded = degradedCount();
        long faulted = faultedCount();
        String health = faulted > 0 ? "FAULTED(" + faulted + ")"
                : degraded > 0 ? "DEGRADED(" + degraded + ")"
                : "OK(" + healthy + ")";
        return engine.statusLine() + " | subs=" + health;
    }

    /** Multi-line detailed report. */
    public String detailedReport() {
        StringBuilder sb = new StringBuilder(512);
        sb.append("=== World Telemetry ===\n");
        sb.append(engine.statusLine()).append('\n');
        sb.append("Uptime:            ").append(String.format("%.1fs", engine.uptimeSeconds())).append('\n');
        if (lastError != null) {
            sb.append("Last error:        ").append(lastError).append('\n');
        }
        sb.append("Subsystems:\n");
        for (var entry : subsystems.entrySet()) {
            SubsystemTelemetry st = entry.getValue();
            sb.append("  ").append(String.format("%-15s", entry.getKey()))
              .append(" ").append(st.health().state());
            if (st.health().lastError() != null) {
                sb.append(" - ").append(st.health().lastError());
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
