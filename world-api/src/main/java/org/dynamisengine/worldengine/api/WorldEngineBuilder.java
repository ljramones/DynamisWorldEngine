package org.dynamisengine.worldengine.api;

import org.dynamisengine.worldengine.api.config.WorldConfig;
import org.dynamisengine.worldengine.api.lifecycle.WorldBootstrapper;
import org.dynamisengine.worldengine.api.lifecycle.WorldProjector;
import org.dynamisengine.worldengine.api.lifecycle.WorldTickRunner;

import java.util.Objects;

/**
 * Configures and launches a WorldEngine.
 *
 * <h2>Simple Path</h2>
 * <pre>{@code
 * WorldEngine.run(new MyGame());
 * }</pre>
 *
 * <h2>Advanced Path</h2>
 * <pre>{@code
 * WorldEngine.builder()
 *     .application(new MyGame())
 *     .config(new WorldConfig("1.0.0", 1, 0L))
 *     .tickRate(60)
 *     .headless()
 *     .run();
 * }</pre>
 *
 * The builder has excellent defaults. Most games only need {@code application()}.
 */
public final class WorldEngineBuilder {

    private WorldApplication application;
    private WorldConfig config;
    private WorldBootstrapper bootstrapper;
    private WorldTickRunner tickRunner;
    private WorldProjector projector;
    private int tickRate = 60;
    private boolean headless = false;
    private boolean testMode = false;

    WorldEngineBuilder() {}

    /** Set the application to run. Required. */
    public WorldEngineBuilder application(WorldApplication app) {
        this.application = Objects.requireNonNull(app, "application must not be null");
        return this;
    }

    /** Set world configuration (version, save format, initial tick). */
    public WorldEngineBuilder config(WorldConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        return this;
    }

    /**
     * Override the default bootstrapper. Advanced use only.
     * Most games should not call this.
     */
    public WorldEngineBuilder bootstrapper(WorldBootstrapper bootstrapper) {
        this.bootstrapper = bootstrapper;
        return this;
    }

    /**
     * Override the default tick runner. Advanced use only.
     */
    public WorldEngineBuilder tickRunner(WorldTickRunner tickRunner) {
        this.tickRunner = tickRunner;
        return this;
    }

    /**
     * Override the default projector. Advanced use only.
     */
    public WorldEngineBuilder projector(WorldProjector projector) {
        this.projector = projector;
        return this;
    }

    /** Set the target tick rate in Hz. Default: 60. */
    public WorldEngineBuilder tickRate(int hz) {
        if (hz < 1 || hz > 1000) throw new IllegalArgumentException("tickRate must be 1-1000 Hz");
        this.tickRate = hz;
        return this;
    }

    /** Run without a window or renderer. Useful for servers, tools, tests. */
    public WorldEngineBuilder headless() {
        this.headless = true;
        return this;
    }

    /** Run in test mode: NullAudioBackend, deterministic, no real devices. */
    public WorldEngineBuilder testMode() {
        this.testMode = true;
        this.headless = true;
        return this;
    }

    // -- Accessors for the runtime implementation ----------------------------

    /** @return the configured application, or null if not set */
    public WorldApplication application() { return application; }

    /** @return the configured world config, or a default */
    public WorldConfig configOrDefault() {
        return config != null ? config : new WorldConfig("0.0.1-dev", 1, 0L);
    }

    /** @return custom bootstrapper, or null for default */
    public WorldBootstrapper bootstrapper() { return bootstrapper; }

    /** @return custom tick runner, or null for default */
    public WorldTickRunner tickRunner() { return tickRunner; }

    /** @return custom projector, or null for default */
    public WorldProjector projector() { return projector; }

    /** @return target tick rate in Hz */
    public int tickRate() { return tickRate; }

    /** @return true if headless mode is enabled */
    public boolean isHeadless() { return headless; }

    /** @return true if test mode is enabled */
    public boolean isTestMode() { return testMode; }

    /**
     * Build and immediately run. Blocks until the engine stops.
     * This is the most common usage.
     */
    public void run() {
        WorldEngineHandle handle = build();
        handle.start();
    }

    /**
     * Build the engine without running. Returns a handle for programmatic control.
     * The engine is in CREATED state. Call {@link WorldEngineHandle#start()} to begin.
     *
     * @return a lifecycle control handle
     * @throws IllegalStateException if application is not set
     */
    public WorldEngineHandle build() {
        if (application == null) {
            throw new IllegalStateException("WorldApplication must be set before build()");
        }
        // The runtime module provides the concrete implementation.
        // This is resolved via ServiceLoader or direct instantiation in the runtime.
        return WorldEngine.createHandle(this);
    }
}
