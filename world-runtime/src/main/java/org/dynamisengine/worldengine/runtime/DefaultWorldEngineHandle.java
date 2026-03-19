package org.dynamisengine.worldengine.runtime;

import org.dynamisengine.worldengine.api.*;
import org.dynamisengine.worldengine.api.config.WorldConfig;
import org.dynamisengine.worldengine.api.lifecycle.*;
import org.dynamisengine.worldengine.runtime.projection.DefaultWorldProjector;
import org.dynamisengine.worldengine.runtime.session.DefaultWorldBootstrapper;

import java.util.Objects;

/**
 * Default runtime implementation of {@link WorldEngineHandle}.
 *
 * Glues the developer-facing facade ({@link WorldEngine}, {@link WorldApplication})
 * to the existing orchestration core ({@link WorldBootstrapper}, {@link WorldTickRunner},
 * {@link WorldProjector}).
 *
 * The implementation is deliberately simple and readable. It does not expose
 * internal subsystem complexity to game developers.
 */
final class DefaultWorldEngineHandle implements WorldEngineHandle {

    private static final System.Logger LOG =
            System.getLogger(DefaultWorldEngineHandle.class.getName());

    private final WorldApplication application;
    private final WorldConfig config;
    private final WorldBootstrapper bootstrapper;
    private final WorldTickRunner tickRunner;
    private final int tickRate;
    private final boolean headless;

    private volatile WorldEngineState state = WorldEngineState.CREATED;
    private volatile GameContext gameContext;
    private volatile boolean stopRequested = false;

    DefaultWorldEngineHandle(WorldEngineBuilder builder) {
        this.application = Objects.requireNonNull(builder.application());
        this.config = builder.configOrDefault();
        this.tickRate = builder.tickRate();
        this.headless = builder.isHeadless();

        // Use custom or default core components
        WorldProjector projector = builder.projector() != null
                ? builder.projector()
                : new DefaultWorldProjector();

        this.tickRunner = builder.tickRunner() != null
                ? builder.tickRunner()
                : new DefaultWorldTickRunner(projector);

        this.bootstrapper = builder.bootstrapper() != null
                ? builder.bootstrapper()
                : new DefaultWorldBootstrapper();
    }

    @Override
    public void start() {
        if (state != WorldEngineState.CREATED) {
            throw new IllegalStateException(
                    "Cannot start from state " + state + " (expected CREATED)");
        }

        try {
            // -- INITIALIZING -------------------------------------------------
            state = WorldEngineState.INITIALIZING;
            LOG.log(System.Logger.Level.INFO, "WorldEngine initializing...");

            // Bootstrap the world (creates WorldContext with ECS, session, content, scene)
            WorldContext worldContext = bootstrapper.newGame(config);

            // Create enriched GameContext
            this.gameContext = new GameContext(worldContext, 0, 0.0, state);

            // Call application initialize
            LOG.log(System.Logger.Level.INFO, "Calling application.initialize()");
            application.initialize(gameContext);

            // -- RUNNING ------------------------------------------------------
            state = WorldEngineState.RUNNING;
            LOG.log(System.Logger.Level.INFO,
                    "WorldEngine running at {0}Hz (headless={1})", tickRate, headless);

            runLoop(worldContext);

        } catch (DynamisInitException e) {
            LOG.log(System.Logger.Level.ERROR,
                    "Initialization failed ({0}): {1}", e.subsystemName(), e.getMessage());
            state = WorldEngineState.FAULTED;
        } catch (Throwable t) {
            LOG.log(System.Logger.Level.ERROR,
                    "Unexpected error during startup: " + t.getMessage(), t);
            state = WorldEngineState.FAULTED;
        } finally {
            shutdown();
        }
    }

    @Override
    public void pause() {
        if (state == WorldEngineState.RUNNING) {
            state = WorldEngineState.PAUSED;
            LOG.log(System.Logger.Level.INFO, "WorldEngine paused");
            try {
                application.onPause(gameContext);
            } catch (Throwable t) {
                LOG.log(System.Logger.Level.WARNING, "onPause threw: " + t.getMessage(), t);
            }
        }
    }

