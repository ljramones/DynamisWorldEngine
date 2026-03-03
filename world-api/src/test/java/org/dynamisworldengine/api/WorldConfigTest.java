package org.dynamisworldengine.api;

import org.dynamisworldengine.api.config.WorldConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorldConfigTest {

    @Test
    void validConfigBuilds() {
        WorldConfig config = new WorldConfig("1.0.0", 1, 0L);

        assertEquals("1.0.0", config.buildVersion());
        assertEquals(1, config.saveFormatVersion());
        assertEquals(0L, config.initialTick());
    }

    @Test
    void blankBuildVersionIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new WorldConfig(" ", 1, 0L));
    }

    @Test
    void negativeSaveFormatVersionIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new WorldConfig("1.0.0", -1, 0L));
    }

    @Test
    void negativeInitialTickIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new WorldConfig("1.0.0", 1, -1L));
    }
}
