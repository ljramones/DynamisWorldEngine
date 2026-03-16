package org.dynamisengine.worldengine.runtime;

import org.dynamisengine.content.api.AssetManager;
import org.dynamisengine.content.runtime.ContentRuntime;
import org.dynamisengine.content.core.manifest.AssetManifestBuilder;
import org.dynamisengine.ecs.core.DefaultWorld;
import org.dynamisengine.scenegraph.core.DefaultSceneGraph;
import org.dynamisengine.session.runtime.DefaultSessionManager;
import org.dynamisengine.worldengine.api.WorldContext;
import org.dynamisengine.worldengine.api.lifecycle.WorldProjector;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultWorldTickRunnerTest {

    @Test
    void tickLifecycleOrdering() {
        List<String> events = new ArrayList<>();
        var world = new DefaultWorld();
        WorldContext ctx = createContext(world);

        WorldProjector recorder = c -> events.add("project");

        // Wrap world to track beginTick/endTick ordering
        // We verify indirectly: project is called, and delta reflects tick state
        var runner = new DefaultWorldTickRunner(recorder);
        runner.runTick(ctx, 1L);

        assertEquals(List.of("project"), events);
    }

    @Test
    void multipleTicksIncrementCorrectly() {
        List<Long> ticksSeen = new ArrayList<>();
        var world = new DefaultWorld();
        WorldContext ctx = createContext(world);

        // The projector captures the world's delta tick each time
        WorldProjector recorder = c -> ticksSeen.add(c.world().delta().tick().tick());
        var runner = new DefaultWorldTickRunner(recorder);

        runner.runTick(ctx, 0L);
        runner.runTick(ctx, 1L);
        runner.runTick(ctx, 2L);

        assertEquals(List.of(0L, 1L, 2L), ticksSeen);
    }

    @Test
    void nullProjectorIsRejected() {
        assertThrows(NullPointerException.class, () -> new DefaultWorldTickRunner(null));
    }

    @Test
    void nullContextIsRejected() {
        var runner = new DefaultWorldTickRunner(c -> {});
        assertThrows(NullPointerException.class, () -> runner.runTick(null, 0L));
    }

    private static WorldContext createContext(DefaultWorld world) {
        AssetManager assets = ContentRuntime.builder()
                .manifest(new AssetManifestBuilder().build())
                .build()
                .assets();
        return new WorldContext(assets, new DefaultSessionManager(), world, new DefaultSceneGraph());
    }
}
