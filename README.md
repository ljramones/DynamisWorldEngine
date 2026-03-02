# DynamisWorldEngine

The top-level orchestrator for the Dynamis game engine ecosystem. DynamisWorldEngine is the single integration point a game developer instantiates to run a complete game world — it owns the tick loop, initializes all subsystems in dependency order, routes input through DynamisScripting and DynamisAI, and drives the full simulation pipeline per tick.

---

## Overview

The Dynamis ecosystem is a collection of focused, independently usable libraries. DynamisWorldEngine is the component that wires them together into a running game. It is not a library — it is a runtime. A game project depends on DynamisWorldEngine and nothing else; all other Dynamis components are pulled in transitively.

```
Your Game
    └── DynamisWorldEngine
            ├── DynamisScripting ──── DynamisExpression
            ├── DynamisAI
            ├── DynamisSession ─────── DynamisContent
            ├── DynamisSceneGraph
            ├── DynamisPhysics ─────── DynamisCollision
            │                   └──── Vectrix
            ├── DynamisLightEngine ─── DynamisGPU
            │                   └──── MeshForge
            ├── DynamisAudio
            ├── DynamisVFX
            ├── DynamisTerrain ─────── FastNoiseNouveau
            ├── DynamisSky
            ├── Animus
            ├── DynamisUI
            └── DynamisInput
```

---

## Responsibilities

**Tick loop ownership** — DynamisWorldEngine drives the main game loop at a fixed simulation rate. Each tick dispatches to physics, scripting, AI, scene graph updates, and rendering in the correct order.

**Subsystem initialization** — On startup, DynamisWorldEngine initializes all registered subsystems in dependency order. DynamisContent loads first. DynamisSession initializes against the loaded content. Rendering, physics, audio, and scripting initialize against the live session.

**Input routing** — DynamisInput events are captured each tick and routed to DynamisUI (for menu/HUD interaction), DynamisScripting (for trigger evaluation), and DynamisAI (for agent perception).

**Session lifecycle** — DynamisWorldEngine manages the transitions between application states: startup, main menu, session load, active play, save, and shutdown. It delegates world state persistence to DynamisSession and asset management to DynamisContent.

**Subsystem coordination** — Physics results feed DynamisSceneGraph. SceneGraph transforms feed DynamisLightEngine and DynamisAudio. DynamisScripting and DynamisAI read from DynamisSession and publish consequences back through Oracle. DynamisWorldEngine owns the coordination order.

---

## Ecosystem Components

| Component | Role |
|---|---|
| **Vectrix** | SIMD-optimized math kernel. Foundation for physics and geometry. |
| **DynamisGPU** | GPU utilities and abstraction layer. |
| **FastNoiseNouveau** | High-performance procedural noise generation. |
| **DynamisInput** | Keyboard, mouse, gamepad, and touch input. |
| **MeshForge** | Mesh processing and authoring. |
| **DynamisCollision** | Collision detection. |
| **DynamisAudio** | Spatial audio processing and playback. |
| **DynamisLightEngine** | Rendering for Vulkan and OpenGL. |
| **DynamisPhysics** | Dual ode4j and Jolt physics engine. |
| **DynamisVFX** | Particle simulation (Niagara-style). |
| **DynamisTerrain** | Procedural terrain generation. |
| **DynamisSky** | Sky and atmosphere generation. |
| **Animus** | Animation system. |
| **DynamisContent** | Asset pipeline — loads, caches, and serves all authored resources. |
| **DynamisSession** | Live world state, save/load serialization, scene transitions. |
| **DynamisSceneGraph** | Spatial and hierarchical scene representation. |
| **DynamisExpression** | High-performance expression engine for predicate evaluation. |
| **DynamisScripting** | Rules engine — Oracle, Chronicler, and DynamisAI scripting layer. |
| **DynamisAI** | Complex AI and LLM-driven agent intelligence. |
| **DynamisUI** | In-game UI, HUD, and menu systems. |

---

## Requirements

- Java 25+
- Vulkan 1.3+ or OpenGL 4.6 (for DynamisLightEngine)

---

## License

Apache 2.0 — see [LICENSE](LICENSE) for details.
