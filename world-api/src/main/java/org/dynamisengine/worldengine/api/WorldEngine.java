package org.dynamisengine.worldengine.api;

import org.dynamisengine.worldengine.api.config.WorldConfig;

import java.util.ServiceLoader;

/**
 * Main entry point for the Dynamis World Engine.
 *
 * <h2>Easy Path</h2>
 * <pre>{@code
 * WorldEngine.run(new MyGame());
 * }</pre>
 *
 * <h2>With Configuration</h2>
 * <pre>{@code
 * WorldEngine.run(new MyGame(), new WorldConfig("1.0.0", 1, 0L));
 * }</pre>
 *
 * <h2>Advanced Builder</h2>
 * <pre>{@code
 * WorldEngine.builder()
 *     .application(new MyGame())
 *     .tickRate(120)
 *     .run();
 * }</pre>
 *
 * This is the friendly facade over the engine's internal orchestration core.
 * Game developers interact with this class and {@link WorldApplication}.
 * They do not need to understand bootstrappers, tick runners, or projectors.
 */
public final class WorldEngine {

    private WorldEngine() {} // static utility

    /**
     * Run a game with all defaults. Blocks until the engine stops.
     *
     * This is the simplest possible way to start a Dynamis game:
     * <pre>{@code
     * WorldEngine.run(new MyGame());
     * }</pre>
     *
     * @param app the game application
     */
    public static void run(WorldApplication app) {
        builder().application(app).run();
    }

    /**
     * Run a game with a specific configuration. Blocks until the engine stops.
     *
     * @param app    the game application
     * @param config world configuration (version, save format, initial tick)
     */
    public static void run(WorldApplication app, WorldConfig config) {
        builder().application(app).config(config).run();
    }

    /**
     * Create a builder for advanced configuration.
     *
     * @return a new builder with excellent defaults
     */
    public static WorldEngineBuilder builder() {
        return new WorldEngineBuilder();
    }

    // -- Internal: handle creation via runtime module -----------------------

    /**
     * Create a WorldEngineHandle from a builder. Called by WorldEngineBuilder.build().
     *
     * Resolves the runtime implementation via ServiceLoader. If no provider is found,
     * throws a clear error message.
     */
    static WorldEngineHandle createHandle(WorldEngineBuilder builder) {
        ServiceLoader<WorldEngineHandleFactory> loader =
                ServiceLoader.load(WorldEngineHandleFactory.class);

        WorldEngineHandleFactory factory = loader.findFirst().orElseThrow(
                () -> new IllegalStateException(
                        "No WorldEngine runtime found. Ensure dynamis-worldengine-runtime " +
                        "is on the module path or classpath."));

        return factory.create(builder);
    }

    /**
     * SPI for runtime implementations to provide WorldEngineHandle instances.
     * Not intended for game developer use.
     */
    public interface WorldEngineHandleFactory {
        WorldEngineHandle create(WorldEngineBuilder builder);
    }
}
