package org.dynamisworldengine.runtime.session;

import org.dynamiscontent.api.AssetManager;
import org.dynamiscontent.runtime.ContentRuntime;
import org.dynamiscontent.core.manifest.AssetManifestBuilder;
import org.dynamisecs.api.world.World;
import org.dynamisscenegraph.api.SceneGraph;
import org.dynamisscenegraph.core.DefaultSceneGraph;
import org.dynamissession.api.SessionManager;
import org.dynamissession.api.codec.CodecRegistry;
import org.dynamissession.core.codec.DefaultCodecRegistry;
import org.dynamissession.runtime.DefaultSessionManager;
import org.dynamisworldengine.api.WorldContext;
import org.dynamisworldengine.api.config.WorldConfig;
import org.dynamisworldengine.api.lifecycle.WorldBootstrapper;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

public final class DefaultWorldBootstrapper implements WorldBootstrapper {

    private final AssetManager assets;
    private final SessionManager session;
    private final CodecRegistry codecRegistry;
    private final Supplier<SceneGraph> sceneGraphFactory;

    public DefaultWorldBootstrapper() {
        this(defaultAssets(), new DefaultSessionManager(), new DefaultCodecRegistry(), DefaultSceneGraph::new);
    }

    public DefaultWorldBootstrapper(CodecRegistry codecRegistry) {
        this(defaultAssets(), new DefaultSessionManager(), codecRegistry, DefaultSceneGraph::new);
    }

    public DefaultWorldBootstrapper(
            AssetManager assets,
            SessionManager session,
            CodecRegistry codecRegistry,
            Supplier<SceneGraph> sceneGraphFactory
    ) {
        this.assets = Objects.requireNonNull(assets, "assets");
        this.session = Objects.requireNonNull(session, "session");
        this.codecRegistry = Objects.requireNonNull(codecRegistry, "codecRegistry");
        this.sceneGraphFactory = Objects.requireNonNull(sceneGraphFactory, "sceneGraphFactory");
    }

    @Override
    public WorldContext newGame(WorldConfig config) {
        Objects.requireNonNull(config, "config");
        World world = session.newGame();
        return new WorldContext(assets, session, world, sceneGraphFactory.get());
    }

    @Override
    public WorldContext loadGame(WorldConfig config, Path slotFile) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(slotFile, "slotFile");

        World world = session.load(slotFile, codecRegistry);
        return new WorldContext(assets, session, world, sceneGraphFactory.get());
    }

    private static AssetManager defaultAssets() {
        return ContentRuntime.builder()
                .manifest(new AssetManifestBuilder().build())
                .build()
                .assets();
    }
}
