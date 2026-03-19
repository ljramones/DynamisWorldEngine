package org.dynamisengine.worldengine.api;

import org.dynamisengine.ecs.api.world.World;
import org.dynamisengine.scenegraph.api.SceneGraph;

import java.util.Objects;

/**
 * The facade-level context passed to {@link WorldApplication} lifecycle hooks.
 *
 * Wraps the core {@link WorldContext} (assets, session, world, sceneGraph) and
 * adds timing and engine state information that game developers need.
 *
 * <h2>Naming Convention</h2>
 * <ul>
 *   <li>{@link WorldContext} — core orchestration context (lean, internal-facing)</li>
 *   <li>{@link GameContext} — facade-level context for game developers (enriched)</li>
 * </ul>
 *
 * Game developers use {@code GameContext}. Engine internals use {@code WorldContext}.
 * There is no ambiguity.
 *
 * @param world          the core world context (assets, session, ECS world, scene graph)
 * @param tick           current tick number
 * @param elapsedSeconds total elapsed time since engine start, in seconds
 * @param engineState    current lifecycle state
 */
public record GameContext(
        WorldContext world,
        long tick,
        double elapsedSeconds,
        WorldEngineState engineState,
        Runnable stopRequester) {

    public GameContext {
        Objects.requireNonNull(world, "world context must not be null");
        Objects.requireNonNull(engineState, "engineState must not be null");
    }

    /** Request the engine to stop after the current tick. */
    public void requestStop() {
        if (stopRequester != null) stopRequester.run();
    }

    /** Convenience: direct access to the ECS world. */
    public World ecsWorld() {
        return world.world();
    }

    /** Convenience: direct access to the scene graph. */
    public SceneGraph sceneGraph() {
        return world.sceneGraph();
    }
}
