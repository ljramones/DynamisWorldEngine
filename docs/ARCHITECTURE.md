# Architecture

DynamisWorldEngine provides thin orchestration over existing subsystems.

Core responsibilities:
- Build a world context from content, session, ECS, and scene graph services.
- Enforce runtime order of operations for bootstrap, tick, projection, and extraction.
- Keep contracts in `world-api` and implementation details in `world-runtime`.

This repository intentionally avoids re-implementing domain logic owned by peer engines.
