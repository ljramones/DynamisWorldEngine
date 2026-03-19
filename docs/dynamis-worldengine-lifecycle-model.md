# DynamisWorldEngine Lifecycle Model

Date: 2026-03-18
Status: PROPOSED — defines facade lifecycle states and transitions

---

# 1. State Machine

```
CREATED ──initialize()──→ INITIALIZING ──success──→ RUNNING
                                        ──failure──→ FAULTED

RUNNING ──pause()──→ PAUSED ──resume()──→ RUNNING
RUNNING ──tick exception (non-recoverable)──→ FAULTED
RUNNING ──stop() / window close──→ STOPPING ──complete──→ STOPPED

PAUSED ──stop()──→ STOPPING ──complete──→ STOPPED

FAULTED ──stop()──→ STOPPING ──complete──→ STOPPED

STOPPING ──complete──→ STOPPED
```

STOPPED is terminal.

---

# 2. States

| State | Description | Tick loop? | App hooks? |
|-------|-------------|-----------|------------|
| CREATED | Builder constructed, nothing started | No | No |
| INITIALIZING | Subsystems starting, bootstrapper running | No | No |
| RUNNING | Tick loop active, app receiving update() calls | Yes | update() |
| PAUSED | Tick loop suspended, app notified | No | onPause/onResume |
| FAULTED | Non-recoverable error, awaiting shutdown | No | No |
| STOPPING | Shutdown in progress, subsystems closing | No | shutdown() |
| STOPPED | All resources released | No | No |

---

# 3. Transition Triggers

| Transition | Triggered By | Thread |
|-----------|-------------|--------|
| CREATED → INITIALIZING | `WorldEngine.run()` or `handle.start()` | Calling thread |
| INITIALIZING → RUNNING | All subsystems initialized, app.initialize() completed | Engine thread |
| INITIALIZING → FAULTED | DynamisInitException or uncaught exception | Engine thread |
| RUNNING → PAUSED | `handle.pause()` or OS-level pause event | Engine/OS thread |
| PAUSED → RUNNING | `handle.resume()` or OS-level resume | Engine/OS thread |
| RUNNING → FAULTED | Non-recoverable exception during tick | Engine thread |
| RUNNING → STOPPING | `handle.stop()`, window close, or app exit signal | Engine/OS thread |
| PAUSED → STOPPING | `handle.stop()` | Engine thread |
| FAULTED → STOPPING | `handle.stop()` | Engine thread |
| STOPPING → STOPPED | All subsystems shut down, app.shutdown() completed | Engine thread |

---

# 4. What Happens in Each State

## CREATED
- Builder has been constructed
- No subsystems started
- No resources allocated
- Waiting for `run()` or `start()`

## INITIALIZING
- Subsystems start in dependency order:
  1. Core services (content, session, ECS)
  2. Window system
  3. Input runtime
  4. Audio runtime (with NullAudioBackend fallback)
  5. Renderer (if not headless)
- WorldBootstrapper creates WorldContext (newGame or loadGame)
- `app.initialize(context)` called
- If any critical subsystem fails: → FAULTED

## RUNNING
- Fixed-rate tick loop active (default 60Hz)
- Each tick:
  1. Poll window events
  2. Process input → produce InputFrame
  3. `WorldTickRunner.runTick(context, tick)` — ECS simulation + projection
  4. `app.update(context, deltaSeconds)` — game logic
  5. Render frame (if not headless)
- Recoverable tick exceptions (DynamisTickException): logged, tick skipped, continue
- Non-recoverable exceptions: → FAULTED

## PAUSED
- Tick loop suspended
- `app.onPause(context)` called on entry
- `app.onResume(context)` called on exit
- Subsystems remain alive but not ticking
- Useful for: OS backgrounding, debug pause, menu overlay

## FAULTED
- Engine has encountered a non-recoverable error
- Tick loop stopped
- Subsystems may be in inconsistent state
- Only valid transition: → STOPPING → STOPPED
- Diagnostic information available via telemetry/logging

## STOPPING
- `app.shutdown(context)` called
- Subsystems shut down in reverse dependency order:
  1. Renderer
  2. Audio runtime
  3. Input runtime
  4. Window system
  5. Core services
- DynamisShutdownException caught and logged (non-fatal, continues shutdown)

## STOPPED
- All resources released
- Terminal state
- Engine object is no longer usable

---

# 5. Relationship to Existing Core Lifecycle

The existing core has implicit lifecycle through method calls:

```
existing: bootstrapper.newGame(config) → context → tickRunner.runTick(ctx, tick)
```

The facade wraps this with explicit state transitions and error handling:

```
facade: CREATED → INITIALIZING (bootstrapper) → RUNNING (tick loop with runner) → STOPPING → STOPPED
```

The existing `DynamisInitException`, `DynamisTickException`, and `DynamisShutdownException` map directly into the facade's state transitions:

| Exception | Facade Response |
|-----------|----------------|
| DynamisInitException (non-recoverable) | INITIALIZING → FAULTED |
| DynamisTickException (recoverable) | Log, continue RUNNING |
| DynamisTickException (repeated/critical) | RUNNING → FAULTED |
| DynamisShutdownException (recoverable) | Log, continue STOPPING |

---

# 6. Subsystem Initialization Order

Based on the existing engine dependency DAG:

```
1. DynamisCore (always present)
2. DynamisContent → DynamisSession (content/session bootstrap)
3. DynamisECS (world creation via bootstrapper)
4. DynamisSceneGraph (scene creation)
5. DynamisWindow (platform window)
6. DynamisInput (input runtime, depends on window events)
7. DynamisAudio (audio runtime, independent)
8. DynamisLightEngine (renderer, depends on window + scene)
```

Shutdown is reverse order. Optional subsystems (audio, renderer) gracefully degrade if unavailable.

---

# 7. Tick Loop Detail

```java
while (state == RUNNING) {
    long tickStart = System.nanoTime();

    // 1. Poll platform events
    windowSystem.pollEvents();

    // 2. Check for close/pause signals
    if (windowCloseRequested) { transition(STOPPING); break; }

    // 3. Process input
    inputRuntime.feed(windowEvents, tick);
    InputFrame inputFrame = inputRuntime.snapshot(tick);

    // 4. Run world tick (ECS simulation + projection)
    try {
        tickRunner.runTick(context, tick);
    } catch (DynamisTickException e) {
        log.warn("Tick {} failed: {}", tick, e.getMessage());
        // recoverable — continue
    }

    // 5. Application update
    app.update(context, deltaSeconds);

    // 6. Render (if not headless)
    if (renderer != null) renderer.render(sceneGraph);

    // 7. Pace to target tick rate
    tick++;
    sleepUntilNextTick(tickStart, targetTickNanos);
}
```

---

# 8. Illegal Transitions

Any transition not listed in section 3 is illegal and throws `IllegalStateException`:

- CREATED → RUNNING (must go through INITIALIZING)
- STOPPED → anything (terminal)
- FAULTED → RUNNING (must shutdown first; no auto-recovery)
- INITIALIZING → PAUSED (not yet running)
