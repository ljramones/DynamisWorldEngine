# DynamisWorldEngine Public API Plan

Date: 2026-03-18
Status: PROPOSED — defines developer-facing types and ergonomics

---

# 1. Design Target

A game developer writes:

```java
public final class MyGame implements WorldApplication {

    @Override
    public void initialize(WorldContext context) {
        // Create entities, load content, set up initial state
    }

    @Override
    public void update(WorldContext context, float deltaSeconds) {
        // Game logic, input reading, state updates
    }

    @Override
    public void shutdown(WorldContext context) {
        // Cleanup
    }
}
```

And launches with:

```java
WorldEngine.run(new MyGame());
```

That's the entire minimum viable game.

---

# 2. Public Types

## 2.1 WorldApplication (interface)

The user-implemented game contract.

```java
public interface WorldApplication {

    /** Called once after all subsystems are initialized. WorldContext is fully populated. */
    void initialize(WorldContext context);

    /**
     * Called once per tick during the run loop.
     * @param context the enriched world context (includes timing, input, etc.)
     * @param deltaSeconds time since last tick in seconds
     */
    void update(WorldContext context, float deltaSeconds);

    /** Called once before shutdown. Last chance to save state or release resources. */
    void shutdown(WorldContext context);

    /** Optional: called when the engine enters a paused state. Default no-op. */
    default void onPause(WorldContext context) {}

    /** Optional: called when the engine resumes from pause. Default no-op. */
    default void onResume(WorldContext context) {}
}
```

## 2.2 WorldEngine (class)

The main entry point. Two paths:

```java
public final class WorldEngine {

    /** Easy path: run with defaults. */
    public static void run(WorldApplication app) { ... }

    /** Easy path with config: run with custom configuration. */
    public static void run(WorldApplication app, WorldConfig config) { ... }

    /** Advanced path: full builder. */
    public static WorldEngineBuilder builder() { ... }
}
```

## 2.3 WorldEngineBuilder (class)

Configuration path for power users:

```java
public final class WorldEngineBuilder {

    /** Set the application to run. Required. */
    public WorldEngineBuilder application(WorldApplication app) { ... }

    /** Set world configuration (version, save format, initial tick). */
    public WorldEngineBuilder config(WorldConfig config) { ... }

    /** Override the default bootstrapper. */
    public WorldEngineBuilder bootstrapper(WorldBootstrapper bootstrapper) { ... }

    /** Override the default tick runner. */
    public WorldEngineBuilder tickRunner(WorldTickRunner runner) { ... }

    /** Override the default projector. */
    public WorldEngineBuilder projector(WorldProjector projector) { ... }

    /** Run in headless mode (no window, no renderer). */
    public WorldEngineBuilder headless() { ... }

    /** Run in test mode (NullAudioBackend, FakeWindow, deterministic). */
    public WorldEngineBuilder testMode() { ... }

    /** Set a custom tick rate in Hz (default: 60). */
    public WorldEngineBuilder tickRate(int hz) { ... }

    /** Build and run. */
    public void run() { ... }

    /** Build without running (for testing or advanced control). */
    public WorldEngineHandle build() { ... }
}
```

## 2.4 WorldEngineState (enum)

```java
public enum WorldEngineState {
    CREATED,        // Builder constructed, not yet initialized
    INITIALIZING,   // Subsystems starting up
    RUNNING,        // Tick loop active
    PAUSED,         // Tick loop suspended
    FAULTED,        // Non-recoverable error
    STOPPING,       // Shutting down subsystems
    STOPPED         // All resources released, terminal
}
```

## 2.5 WorldEngineHandle (interface)

For advanced users who need programmatic control:

```java
public interface WorldEngineHandle {
    void start();
    void pause();
    void resume();
    void stop();
    WorldEngineState state();
    WorldContext context();
}
```

---

# 3. What Game Developers DON'T Need to Touch

| Core Type | Why Hidden |
|-----------|-----------|
| WorldBootstrapper | Facade creates and calls it internally |
| WorldTickRunner | Facade creates and calls it internally |
| WorldProjector | Facade creates and calls it internally |
| DefaultWorldBootstrapper | Implementation detail |
| DefaultWorldTickRunner | Implementation detail |
| DefaultWorldProjector | Implementation detail |

These remain accessible for power users who want to customize via the builder, but normal game developers never need to know they exist.

---

# 4. Enriched WorldContext

The existing `WorldContext(assets, session, world, sceneGraph)` is orchestration-focused. For the facade, game developers also need:

- Current tick number
- Delta time
- Elapsed time
- Current engine state
- Input frame access
- Audio service access (future)

**Approach:** Add accessor methods or a timing record to WorldContext, or provide them through the `update()` method parameters.

The simplest ergonomic choice: `deltaSeconds` is a parameter of `update()`, and `WorldContext` gains a `tick()` accessor:

```java
// WorldContext evolution (backward compatible)
public record WorldContext(
    AssetManager assets,
    SessionManager session,
    World world,
    SceneGraph sceneGraph,
    long tick              // added: current tick number
) { ... }
```

Or keep WorldContext as-is and pass timing through the update signature.

---

# 5. Default Behavior

When a developer calls `WorldEngine.run(new MyGame())`:

1. WorldConfig created with sensible defaults ("1.0.0", saveFormat=1, initialTick=0)
2. DefaultWorldBootstrapper creates a new game → WorldContext
3. Engine enters INITIALIZING, starts subsystems (window, input, audio if available)
4. Calls `app.initialize(context)`
5. Engine enters RUNNING
6. Tick loop: `tickRunner.runTick(ctx, tick)` → `app.update(ctx, deltaSeconds)`
7. On window close or app signal: engine enters STOPPING
8. Calls `app.shutdown(context)`
9. Subsystems shut down in reverse order
10. Engine enters STOPPED

---

# 6. Error Handling

| Failure | Behavior |
|---------|----------|
| DynamisInitException during startup | Log error, transition to FAULTED, call app.shutdown if possible |
| DynamisTickException during run loop | Log warning, continue (recoverable) |
| Uncaught exception in app.update() | Log error, transition to FAULTED, begin shutdown |
| DynamisShutdownException | Log warning, continue shutdown sequence |
| Subsystem startup failure | Log, attempt fallback (NullAudioBackend), or FAULTED if critical |

Error messages should be human-readable:

```
[WorldEngine] Audio initialization failed: CoreAudio not available — using NullAudioBackend
[WorldEngine] Input runtime started: keyboard + mouse (no gamepad detected)
[WorldEngine] Engine running at 60Hz
```

Not:

```
java.lang.RuntimeException: org.dynamisengine.audio.api.device.AudioDeviceException...
```
