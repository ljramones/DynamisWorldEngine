package org.dynamisworldengine.samples.window;

import org.dynamis.window.api.WindowConfig;
import org.dynamis.window.api.WindowEvent;
import org.dynamis.window.api.WindowEvents;
import org.dynamis.window.test.FakeWindow;
import org.dynamis.window.test.FakeWindowSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class FakeWindowLoopHarness {
    private final FakeWindowSystem windowSystem;
    private final FakeWindow window;
    private final List<WindowEvent> observedWindowEvents = new ArrayList<>();

    public FakeWindowLoopHarness(WindowConfig config) {
        this.windowSystem = new FakeWindowSystem();
        this.window = (FakeWindow) windowSystem.create(config);
    }

    public FakeWindow window() {
        return window;
    }

    public List<WindowEvent> observedWindowEvents() {
        return List.copyOf(observedWindowEvents);
    }

    public long runForTicks(int ticks, Consumer<Long> perTick) {
        if (ticks < 0) {
            throw new IllegalArgumentException("ticks must be non-negative");
        }

        long executed = 0;
        for (long tick = 1; tick <= ticks; tick++) {
            WindowEvents events = window.pollEvents();
            observedWindowEvents.addAll(events.windowEvents());
            perTick.accept(tick);
            executed = tick;

            if (window.shouldClose()) {
                break;
            }
        }
        return executed;
    }
}
