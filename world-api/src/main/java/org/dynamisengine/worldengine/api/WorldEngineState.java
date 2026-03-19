package org.dynamisengine.worldengine.api;

/**
 * Lifecycle state of the WorldEngine facade.
 *
 * Reflects what the game developer experiences, not internal subsystem choreography.
 */
public enum WorldEngineState {
    /** Builder constructed, nothing started. */
    CREATED,
    /** Subsystems starting, bootstrapper running. */
    INITIALIZING,
    /** Tick loop active, application receiving update() calls. */
    RUNNING,
    /** Tick loop suspended. Application notified via onPause/onResume. */
    PAUSED,
    /** Non-recoverable error. Awaiting shutdown. */
    FAULTED,
    /** Shutdown in progress, subsystems closing. */
    STOPPING,
    /** All resources released. Terminal. */
    STOPPED
}
