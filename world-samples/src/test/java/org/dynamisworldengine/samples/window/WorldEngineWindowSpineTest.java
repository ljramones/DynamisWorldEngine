package org.dynamisworldengine.samples.window;

import org.dynamis.window.api.BackendHint;
import org.dynamis.window.api.InputEvent;
import org.dynamis.window.api.WindowConfig;
import org.dynamisscenegraph.core.DefaultSceneGraph;
import org.dynamisworldengine.api.config.WorldConfig;
import org.dynamisworldengine.runtime.DefaultWorldTickRunner;
import org.dynamisworldengine.runtime.projection.DefaultWorldProjector;
import org.dynamisworldengine.runtime.session.DefaultWorldBootstrapper;
import org.dynamisworldengine.samples.WorldEngineSpineSample;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldEngineWindowSpineTest {

    @Test
    void worldTickProgressesDeterministicallyWhenDrivenByFakeWindowLoop() throws Exception {
        WorldConfig config = new WorldConfig("1.0.0-SNAPSHOT", 1, 10L);
        var registry = WorldEngineSpineSample.createRegistry();

        var bootstrapper = new DefaultWorldBootstrapper(registry);
        var newGame = bootstrapper.newGame(config);
        WorldEngineSpineSample.populateWorld(newGame.world(), 3);

        var save = new org.dynamissession.api.model.SaveGame(
                new org.dynamissession.api.model.SaveMetadata(
                        config.saveFormatVersion(),
                        config.buildVersion(),
                        System.currentTimeMillis(),
                        config.initialTick(),
                        "world-window-spine-test"),
                new org.dynamissession.api.model.EcsSnapshot(java.util.List.of()));

        var slot = WorldEngineSpineSample.createTempSlot();
        newGame.session().save(slot, newGame.world(), save, registry);

        var loaded = bootstrapper.loadGame(config, slot);
        var tickRunner = new DefaultWorldTickRunner(new DefaultWorldProjector());

        FakeWindowLoopHarness harness = new FakeWindowLoopHarness(
                new WindowConfig("world-loop", 1024, 768, false, true, BackendHint.AUTO));

        harness.window().pushResize(1024, 768);
        harness.window().pushInputEvent(new InputEvent.Key(32, 0, InputEvent.InputAction.PRESS, 0));

        long finalTick = harness.runForTicks(3, tick -> tickRunner.runTick(loaded, tick));

        assertEquals(3L, finalTick);
        var batched = ((DefaultSceneGraph) loaded.sceneGraph()).extractBatched();
        assertEquals(1, batched.batches().size());
        assertEquals(3, WorldEngineSpineSample.totalInstances(batched));
    }
}
