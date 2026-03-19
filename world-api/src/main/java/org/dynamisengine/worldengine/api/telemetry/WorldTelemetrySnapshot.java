package org.dynamisengine.worldengine.api.telemetry;

import java.util.List;
import java.util.Objects;

/**
 * Complete, structured snapshot of engine state at a point in time.
 *
 * This is the central telemetry object. It answers:
 * "What is the state of the engine right now?"
 *
 * Immutable. Strongly typed. No Map&lt;String, Object&gt;.
 * Cheap to construct. Safe to read from any thread after capture.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * WorldTelemetrySnapshot t = context.telemetry();
 * System.out.println(t.statusLine());
 * System.out.println(t.engine().budgetPercent());
 * }</pre>
 *
 * @param engine     core engine timing and state
 * @param subsystems health state of all registered subsystems
 * @param lastError  most recent engine-level error message, or null
 */
public record WorldTelemetrySnapshot(
        EngineTelemetry engine,
        List<SubsystemHealth> subsystems,
        String lastError) {

    public WorldTelemetrySnapshot {
        Objects.requireNonNull(engine, "engine telemetry must not be null");
        subsystems = subsystems != null ? List.copyOf(subsystems) : List.of();
    }

    /** Count of subsystems in HEALTHY state. */
    public long healthyCount() {
        return subsystems.stream()
                .filter(s -> s.state() == SubsystemHealth.HealthState.HEALTHY).count();
    }

    /** Count of subsystems in DEGRADED state. */
    public long degradedCount() {
        return subsystems.stream()
                .filter(s -> s.state() == SubsystemHealth.HealthState.DEGRADED).count();
    }

    /** Count of subsystems in FAULTED state. */
    public long faultedCount() {
        return subsystems.stream()
                .filter(s -> s.state() == SubsystemHealth.HealthState.FAULTED).count();
    }

    /** True if any subsystem is degraded or faulted. */
    public boolean hasIssues() {
        return degradedCount() > 0 || faultedCount() > 0;
    }

    /**
     * Compact status line for profiler overlays and quick diagnostics.
     */
    public String statusLine() {
        long healthy = healthyCount();
        long degraded = degradedCount();
        long faulted = faultedCount();
        String health = faulted > 0 ? "FAULTED(" + faulted + ")"
                : degraded > 0 ? "DEGRADED(" + degraded + ")"
                : "OK(" + healthy + ")";
        return engine.statusLine() + " | subs=" + health;
    }

    /**
     * Multi-line detailed report.
     */
    public String detailedReport() {
        StringBuilder sb = new StringBuilder(512);
        sb.append("=== World Telemetry ===\n");
        sb.append(engine.statusLine()).append('\n');
        sb.append("Uptime:            ").append(String.format("%.1fs", engine.uptimeSeconds())).append('\n');
        if (lastError != null) {
            sb.append("Last error:        ").append(lastError).append('\n');
        }
        sb.append("Subsystems:\n");
        for (SubsystemHealth sub : subsystems) {
            sb.append("  ").append(String.format("%-15s", sub.name()))
              .append(" ").append(sub.state());
            if (sub.lastError() != null) {
                sb.append(" — ").append(sub.lastError());
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
