# DynamisWorldEngine Architecture Boundary Ratification Review

Date: 2026-03-09

## Intent and Scope

This is a boundary-ratification review for DynamisWorldEngine based on current repository code/docs.

This pass does not refactor code. It establishes strict ownership and dependency boundaries between world authority/orchestration and adjacent subsystem layers (ECS, SceneGraph, Session, LightEngine, Content).

## 1) Repo Overview (Grounded)

Repository shape:

- Multi-module Maven project:
  - `world-api`
  - `world-runtime`
  - `world-samples`

Implemented API/runtime surface:

- `world-api`
  - `WorldContext(AssetManager, SessionManager, World, SceneGraph)`
  - lifecycle contracts: `WorldBootstrapper`, `WorldTickRunner`, `WorldProjector`
  - `WorldConfig`
- `world-runtime`
  - `DefaultWorldBootstrapper` (constructs content/session/world/scene context)
  - `DefaultWorldTickRunner` (tick begin -> project -> tick end)
  - `DefaultWorldProjector` (ECS component projection into SceneGraph)
  - projection component keys/types under `runtime.projection.*`
- `world-samples`
  - integration adapters and sample flows for SceneGraph, LightEngine, Content, Input, Session, Window

Dependency shape (from module poms and imports):

- `world-api` depends on ECS API, SceneGraph API, Session API, Content API, DynamisCore.
- `world-runtime` depends on ECS core, SceneGraph core, Session runtime, Content runtime.
- `world-samples` depends on LightEngine API, Input modules, Content modules, Window modules.
- main `world-api`/`world-runtime` modules do not depend directly on LightEngine.

## 2) Strict Ownership Statement

### 2.1 What DynamisWorldEngine should exclusively own

DynamisWorldEngine should own **world-scope authority and orchestration**, including:

- world lifecycle bootstrap (new/load world context)
- world-scope tick sequencing contracts
- coordination boundaries between session, ECS world state, scene projection, and downstream consumers
- world-context composition and ownership transfer rules

### 2.2 What is appropriate for WorldEngine

Appropriate concerns:

- orchestration order at world scope
- boundary contracts for projection and tick runners
- world-level integration points that compose subsystem APIs

### 2.3 What DynamisWorldEngine must never own

DynamisWorldEngine must not own:

- ECS substrate internals (storage/query/indexing mechanics)
- SceneGraph substrate internals (hierarchy/transform/bounds engine)
- Session authority internals (save/load codec/runtime ownership)
- render planning/policy (LightEngine) or GPU execution (DynamisGPU)
- asset shaping/pipeline ownership (MeshForge/AssetPipeline/Content internals)
- scripting runtime/language ownership

## 3) Dependency Rules

### 3.1 Allowed dependencies for DynamisWorldEngine

- ECS/SceneGraph/Session/Content APIs as orchestration inputs
- runtime implementations of those subsystems in `world-runtime` default wiring
- LightEngine API only at sample/adapter edges unless a dedicated integration module is introduced

### 3.2 Forbidden dependencies for DynamisWorldEngine

- direct DynamisGPU execution plumbing in world core/runtime
- LightEngine render-policy internals in world core/runtime
- SceneGraph/ECS/session implementation internals in `world-api`
- feature-subsystem ownership code (AI/VFX/Sky/Terrain/etc.) inside world core contracts

### 3.3 Who may depend on DynamisWorldEngine

- host/application orchestration layer
- integration apps that need a world-loop spine
- test/sample harnesses for subsystem composition

Dependency direction intent:

- WorldEngine orchestrates ECS/SceneGraph/Session/Content; LightEngine consumes projected/extracted outputs; GPU execution remains below LightEngine.

## 4) Public vs Internal Boundary Assessment

### 4.1 Canonical public boundary

Public boundary should primarily be:

- `world-api` (`WorldContext`, lifecycle interfaces, config)

### 4.2 Internal/implementation areas

Internal by intent:

- `world-runtime` default bootstrap/tick/project implementations
- `world-samples` adapters/spikes/testing harnesses

### 4.3 Current boundary pressure points

