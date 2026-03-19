package org.dynamisengine.worldengine.api;

/**
 * Programmatic control handle for a built but not-yet-running WorldEngine.
 *
 * Returned by {@link WorldEngineBuilder#build()} for advanced users who need
 * explicit lifecycle control (e.g., test harnesses, tool integrations).
 *
 * KEPT DELIBERATELY MINIMAL — lifecycle-oriented, not subsystem-oriented.
 * Does not expose internal coordinators, subsystem registries, or mutation hooks.
 */
public interface WorldEngineHandle {

    /** Start the engine: initialize subsystems, enter run loop. */
    void start();

    /** Pause the tick loop. Application receives onPause(). Resumable. */
    void pause();

    /** Resume from pause. Application receives onResume(). */
    void resume();

    /** Request shutdown. Triggers app.shutdown() and subsystem teardown. */
    void stop();

    /** Current lifecycle state. Safe to call from any thread. */
    WorldEngineState state();

    /**
     * The game context. Only valid after RUNNING state is reached.
     * Returns null if not yet initialized.
     */
    GameContext context();
}
