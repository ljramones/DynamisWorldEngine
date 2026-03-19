# DynamisWorldEngine Authority Model

Date: 2026-03-18
Status: PROPOSED — defines facade authority boundaries

---

# 1. What WorldEngine Facade Owns

| Domain | Authority |
|--------|-----------|
| Engine startup/shutdown sequencing | Orders subsystem initialization and teardown |
| Run loop orchestration | Drives tick cadence, calls application hooks |
| Subsystem lifecycle coordination | Starts/stops registered subsystems in dependency order |
| Aggregate engine state | Maintains CREATED → RUNNING → FAULTED → STOPPED state machine |
| Default subsystem selection | Picks sane defaults for window, input, audio, rendering |
| Failure escalation | Catches subsystem exceptions, reports diagnostics, transitions state |
| Developer-facing API surface | WorldEngine, WorldEngineBuilder, WorldApplication, enriched context |

---

# 2. What WorldEngine Facade Does NOT Own

| Domain | Owner | Reason |
|--------|-------|--------|
| ECS world model | DynamisECS | WorldEngine reads/writes world via WorldContext, doesn't define it |
| Scene graph structure | DynamisSceneGraph | Projection maps ECS → scene, but scene is its own subsystem |
| Content/asset management | DynamisContent / DynamisAssetPipeline | WorldContext carries AssetManager, doesn't implement it |
| Session/save persistence | DynamisSession | WorldContext carries SessionManager |
| Audio backend behavior | DynamisAudio | Facade may start AudioDeviceManager, doesn't control its internals |
| Input grammar/mapping | DynamisInput | Facade may start InputRuntime, exposes InputFrame, doesn't own semantics |
| Renderer implementation | DynamisLightEngine | Facade may wire renderer, doesn't own draw calls |
| Gameplay logic | Game application | User implements WorldApplication, WorldEngine just calls the hooks |
| AI / Scripting / Physics | Layer 5 subsystems | These are simulation systems, not orchestration |

---

# 3. Facade vs Core Authority Split

| Concern | Facade (new) | Core (existing) |
|---------|-------------|-----------------|
| "How do I start a game?" | WorldEngine / WorldEngineBuilder | — |
| "What are my update hooks?" | WorldApplication interface | — |
| "How does a world get created?" | Delegates to... | WorldBootstrapper |
| "What happens each tick?" | Calls... | WorldTickRunner.runTick() |
| "How do entities become visible?" | — | WorldProjector.project() |
| "What's the engine state?" | WorldEngineState machine | — |
| "What if init fails?" | Catches, reports, transitions | DynamisInitException |
| "What if a tick fails?" | Catches, decides recovery | DynamisTickException |

---

# 4. Subsystem Integration Authority

The facade is the **wiring authority** — it decides which subsystems to start and in what order. But it does not own subsystem internals.

```
WorldEngine facade
    ├── discovers/configures: Window, Input, Audio, Renderer
    ├── starts them in correct order
    ├── provides references via enriched context
    ├── monitors their health state
    └── shuts them down in reverse order

Each subsystem
    ├── owns its own backend selection
    ├── owns its own lifecycle details
    ├── owns its own telemetry
    └── reports failures to the facade via exceptions
```

---

# 5. What Game Developers Should See

| Visible | Hidden |
|---------|--------|
| WorldEngine | DefaultWorldBootstrapper |
| WorldEngineBuilder | DefaultWorldTickRunner |
| WorldApplication | DefaultWorldProjector |
| WorldContext (enriched) | Subsystem registry internals |
| WorldEngineState | Dependency ordering logic |
| Lifecycle hooks (init, update, shutdown) | Tick runner wiring |
| Input/Audio/Timing access | Backend SPI details |
| Good error messages | Raw exception chains |
