# Integration Guide

Minimal integration flow:
1. Construct a `WorldConfig`.
2. Bootstrap a `WorldContext` (new game or load slot).
3. Run ticks through `WorldTickRunner`.
4. Project ECS state into SceneGraph each tick.
5. Extract scene output for downstream rendering systems.

Use `world-samples` for executable reference flows and smoke tests.
