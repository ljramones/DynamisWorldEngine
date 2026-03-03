package org.dynamisworldengine.api;

import org.dynamiscontent.api.AssetManager;
import org.dynamisecs.api.world.World;
import org.dynamisscenegraph.api.SceneGraph;
import org.dynamissession.api.SessionManager;

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
