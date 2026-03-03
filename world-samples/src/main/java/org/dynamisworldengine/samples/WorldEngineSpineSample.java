package org.dynamisworldengine.samples;

import org.dynamis.core.entity.EntityId;
import org.dynamisecs.api.world.World;
import org.dynamisscenegraph.api.extract.BatchedRenderScene;
import org.dynamissession.api.model.EcsSnapshot;
import org.dynamissession.api.model.SaveGame;
import org.dynamissession.api.model.SaveMetadata;
import org.dynamissession.core.codec.DefaultCodecRegistry;
import org.dynamisworldengine.api.WorldContext;
import org.dynamisworldengine.api.config.WorldConfig;
import org.dynamisworldengine.runtime.DefaultWorldTickRunner;
import org.dynamisworldengine.runtime.projection.DefaultWorldProjector;
import org.dynamisworldengine.runtime.projection.ProjectionKeys;
import org.dynamisworldengine.runtime.projection.components.BoundsSphereComponent;
import org.dynamisworldengine.runtime.projection.components.RenderableComponent;
import org.dynamisworldengine.runtime.projection.components.TranslationComponent;
import org.dynamisworldengine.runtime.session.DefaultWorldBootstrapper;
import org.dynamisworldengine.samples.codecs.BoundsSphereCodec;
import org.dynamisworldengine.samples.codecs.RenderableCodec;
import org.dynamisworldengine.samples.codecs.TranslationCodec;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class WorldEngineSpineSample {

    private WorldEngineSpineSample() {
    }

    public static DefaultCodecRegistry createRegistry() {
        DefaultCodecRegistry registry = new DefaultCodecRegistry();
        registry.register(new TranslationCodec());
        registry.register(new BoundsSphereCodec());
        registry.register(new RenderableCodec());
        return registry;
    }

    public static Set<EntityId> populateWorld(World world, int count) {
        Set<EntityId> ids = new LinkedHashSet<>();

        for (int i = 0; i < count; i++) {
            EntityId entity = world.createEntity();
            ids.add(entity);
            int x = i % 4;
            int z = i / 4;
            world.add(entity, ProjectionKeys.TRANSLATION, new TranslationComponent(x, 0f, z));
            world.add(entity, ProjectionKeys.BOUNDS, new BoundsSphereComponent(0f, 0f, 0f, 0.5f));
            world.add(entity, ProjectionKeys.RENDERABLE, new RenderableComponent(1, "mat.default"));
        }

        return ids;
    }

    public static WorldContext saveLoadAndTick(
            WorldConfig config,
            Path slot,
            int entityCount,
            DefaultCodecRegistry registry
    ) throws Exception {
        DefaultWorldBootstrapper bootstrapper = new DefaultWorldBootstrapper(registry);
        WorldContext newGame = bootstrapper.newGame(config);
        populateWorld(newGame.world(), entityCount);

        SaveGame save = new SaveGame(
                new SaveMetadata(
                        config.saveFormatVersion(),
                        config.buildVersion(),
                        System.currentTimeMillis(),
                        config.initialTick(),
                        "world-spine"),
                new EcsSnapshot(List.of()));

        newGame.session().save(slot, newGame.world(), save, registry);

        WorldContext loaded = bootstrapper.loadGame(config, slot);
        new DefaultWorldTickRunner(new DefaultWorldProjector()).runTick(loaded, config.initialTick() + 1);
        return loaded;
    }

    public static Path createTempSlot() throws Exception {
        return Files.createTempFile("worldengine-spine-", ".dses");
    }

    public static int totalInstances(BatchedRenderScene scene) {
        return scene.batches().stream().mapToInt(batch -> batch.instanceCount()).sum();
    }
}