    @Override
    public void resume() {
        if (state == WorldEngineState.PAUSED) {
            state = WorldEngineState.RUNNING;
            LOG.log(System.Logger.Level.INFO, "WorldEngine resumed");
            try {
                application.onResume(gameContext);
            } catch (Throwable t) {
                LOG.log(System.Logger.Level.WARNING, "onResume threw: " + t.getMessage(), t);
            }
        }
    }

    @Override
    public void stop() {
        stopRequested = true;
    }

    @Override
    public WorldEngineState state() {
        return state;
    }

    @Override
    public GameContext context() {
        return gameContext;
    }

    // -- Run Loop -------------------------------------------------------------

    private void runLoop(WorldContext worldContext) {
        long tick = config.initialTick();
        long tickDurationNanos = 1_000_000_000L / tickRate;
        long startTimeNanos = System.nanoTime();
        long previousTickNanos = startTimeNanos;

        while (state == WorldEngineState.RUNNING && !stopRequested) {
            long tickStart = System.nanoTime();

            // Calculate delta
            float deltaSeconds = (float) ((tickStart - previousTickNanos) / 1_000_000_000.0);
            previousTickNanos = tickStart;
            double elapsedSeconds = (tickStart - startTimeNanos) / 1_000_000_000.0;

            // Update GameContext with current timing
            this.gameContext = new GameContext(worldContext, tick, elapsedSeconds, state);

            // Run world tick (ECS simulation + projection)
            try {
                tickRunner.runTick(worldContext, tick);
            } catch (DynamisTickException e) {
                if (e.isRecoverable()) {
                    LOG.log(System.Logger.Level.WARNING,
                            "Recoverable tick error ({0}, tick {1}): {2}",
                            e.subsystemName(), e.tickNumber(), e.getMessage());
                } else {
                    LOG.log(System.Logger.Level.ERROR,
                            "Non-recoverable tick error ({0}, tick {1}): {2}",
                            e.subsystemName(), e.tickNumber(), e.getMessage());
                    state = WorldEngineState.FAULTED;
                    break;
                }
            } catch (Throwable t) {
                LOG.log(System.Logger.Level.ERROR,
                        "Uncaught exception in tick " + tick + ": " + t.getMessage(), t);
                state = WorldEngineState.FAULTED;
                break;
            }

            // Application update
            try {
                application.update(gameContext, deltaSeconds);
            } catch (Throwable t) {
                LOG.log(System.Logger.Level.ERROR,
                        "Uncaught exception in application.update (tick " + tick + "): " + t.getMessage(), t);
                state = WorldEngineState.FAULTED;
                break;
            }

            tick++;

            // Pace to target tick rate
            long elapsed = System.nanoTime() - tickStart;
            long sleepNanos = tickDurationNanos - elapsed;
            if (sleepNanos > 1_000_000) { // only sleep if > 1ms remaining
                try {
                    Thread.sleep(sleepNanos / 1_000_000, (int) (sleepNanos % 1_000_000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    // -- Shutdown -------------------------------------------------------------

    private void shutdown() {
        if (state == WorldEngineState.STOPPED) return;
        state = WorldEngineState.STOPPING;
        LOG.log(System.Logger.Level.INFO, "WorldEngine shutting down...");

        // Call application shutdown
        if (gameContext != null) {
            try {
                application.shutdown(gameContext);
            } catch (DynamisShutdownException e) {
                LOG.log(System.Logger.Level.WARNING,
                        "Application shutdown warning: " + e.getMessage());
            } catch (Throwable t) {
                LOG.log(System.Logger.Level.WARNING,
                        "Application shutdown threw: " + t.getMessage(), t);
            }
        }

        state = WorldEngineState.STOPPED;
        LOG.log(System.Logger.Level.INFO, "WorldEngine stopped.");
    }
}
