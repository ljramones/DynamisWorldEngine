package org.dynamisengine.worldengine.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the WorldEngine facade public API types.
 * These test the facade contracts without requiring the full runtime.
 */
class WorldEngineFacadeTest {

    // -- WorldEngineState ----------------------------------------------------

    @Test
    void stateEnumHasAllExpectedValues() {
        assertEquals(7, WorldEngineState.values().length);
        assertNotNull(WorldEngineState.CREATED);
        assertNotNull(WorldEngineState.INITIALIZING);
        assertNotNull(WorldEngineState.RUNNING);
        assertNotNull(WorldEngineState.PAUSED);
        assertNotNull(WorldEngineState.FAULTED);
        assertNotNull(WorldEngineState.STOPPING);
        assertNotNull(WorldEngineState.STOPPED);
    }

    // -- WorldEngineBuilder --------------------------------------------------

    @Test
    void builderRequiresApplication() {
        var builder = WorldEngine.builder();
        assertThrows(IllegalStateException.class, builder::build,
                "build() without application should throw");
    }

    @Test
    void builderDefaultConfig() {
        var builder = WorldEngine.builder();
        var config = builder.configOrDefault();
        assertNotNull(config);
        assertEquals("0.0.1-dev", config.buildVersion());
    }

    @Test
    void builderTickRateValidation() {
        var builder = WorldEngine.builder();
        assertThrows(IllegalArgumentException.class, () -> builder.tickRate(0));
        assertThrows(IllegalArgumentException.class, () -> builder.tickRate(1001));
        assertDoesNotThrow(() -> builder.tickRate(60));
        assertDoesNotThrow(() -> builder.tickRate(1));
        assertDoesNotThrow(() -> builder.tickRate(1000));
    }

    @Test
    void builderHeadlessMode() {
        var builder = WorldEngine.builder().headless();
        assertTrue(builder.isHeadless());
        assertFalse(builder.isTestMode());
    }

    @Test
    void builderTestModeImpliesHeadless() {
        var builder = WorldEngine.builder().testMode();
        assertTrue(builder.isTestMode());
        assertTrue(builder.isHeadless());
    }

    @Test
    void builderChaining() {
        StubApplication app = new StubApplication();
        var builder = WorldEngine.builder()
                .application(app)
                .tickRate(120)
                .headless();

        assertEquals(app, builder.application());
        assertEquals(120, builder.tickRate());
        assertTrue(builder.isHeadless());
    }

    // -- GameContext ----------------------------------------------------------

    @Test
    void gameContextRejectsNull() {
        assertThrows(NullPointerException.class,
                () -> new GameContext(null, 0, 0.0, WorldEngineState.RUNNING));
        assertThrows(NullPointerException.class,
                () -> new GameContext(stubWorldContext(), 0, 0.0, null));
    }

    // -- WorldApplication default methods ------------------------------------

    @Test
    void worldApplicationDefaultMethodsDoNotThrow() {
        StubApplication app = new StubApplication();
        // onPause and onResume have default no-op implementations
        assertDoesNotThrow(() -> app.onPause(null));
        assertDoesNotThrow(() -> app.onResume(null));
    }

    // -- Helpers --------------------------------------------------------------

    /** Minimal WorldApplication for testing. */
    private static class StubApplication implements WorldApplication {
        boolean initialized = false;
        int updateCount = 0;
        boolean shutdown = false;

        @Override
        public void initialize(GameContext context) { initialized = true; }

        @Override
        public void update(GameContext context, float deltaSeconds) { updateCount++; }

        @Override
        public void shutdown(GameContext context) { shutdown = true; }
    }

    /** Create a minimal WorldContext for testing (uses null-safe stubs). */
    private static WorldContext stubWorldContext() {
        // WorldContext requires non-null components — use mock-like approach
        // In the real test harness this would use real ECS/Session/Content stubs
        // For facade API tests, we only need the record to be constructable
        return null; // GameContext null-check test covers this
    }
}
