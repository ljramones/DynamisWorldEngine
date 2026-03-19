package org.dynamisengine.worldengine.runtime.subsystem;

import org.dynamisengine.worldengine.api.WorldContext;
import org.dynamisengine.worldengine.api.lifecycle.DynamisInitException;
import org.dynamisengine.worldengine.api.lifecycle.DynamisTickException;
import org.dynamisengine.worldengine.api.lifecycle.DynamisShutdownException;
import org.dynamisengine.worldengine.api.telemetry.SubsystemHealth;
import org.dynamisengine.worldengine.api.telemetry.SubsystemTelemetry;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SubsystemContractTest {

    // -- Dependency ordering --------------------------------------------------

    @Test
    void dependenciesInitializedFirst() {
        var registry = new SubsystemRegistry();
        var order = new ArrayList<String>();

        registry.register(new StubSubsystem("B", Set.of("A")) {
            @Override public void initialize(WorldContext ctx) { order.add("B"); }
        });
        registry.register(new StubSubsystem("A", Set.of()) {
            @Override public void initialize(WorldContext ctx) { order.add("A"); }
        });

        List<WorldSubsystem> ordered = registry.ordered();
        assertEquals("A", ordered.get(0).name());
        assertEquals("B", ordered.get(1).name());
    }

    @Test
    void threeLayerDependencyChain() {
        var registry = new SubsystemRegistry();
        registry.register(new StubSubsystem("C", Set.of("B")));
        registry.register(new StubSubsystem("A", Set.of()));
        registry.register(new StubSubsystem("B", Set.of("A")));

        List<WorldSubsystem> ordered = registry.ordered();
        assertEquals("A", ordered.get(0).name());
        assertEquals("B", ordered.get(1).name());
        assertEquals("C", ordered.get(2).name());
    }

    @Test
    void cycleDetected() {
        var registry = new SubsystemRegistry();
        registry.register(new StubSubsystem("A", Set.of("B")));
        registry.register(new StubSubsystem("B", Set.of("A")));

        assertThrows(IllegalStateException.class, registry::ordered);
    }

    @Test
    void missingDependencyDetected() {
        var registry = new SubsystemRegistry();
        registry.register(new StubSubsystem("A", Set.of("Missing")));

        assertThrows(IllegalStateException.class, registry::ordered);
    }

    @Test
    void reverseOrderForShutdown() {
        var registry = new SubsystemRegistry();
        registry.register(new StubSubsystem("A", Set.of()));
        registry.register(new StubSubsystem("B", Set.of("A")));

        List<WorldSubsystem> reversed = registry.reverseOrdered();
        assertEquals("B", reversed.get(0).name());
        assertEquals("A", reversed.get(1).name());
    }

    @Test
    void duplicateRegistrationThrows() {
        var registry = new SubsystemRegistry();
        registry.register(new StubSubsystem("A", Set.of()));
        assertThrows(IllegalStateException.class,
                () -> registry.register(new StubSubsystem("A", Set.of())));
    }

    // -- Coordinator lifecycle ------------------------------------------------

    @Test
    void coordinatorLifecycleOrder() throws Exception {
        var registry = new SubsystemRegistry();
        var events = new ArrayList<String>();

        registry.register(new StubSubsystem("Core", Set.of()) {
            @Override public void initialize(WorldContext ctx) { events.add("Core.init"); }
            @Override public void start() { events.add("Core.start"); }
            @Override public void tick(long t, float dt) { events.add("Core.tick"); }
            @Override public void stop() { events.add("Core.stop"); }
            @Override public void shutdown() { events.add("Core.shutdown"); }
        });
        registry.register(new StubSubsystem("Audio", Set.of("Core")) {
            @Override public void initialize(WorldContext ctx) { events.add("Audio.init"); }
            @Override public void start() { events.add("Audio.start"); }
            @Override public void tick(long t, float dt) { events.add("Audio.tick"); }
            @Override public void stop() { events.add("Audio.stop"); }
            @Override public void shutdown() { events.add("Audio.shutdown"); }
        });

        var coordinator = new SubsystemCoordinator(registry);
        coordinator.initializeAll(null);
        coordinator.startAll();
        coordinator.tickAll(1, 0.016f);
        coordinator.stopAll();
        coordinator.shutdownAll();

        assertEquals(List.of(
                "Core.init", "Audio.init",
                "Core.start", "Audio.start",
                "Core.tick", "Audio.tick",
                "Audio.stop", "Core.stop",
                "Audio.shutdown", "Core.shutdown"),
                events);
    }

    // -- Failure propagation --------------------------------------------------

    @Test
    void initFailurePropagates() {
        var registry = new SubsystemRegistry();
        registry.register(new StubSubsystem("Broken", Set.of()) {
            @Override public void initialize(WorldContext ctx) throws DynamisInitException {
                throw new DynamisInitException("Broken", "test failure", null);
            }
        });

        var coordinator = new SubsystemCoordinator(registry);
        assertThrows(DynamisInitException.class, () -> coordinator.initializeAll(null));
    }

    @Test
    void recoverableTickErrorContinues() throws Exception {
        var registry = new SubsystemRegistry();
        var ticked = new boolean[]{false};

        registry.register(new StubSubsystem("Flaky", Set.of()) {
            @Override public void tick(long t, float dt) throws DynamisTickException {
                throw new DynamisTickException("Flaky", t, new RuntimeException("oops"));
            }
        });
        registry.register(new StubSubsystem("Stable", Set.of()) {
            @Override public void tick(long t, float dt) { ticked[0] = true; }
        });

        var coordinator = new SubsystemCoordinator(registry);
        coordinator.initializeAll(null);

        // DynamisTickException is recoverable by default — coordinator should continue
        coordinator.tickAll(1, 0.016f);
        assertTrue(ticked[0], "Stable subsystem should still tick after Flaky's recoverable error");
    }

    @Test
    void shutdownErrorDoesNotPreventOthers() throws Exception {
        var registry = new SubsystemRegistry();
        var events = new ArrayList<String>();

        registry.register(new StubSubsystem("A", Set.of()) {
            @Override public void shutdown() { events.add("A.shutdown"); }
        });
        registry.register(new StubSubsystem("B", Set.of()) {
            @Override public void shutdown() throws DynamisShutdownException {
                events.add("B.shutdown-error");
                throw new DynamisShutdownException("B", new RuntimeException("fail"));
            }
        });

        var coordinator = new SubsystemCoordinator(registry);
        coordinator.initializeAll(null);
        coordinator.shutdownAll();

        // Both should have attempted shutdown despite B's error
        assertTrue(events.contains("A.shutdown"));
        assertTrue(events.contains("B.shutdown-error"));
    }

    // -- Telemetry aggregation ------------------------------------------------

    @Test
    void telemetryAggregation() throws Exception {
        var registry = new SubsystemRegistry();
        registry.register(new StubSubsystem("Audio", Set.of()) {
            @Override public Optional<Object> captureTelemetry() {
                return Optional.of("AudioTelemetryDetail");
            }
        });
        registry.register(new StubSubsystem("Input", Set.of()) {
            // No telemetry detail
        });

        var coordinator = new SubsystemCoordinator(registry);
        coordinator.initializeAll(null);

        Map<String, SubsystemTelemetry> snapshot = coordinator.telemetrySnapshot();
        assertEquals(2, snapshot.size());

        SubsystemTelemetry audio = snapshot.get("Audio");
        assertNotNull(audio);
        assertTrue(audio.hasDetail());
        assertEquals("AudioTelemetryDetail", audio.detailAs(String.class));

        SubsystemTelemetry input = snapshot.get("Input");
        assertNotNull(input);
        assertFalse(input.hasDetail());
    }

    @Test
    void emptyRegistryProducesEmptyTelemetry() {
        var coordinator = new SubsystemCoordinator(new SubsystemRegistry());
        Map<String, SubsystemTelemetry> snapshot = coordinator.telemetrySnapshot();
        assertTrue(snapshot.isEmpty());
    }

    // -- Stub subsystem -------------------------------------------------------

    private static class StubSubsystem implements WorldSubsystem {
        private final String name;
        private final Set<String> deps;

        StubSubsystem(String name, Set<String> deps) {
            this.name = name;
            this.deps = deps;
        }

        @Override public String name() { return name; }
        @Override public Set<String> dependencies() { return deps; }
        @Override public void initialize(WorldContext ctx) throws DynamisInitException {}
        @Override public void start() throws DynamisInitException {}
        @Override public void tick(long tick, float dt) throws DynamisTickException {}
        @Override public void stop() throws DynamisShutdownException {}
        @Override public void shutdown() throws DynamisShutdownException {}
        @Override public SubsystemHealth health() {
            return SubsystemHealth.healthy(name, 0);
        }
    }
}