1. `world-runtime` projection path currently hard-casts `SceneGraph` to `DefaultSceneGraph`, creating implementation coupling to SceneGraph core rather than API-only dependency.

2. `world-runtime` owns projection component keys/types (`demo.translation`, `demo.boundsSphere`, `demo.renderable`) under runtime packages. Those are integration/demo semantics rather than durable world-authority contracts.

3. `WorldContext` directly exposes subsystem objects (`AssetManager`, `SessionManager`, `World`, `SceneGraph`), which is practical but makes policy boundaries easy to bypass.

## 5) Policy Leakage / Overlap Findings

### 5.1 Major clean boundaries confirmed

- WorldEngine has a small code footprint centered on bootstrap/tick/project orchestration.
- LightEngine coupling is kept in `world-samples` adapters, not in `world-api`/`world-runtime`.
- Session and Content are consumed via their APIs/runtime builders rather than reimplemented.

### 5.2 Notable overlap/drift risks

1. **WorldEngine <-> SceneGraph overlap (runtime coupling)**  
`DefaultWorldProjector` requires `DefaultSceneGraph` (concrete type), tightening coupling where API-level projection should be preferred.

2. **WorldEngine <-> ECS overlap (component semantics in runtime)**  
Projection component keys and component record types in `world-runtime` are currently demo-oriented ECS schema decisions embedded in runtime package.

3. **WorldEngine <-> Session authority ambiguity (README overreach)**  
README positions WorldEngine as owning broad lifecycle/input routing/oracle-level orchestration not reflected in actual implemented modules. Docs overstate authority relative to code.

4. **WorldEngine <-> LightEngine integration boundary (samples-only, currently clean)**  
Adapters are correctly sample-scoped, but future movement of those adapters into `world-runtime` would blur render-planning boundaries.

5. **WorldEngine <-> Scripting/Event scope (not implemented yet, ambiguity)**  
No scripting/event orchestration code is present in `world-api`/`world-runtime`, despite README claims. This is an expectation-vs-implementation boundary risk.

## 6) Relationship Clarification

### 6.1 WorldEngine vs ECS

- ECS owns state substrate and query/storage semantics.
- WorldEngine owns sequencing around ECS tick windows and world-scope coordination.
- WorldEngine should not define long-term ECS domain schema in core runtime packages.

### 6.2 WorldEngine vs SceneGraph

- SceneGraph owns spatial substrate.
- WorldEngine owns projection orchestration from world state into SceneGraph.
- Projection should target SceneGraph contracts, minimizing concrete `DefaultSceneGraph` coupling in reusable runtime paths.

### 6.3 WorldEngine vs Session

- Session owns save/load/runtime session authority.
- WorldEngine consumes session services for world lifecycle entry points.
- WorldEngine should not absorb codec/persistence authority.

### 6.4 WorldEngine vs LightEngine

- WorldEngine should feed render-ready world outputs via adapters/integration seams.
- LightEngine owns render planning/policy/runtime rendering.
- WorldEngine core should avoid renderer API ownership beyond integration boundaries.

## 7) Ratification Result

**Ratified with constraints.**

Why:

- Core implemented behavior is small, focused, and orchestration-centered.
- Major responsibilities are expressed through thin lifecycle interfaces.
- Constraints are required due to concrete SceneGraph coupling, demo-schema projection types in runtime, and documentation overclaiming future orchestration ownership not yet present in code.

## 8) Strict Boundary Rules to Carry Forward

1. Keep `world-api` as the stable public contract; avoid exposing subsystem concrete internals there.
2. Keep `world-runtime` focused on orchestration glue, not subsystem-specific schema ownership.
3. Keep render integration adapters outside core runtime (samples/integration modules).
4. Keep session/content internals external; consume through their APIs/runtime boundaries.
5. Align README claims with implemented boundary reality to avoid accidental scope creep.

## 9) Recommended Next Step

Next deep review should be **DynamisSession**.

Reason:

- Session authority is the nearest high-risk overlap with WorldEngine world lifecycle ownership.
- Clarifying Session boundaries next will lock save/load authority and reduce world/session policy ambiguity before broader integration planning.
