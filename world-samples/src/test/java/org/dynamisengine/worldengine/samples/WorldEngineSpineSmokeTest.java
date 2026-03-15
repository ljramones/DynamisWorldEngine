package org.dynamisengine.worldengine.samples;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scenegraph.core.DefaultSceneGraph;
import org.dynamisengine.worldengine.api.config.WorldConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldEngineSpineSmokeTest {

    @Test
    void loadProjectExtractShouldPreserveEntityIdsAndBuildSingleBatch() throws Exception {
        WorldConfig config = new WorldConfig("1.0.0-SNAPSHOT", 1, 10L);
        var registry = WorldEngineSpineSample.createRegistry();

        var bootstrapper = new org.dynamisengine.worldengine.runtime.session.DefaultWorldBootstrapper(registry);
        var newGame = bootstrapper.newGame(config);
        Set<EntityId> beforeIds = new LinkedHashSet<>(WorldEngineSpineSample.populateWorld(newGame.world(), 3));

        Path slot = WorldEngineSpineSample.createTempSlot();
        var save = new org.dynamisengine.session.api.model.SaveGame(
                new org.dynamisengine.session.api.model.SaveMetadata(
                        config.saveFormatVersion(),
                        config.buildVersion(),
                        System.currentTimeMillis(),
                        config.initialTick(),
                        "world-spine-test"),
                new org.dynamisengine.session.api.model.EcsSnapshot(java.util.List.of()));

        newGame.session().save(slot, newGame.world(), save, registry);

        var loaded = bootstrapper.loadGame(config, slot);
        Set<EntityId> afterIds = new LinkedHashSet<>(loaded.world().entities());
        assertEquals(beforeIds, afterIds);

        new org.dynamisengine.worldengine.runtime.DefaultWorldTickRunner(
                new org.dynamisengine.worldengine.runtime.projection.DefaultWorldProjector())
                .runTick(loaded, config.initialTick() + 1);

        var batched = ((DefaultSceneGraph) loaded.sceneGraph()).extractBatched();
        assertEquals(1, batched.batches().size());
        assertEquals(3, WorldEngineSpineSample.totalInstances(batched));
    }
}
