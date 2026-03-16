package org.dynamisengine.worldengine.runtime.session;

import org.dynamisengine.content.api.AssetManager;
import org.dynamisengine.content.runtime.ContentRuntime;
import org.dynamisengine.content.core.manifest.AssetManifestBuilder;
import org.dynamisengine.scenegraph.api.SceneGraph;
import org.dynamisengine.scenegraph.core.DefaultSceneGraph;
import org.dynamisengine.session.api.SessionManager;
import org.dynamisengine.session.api.codec.CodecRegistry;
import org.dynamisengine.session.api.model.EcsSnapshot;
import org.dynamisengine.session.api.model.SaveGame;
import org.dynamisengine.session.api.model.SaveMetadata;
import org.dynamisengine.session.core.codec.DefaultCodecRegistry;
import org.dynamisengine.session.runtime.DefaultSessionManager;
import org.dynamisengine.worldengine.api.WorldContext;
import org.dynamisengine.worldengine.api.config.WorldConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultWorldBootstrapperTest {

    private static final WorldConfig CONFIG = new WorldConfig("1.0.0", 1, 0L);

    @Test
    void newGameCreatesValidContext() {
        var bootstrapper = createBootstrapper();
        WorldContext ctx = bootstrapper.newGame(CONFIG);

        assertNotNull(ctx.assets(), "assets should not be null");
        assertNotNull(ctx.session(), "session should not be null");
        assertNotNull(ctx.world(), "world should not be null");
        assertNotNull(ctx.sceneGraph(), "sceneGraph should not be null");
    }

    @Test
    void newGameWorldIsEmpty() {
        var bootstrapper = createBootstrapper();
        WorldContext ctx = bootstrapper.newGame(CONFIG);

        assertTrue(ctx.world().entities().isEmpty(), "New game world should have no entities");
    }

    @Test
    void newGameNullConfigIsRejected() {
        var bootstrapper = createBootstrapper();
        assertThrows(NullPointerException.class, () -> bootstrapper.newGame(null));
    }

    @Test
    void loadGameNullConfigIsRejected() {
        var bootstrapper = createBootstrapper();
        assertThrows(NullPointerException.class, () -> bootstrapper.loadGame(null, Path.of("slot.dat")));
    }

    @Test
    void loadGameNullSlotFileIsRejected() {
        var bootstrapper = createBootstrapper();
        assertThrows(NullPointerException.class, () -> bootstrapper.loadGame(CONFIG, null));
    }

    @Test
    void loadGameRestoresState() throws Exception {
        CodecRegistry registry = new DefaultCodecRegistry();
        SessionManager session = new DefaultSessionManager();
        AssetManager assets = defaultAssets();
        var bootstrapper = new DefaultWorldBootstrapper(assets, session, registry, DefaultSceneGraph::new);

        // Create a game, add an entity, save it
        WorldContext created = bootstrapper.newGame(CONFIG);
        created.world().createEntity();

        Path slot = java.nio.file.Files.createTempFile("test-slot", ".dat");
        slot.toFile().deleteOnExit();

        var save = new SaveGame(
                new SaveMetadata(CONFIG.saveFormatVersion(), CONFIG.buildVersion(),
                        System.currentTimeMillis(), CONFIG.initialTick(), "test"),
                new EcsSnapshot(List.of()));
        session.save(slot, created.world(), save, registry);

        // Load and verify
        WorldContext loaded = bootstrapper.loadGame(CONFIG, slot);
        assertNotNull(loaded.world());
        assertNotNull(loaded.sceneGraph());
    }

    @Test
    void constructorRejectsNullAssets() {
        assertThrows(NullPointerException.class, () ->
                new DefaultWorldBootstrapper(null, new DefaultSessionManager(),
                        new DefaultCodecRegistry(), DefaultSceneGraph::new));
    }

    @Test
    void constructorRejectsNullSession() {
        assertThrows(NullPointerException.class, () ->
                new DefaultWorldBootstrapper(defaultAssets(), null,
                        new DefaultCodecRegistry(), DefaultSceneGraph::new));
    }

    @Test
    void constructorRejectsNullCodecRegistry() {
        assertThrows(NullPointerException.class, () ->
                new DefaultWorldBootstrapper(defaultAssets(), new DefaultSessionManager(),
                        null, DefaultSceneGraph::new));
    }

    @Test
    void constructorRejectsNullSceneGraphFactory() {
        assertThrows(NullPointerException.class, () ->
                new DefaultWorldBootstrapper(defaultAssets(), new DefaultSessionManager(),
                        new DefaultCodecRegistry(), null));
    }

    private static DefaultWorldBootstrapper createBootstrapper() {
        return new DefaultWorldBootstrapper(
                defaultAssets(),
                new DefaultSessionManager(),
                new DefaultCodecRegistry(),
                DefaultSceneGraph::new
        );
    }

    private static AssetManager defaultAssets() {
        return ContentRuntime.builder()
                .manifest(new AssetManifestBuilder().build())
                .build()
                .assets();
    }
}
