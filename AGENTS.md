# Repository Guidelines

## Scope
DynamisWorldEngine is the runtime orchestrator and integration guide for composing Dynamis subsystems into a runnable world loop.

## Hard Boundaries
- WorldEngine wires subsystems; it does not re-implement them.
- No math types in this repository; Vectrix owns math.
- No renderer ownership here; LightEngine remains the renderer.
- Session data persists `AssetId` values and ECS snapshots.
- SceneGraph is a projection of runtime world state, not source-of-truth simulation data.

## Module Layout
- `world-api/`: orchestration contracts and lifecycle interfaces.
- `world-runtime/`: default runtime bootstrap/tick/project implementations.
- `world-samples/`: headless sample integrations and smoke tests.
- `docs/`: architecture and integration guidance.

## Build and Test
- Java `25` with preview enabled.
- `mvn validate` checks project structure.
- `mvn test` executes full reactor tests.

## Contribution Flow
- Keep changes phase-scoped and test-backed.
- Prefer deterministic headless samples for integration coverage.
- Commit after each phase; do not push unless explicitly requested.
