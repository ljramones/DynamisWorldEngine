package org.dynamisengine.worldengine.api;

import org.dynamisengine.content.api.AssetManager;
import org.dynamisengine.ecs.api.world.World;
import org.dynamisengine.scenegraph.api.SceneGraph;
import org.dynamisengine.session.api.SessionManager;

import java.util.Objects;

public record WorldContext(
        AssetManager assets,
        SessionManager session,
        World world,
        SceneGraph sceneGraph
) {
    public WorldContext {
        Objects.requireNonNull(assets, "assets");
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(sceneGraph, "sceneGraph");
    }
}
