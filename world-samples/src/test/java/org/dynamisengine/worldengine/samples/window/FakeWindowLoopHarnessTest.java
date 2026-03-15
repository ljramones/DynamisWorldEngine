package org.dynamisengine.worldengine.samples.window;

import org.dynamisengine.window.api.BackendHint;
import org.dynamisengine.window.api.InputEvent;
import org.dynamisengine.window.api.WindowConfig;
import org.dynamisengine.window.api.WindowEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FakeWindowLoopHarnessTest {

    @Test
    void loopPumpsQueuedEventsInOrderAndStopsOnClose() {
        FakeWindowLoopHarness harness = new FakeWindowLoopHarness(
                new WindowConfig("fake-loop", 800, 600, false, true, BackendHint.AUTO));

        harness.window().pushResize(1280, 720);
        harness.window().pushInputEvent(new InputEvent.Key(65, 0, InputEvent.InputAction.PRESS, 0));
        harness.window().requestClose();

        List<Long> ticks = new ArrayList<>();
        long executed = harness.runForTicks(5, ticks::add);

        assertEquals(1L, executed);
        assertEquals(List.of(1L), ticks);
        assertEquals(
                List.of(
                        new WindowEvent.Resized(new org.dynamisengine.window.api.WindowSize(1280, 720)),
                        new WindowEvent.CloseRequested()),
                harness.observedWindowEvents());
    }
}
