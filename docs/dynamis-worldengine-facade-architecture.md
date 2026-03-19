# DynamisWorldEngine Facade Architecture

Date: 2026-03-18
Status: PROPOSED — defines developer-facing layer over existing orchestration core

---

# 1. Purpose

DynamisWorldEngine already has a **mature orchestration core**: WorldContext, WorldBootstrapper, WorldTickRunner, WorldProjector, lifecycle exceptions, ECS-to-SceneGraph projection, and content/session integration.

What it lacks is a **developer-friendly facade** — the entry point that makes game developers productive without requiring them to understand orchestration internals.

This document defines that facade layer. The goal is not to redesign the core — it is to **wrap, clarify, and elevate** it into a proper engine entry point.

---

# 2. Two-Layer Model

```
┌──────────────────────────────────────────────────┐
│  Developer-Facing Facade (NEW)                    │
│  WorldEngine, WorldEngineBuilder, WorldApplication│
│  WorldEngineState, enriched WorldContext           │
├──────────────────────────────────────────────────┤
│  Orchestration Core (EXISTS)                      │
│  WorldBootstrapper, WorldTickRunner, WorldProjector│
│  WorldConfig, DefaultWorldBootstrapper,           │
│  DefaultWorldTickRunner, DefaultWorldProjector    │
│  DynamisInitException, DynamisTickException, etc. │
└──────────────────────────────────────────────────┘
```

The facade delegates to the core. The core does not know about the facade.

---

# 3. What Exists (Core Layer — Keep As-Is)

These types are the proven orchestration spine. They stay where they are.

| Type | Module | Role | Visibility |
|------|--------|------|------------|
| `WorldContext` | world-api | Carries assets, session, world, sceneGraph | Public (used by facade and advanced users) |
| `WorldConfig` | world-api | Build version, save format, initial tick | Public |
| `WorldBootstrapper` | world-api | Interface: newGame / loadGame | Advanced / internal |
| `WorldTickRunner` | world-api | Interface: runTick(ctx, tick) | Advanced / internal |
| `WorldProjector` | world-api | Interface: project(ctx) | Advanced / internal |
| `DynamisInitException` | world-api | Non-recoverable init failure | Public (facade catches and reports) |
| `DynamisTickException` | world-api | Recoverable tick failure | Public |
| `DynamisShutdownException` | world-api | Recoverable shutdown failure | Public |
| `DefaultWorldBootstrapper` | world-runtime | Default bootstrapper impl | Internal |
| `DefaultWorldTickRunner` | world-runtime | Default tick runner | Internal |
| `DefaultWorldProjector` | world-runtime | ECS→SceneGraph projection | Internal |

These types do NOT need to be renamed, moved, or restructured. The facade wraps them.

---

# 4. What's New (Facade Layer)

| Type | Module | Role |
|------|--------|------|
| `WorldEngine` | world-api | Main entry point. Static `run()` and `builder()` methods. |
| `WorldEngineBuilder` | world-api | Configuration path. Defaults to sane setup. Advanced users customize. |
| `WorldApplication` | world-api | User-implemented game contract: initialize, update, shutdown. |
| `WorldEngineState` | world-api | Lifecycle state enum: CREATED, INITIALIZING, RUNNING, PAUSED, FAULTED, STOPPING, STOPPED. |

---

# 5. How the Facade Delegates to the Core

```
WorldEngine.run(myGame)
    ↓
WorldEngineBuilder (resolve config, select subsystems)
    ↓
WorldBootstrapper.newGame(config) → WorldContext
    ↓
WorldApplication.initialize(context)
    ↓
Loop:
    WorldTickRunner.runTick(context, tick)
    WorldApplication.update(context, deltaSeconds)
    ↓
WorldApplication.shutdown(context)
    ↓
Cleanup
```

The facade:
- Constructs the `WorldBootstrapper` (default or custom)
- Calls `newGame()` or `loadGame()` to get a `WorldContext`
- Manages the run loop (calling `WorldTickRunner.runTick` each iteration)
- Exposes `WorldContext` to the application through lifecycle hooks
- Handles exceptions per the lifecycle model
- Manages state transitions

---

# 6. WorldContext Evolution

The existing `WorldContext(assets, session, world, sceneGraph)` carries core orchestration resources. For the facade, game developers also need access to:

- Input (InputFrame snapshots)
- Audio (service access)
- Timing (tick number, delta seconds, elapsed time)
- Engine state

Rather than bloating `WorldContext`, the facade can provide an **enriched context** that wraps the existing one:

```java
// Option A: Extend WorldContext with a richer facade-level context
public record GameContext(
    WorldContext world,      // existing core context
    InputFrame input,        // current input snapshot
    TimingInfo timing,       // tick, delta, elapsed
    WorldEngineState state   // current engine state
) {}

// Option B: WorldContext gains optional service accessors via the facade
// (less clean, risks junk drawer)
```

**Recommendation:** Option A — keep `WorldContext` lean (core orchestration), add a `GameContext` or enriched context at the facade level that wraps it plus additional services.

---

# 7. Module Impact

The facade types live in `world-api` (public contracts). No new modules are needed.

```
world-api/
├── org.dynamisengine.worldengine.api/
│   ├── WorldContext.java          (exists)
│   ├── WorldEngine.java           (NEW)
│   ├── WorldEngineBuilder.java    (NEW)
│   ├── WorldApplication.java      (NEW)
│   ├── WorldEngineState.java      (NEW)
│   ├── config/
│   │   └── WorldConfig.java       (exists)
│   └── lifecycle/
│       ├── WorldBootstrapper.java (exists)
│       ├── WorldTickRunner.java   (exists)
│       ├── WorldProjector.java    (exists)
│       └── exceptions...          (exists)

world-runtime/
├── org.dynamisengine.worldengine.runtime/
│   ├── DefaultWorldEngine.java    (NEW — facade implementation)
│   ├── DefaultWorldTickRunner.java (exists)
│   ├── session/
│   │   └── DefaultWorldBootstrapper.java (exists)
│   └── projection/
│       └── DefaultWorldProjector.java (exists)
```

---

# 8. Design Principles

**P-1: Simple path first.**
`WorldEngine.run(new MyGame())` must work with zero configuration.

**P-2: Defaults are excellent.**
Default window, default input, default audio, default tick runner. A new developer gets a working runtime without knowing what a WorldBootstrapper is.

**P-3: Advanced path available.**
Power users can use `WorldEngine.builder()` to customize everything. They can also bypass the facade and use the core types directly.

**P-4: Facade does not replace core.**
The core orchestration types remain valid and usable independently. The facade is sugar on top, not a replacement.

**P-5: Failure messages are unusually good.**
"Audio backend failed to initialize; falling back to NullAudioBackend" — not stacktrace soup.

**P-6: WorldContext stays lean.**
The facade adds enriched context (timing, input, state) without polluting the existing WorldContext record.
