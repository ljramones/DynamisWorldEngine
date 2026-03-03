# Order Of Operations

Tick loop baseline order:
1. Begin world tick.
2. Execute simulation systems.
3. Rebuild/apply scene projection.
4. Extract scene data for adapters.
5. End world tick.

This order is the canonical runtime contract for WorldEngine v1.
