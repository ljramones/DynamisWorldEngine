package org.dynamisworldengine.samples;

import org.dynamis.core.entity.EntityId;
import org.dynamisscenegraph.core.DefaultSceneGraph;
import org.dynamisworldengine.api.config.WorldConfig;
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

        var bootstrapper = new org.dynamisworldengine.runtime.session.DefaultWorldBootstrapper(registry);
        var newGame = bootstrapper.newGame(config);
        Set<EntityId> beforeIds = new LinkedHashSet<>(WorldEngineSpineSample.populateWorld(newGame.world(), 3));

        Path slot = WorldEngineSpineSample.createTempSlot();
        var save = new org.dynamissession.api.model.SaveGame(
                new org.dynamissession.api.model.SaveMetadata(
                        config.saveFormatVersion(),
                        config.buildVersion(),
                        System.currentTimeMillis(),
                        config.initialTick(),
                        "world-spine-test"),
                new org.dynamissession.api.model.EcsSnapshot(java.util.List.of()));

        newGame.session().save(slot, newGame.world(), save, registry);

        var loaded = bootstrapper.loadGame(config, slot);
        Set<EntityId> afterIds = new LinkedHashSet<>(loaded.world().entities());
        assertEquals(beforeIds, afterIds);

        new org.dynamisworldengine.runtime.DefaultWorldTickRunner(
                new org.dynamisworldengine.runtime.projection.DefaultWorldProjector())
                .runTick(loaded, config.initialTick() + 1);

        var batched = ((DefaultSceneGraph) loaded.sceneGraph()).extractBatched();
        assertEquals(1, batched.batches().size());
        assertEquals(3, WorldEngineSpineSample.totalInstances(batched));
    }
}
