package org.dynamisengine.worldengine.samples;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ecs.api.world.World;
import org.dynamisengine.scenegraph.api.extract.BatchedRenderScene;
import org.dynamisengine.session.api.model.EcsSnapshot;
import org.dynamisengine.session.api.model.SaveGame;
import org.dynamisengine.session.api.model.SaveMetadata;
import org.dynamisengine.session.core.codec.DefaultCodecRegistry;
import org.dynamisengine.worldengine.api.WorldContext;
import org.dynamisengine.worldengine.api.config.WorldConfig;
import org.dynamisengine.worldengine.runtime.DefaultWorldTickRunner;
import org.dynamisengine.worldengine.runtime.projection.DefaultWorldProjector;
import org.dynamisengine.worldengine.runtime.projection.ProjectionKeys;
import org.dynamisengine.worldengine.runtime.projection.components.BoundsSphereComponent;
import org.dynamisengine.worldengine.runtime.projection.components.RenderableComponent;
import org.dynamisengine.worldengine.runtime.projection.components.TranslationComponent;
import org.dynamisengine.worldengine.runtime.session.DefaultWorldBootstrapper;
import org.dynamisengine.worldengine.samples.codecs.BoundsSphereCodec;
import org.dynamisengine.worldengine.samples.codecs.RenderableCodec;
import org.dynamisengine.worldengine.samples.codecs.TranslationCodec;

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
