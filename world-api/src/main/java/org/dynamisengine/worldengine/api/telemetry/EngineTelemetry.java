package org.dynamisengine.worldengine.api.telemetry;

import org.dynamisengine.worldengine.api.WorldEngineState;

/**
 * Core engine timing and state telemetry.
 *
 * @param state              current engine lifecycle state
 * @param tick               current tick number
 * @param uptimeSeconds      seconds since engine start
 * @param lastTickDurationMs duration of the most recent tick in milliseconds
 * @param avgTickDurationMs  rolling average tick duration in milliseconds
 * @param maxTickDurationMs  worst-case tick duration since start
 * @param targetTickMs       target tick duration based on configured tick rate
 * @param tickRate           configured tick rate in Hz
 */
public record EngineTelemetry(
        WorldEngineState state,
        long tick,
        double uptimeSeconds,
        double lastTickDurationMs,
        double avgTickDurationMs,
        double maxTickDurationMs,
        double targetTickMs,
        int tickRate) {

    /**
     * Tick budget utilization as a percentage (0-100+).
     * Over 100% means the engine is slower than real-time.
     */
    public double budgetPercent() {
        return targetTickMs > 0 ? (lastTickDurationMs / targetTickMs) * 100.0 : 0;
    }

    /** Compact status line for overlays. */
    public String statusLine() {
        return String.format("%s | tick=%d | %.1fHz target | dt=%.2fms (avg=%.2f max=%.2f) | budget=%.0f%%",
                state, tick, (double) tickRate,
                lastTickDurationMs, avgTickDurationMs, maxTickDurationMs,
                budgetPercent());
    }
}
