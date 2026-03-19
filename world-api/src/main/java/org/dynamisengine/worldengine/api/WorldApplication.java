package org.dynamisengine.worldengine.api;

/**
 * The game developer's primary contract with the engine.
 *
 * Implement this interface to define your game's behavior. The engine calls
 * these methods at the appropriate lifecycle phases. You do not need to
 * understand subsystem internals, bootstrappers, or tick runners — the engine
 * handles all of that.
 *
 * <h2>Minimal Game</h2>
 * <pre>{@code
 * public final class MyGame implements WorldApplication {
 *     @Override
 *     public void initialize(GameContext context) {
 *         // Create entities, load content
 *     }
 *
 *     @Override
 *     public void update(GameContext context, float deltaSeconds) {
 *         // Game logic
 *     }
 *
 *     @Override
 *     public void shutdown(GameContext context) {
 *         // Cleanup
 *     }
 * }
 * }</pre>
 *
 * Then launch with: {@code WorldEngine.run(new MyGame());}
 */
public interface WorldApplication {

    /**
     * Called once after all subsystems are initialized.
     *
     * The {@link GameContext} is fully populated: world, assets, session,
     * scene graph, and timing are all available. Create initial entities,
     * load content, and set up game state here.
     *
     * @param context the game context with all engine services available
     */
    void initialize(GameContext context);

    /**
     * Called once per tick during the run loop.
     *
     * Read input, update game state, move entities, evaluate game rules.
     * The engine has already advanced the world tick and projected
     * ECS state to the scene graph before this method is called.
     *
     * @param context      the game context (includes current tick, timing)
     * @param deltaSeconds time since last tick in seconds
     */
    void update(GameContext context, float deltaSeconds);

    /**
     * Called once before shutdown. Last chance to save state or release resources.
     *
     * @param context the game context
     */
    void shutdown(GameContext context);

    /**
     * Called when the engine enters a paused state.
     * Default implementation does nothing.
     *
     * @param context the game context
     */
    default void onPause(GameContext context) {}

    /**
     * Called when the engine resumes from pause.
     * Default implementation does nothing.
     *
     * @param context the game context
     */
    default void onResume(GameContext context) {}
}
