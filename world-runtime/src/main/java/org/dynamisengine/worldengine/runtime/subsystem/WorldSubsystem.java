package org.dynamisengine.worldengine.runtime.subsystem;

import org.dynamisengine.worldengine.api.WorldContext;
import org.dynamisengine.worldengine.api.lifecycle.DynamisInitException;
import org.dynamisengine.worldengine.api.lifecycle.DynamisShutdownException;
import org.dynamisengine.worldengine.api.lifecycle.DynamisTickException;
import org.dynamisengine.worldengine.api.telemetry.SubsystemHealth;

import java.util.Optional;
import java.util.Set;

/**
 * Internal contract for engine subsystems managed by WorldEngine.
 *
 * This is NOT a public API. Game developers never see this interface.
 * It exists for engine integration: how Audio, Input, Renderer, etc.
 * plug into the orchestration spine.
 *
 * <h2>Lifecycle</h2>
 * <pre>{@code
 * initialize(ctx) → start() → tick() ... tick() → stop() → shutdown()
 * }</pre>
 * All calls made by {@link SubsystemCoordinator} in dependency order.
 *
 * <h2>Contract</h2>
 * <ul>
 *   <li>Keep the implementation small — adapters over existing subsystem managers</li>
 *   <li>Do not implement game logic or policy</li>
 *   <li>Telemetry is optional but encouraged</li>
 *   <li>Health must always be reportable, even after failure</li>
 * </ul>
 */
public interface WorldSubsystem {

    /** Subsystem name — must match a well-known name from WorldTelemetrySnapshot. */
    String name();

    /**
     * Names of subsystems this one depends on.
     * Coordinator initializes dependencies first, shuts them down last.
     * Return empty set for no dependencies.
     */
    default Set<String> dependencies() { return Set.of(); }

    /**
     * Initialize the subsystem. Called once during engine startup,
     * in dependency order. May allocate, load resources, discover backends.
     */
    void initialize(WorldContext context) throws DynamisInitException;

    /**
     * Start active operation. Called after all subsystems are initialized.
     * Begin producing data, start worker threads, etc.
     */
    void start() throws DynamisInitException;

    /**
     * Per-tick update. Called each engine tick in dependency order.
     * Keep lightweight — heavy work should be on subsystem-owned threads.
     */
    void tick(long tick, float deltaSeconds) throws DynamisTickException;

    /**
     * Stop active operation. Called during shutdown in reverse dependency order.
     * Stop worker threads, cease producing data.
     */
    void stop() throws DynamisShutdownException;

    /**
     * Release all resources. Called after stop, in reverse dependency order.
     */
    void shutdown() throws DynamisShutdownException;

    /** Current health state. Must be safe to call at any time, even after failure. */
    SubsystemHealth health();

    /**
     * Capture subsystem-specific telemetry detail.
     * Returns the typed telemetry object (e.g., AudioTelemetry, InputTelemetry).
     * Empty if telemetry is not available.
     */
    default Optional<Object> captureTelemetry() { return Optional.empty(); }
}
