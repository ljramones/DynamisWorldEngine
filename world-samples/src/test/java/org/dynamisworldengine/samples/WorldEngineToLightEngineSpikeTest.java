package org.dynamisworldengine.samples;

import org.dynamisscenegraph.core.DefaultSceneGraph;
import org.dynamisworldengine.api.config.WorldConfig;
import org.dynamisworldengine.runtime.DefaultWorldTickRunner;
import org.dynamisworldengine.runtime.projection.DefaultWorldProjector;
import org.dynamisworldengine.runtime.session.DefaultWorldBootstrapper;
import org.dynamisworldengine.samples.render.FakeEngineRuntime;
import org.dynamisworldengine.samples.render.SceneGraphToLightEngineAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldEngineToLightEngineSpikeTest {

    @Test
    void spineProjectionBatchesShouldRegisterThenUpdateEngineInstanceBatch() throws Exception {
        WorldConfig config = new WorldConfig("1.0.0-SNAPSHOT", 1, 10L);
        var registry = WorldEngineSpineSample.createRegistry();
        var bootstrapper = new DefaultWorldBootstrapper(registry);
        var ctx = bootstrapper.newGame(config);

        WorldEngineSpineSample.populateWorld(ctx.world(), 3);
        new DefaultWorldTickRunner(new DefaultWorldProjector()).runTick(ctx, config.initialTick() + 1);

        var batched = ((DefaultSceneGraph) ctx.sceneGraph()).extractBatched();
        assertEquals(1, batched.batches().size());
        assertEquals(3, WorldEngineSpineSample.totalInstances(batched));

        FakeEngineRuntime engine = new FakeEngineRuntime();
        SceneGraphToLightEngineAdapter adapter = new SceneGraphToLightEngineAdapter();

        adapter.sync(engine, batched);
        assertEquals(1, engine.registerCalls());
        assertEquals(0, engine.updateCalls());
        assertEquals(1, engine.lastRegisteredMeshHandle());
        assertEquals(3, engine.lastRegisteredRows());
        assertEquals(16, engine.lastRegisteredCols());

        adapter.sync(engine, batched);
        assertEquals(1, engine.registerCalls());
        assertEquals(1, engine.updateCalls());
        assertEquals(1, engine.lastUpdatedHandle());
        assertEquals(3, engine.lastUpdatedRows());
        assertEquals(16, engine.lastUpdatedCols());
    }
}
