package org.dynamisworldengine.samples.input;

import org.dynamis.core.entity.EntityId;
import org.dynamis.window.api.InputEvent;
import org.dynamis.window.api.WindowConfig;
import org.dynamis.window.test.FakeWindow;
import org.dynamis.window.test.FakeWindowSystem;
import org.dynamisecs.core.DefaultWorld;
import org.dynamisinput.api.AxisId;
import org.dynamisinput.api.ContextId;
import org.dynamisinput.api.bind.AxisComposite2D;
import org.dynamisinput.api.context.InputMap;
import org.dynamisinput.core.DefaultInputProcessor;
import org.dynamisinput.core.recording.InputRecorder;
import org.dynamisinput.runtime.InputRuntime;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldEngineInputIntegrationTest {

    private static final ContextId GAMEPLAY = new ContextId("gameplay");
    private static final AxisId MOVE_X = new AxisId("move.x");
    private static final AxisId MOVE_Y = new AxisId("move.y");

    @Test
    void fakeWindowEventsDriveMoveIntentAcrossTicks() {
        FakeWindow window = (FakeWindow) new FakeWindowSystem().create(WindowConfig.defaults());
        DefaultWorld world = new DefaultWorld();
        EntityId player = world.createEntity();

        WorldInputDriver driver = new WorldInputDriver(player, createRuntime());

        window.pushInputEvent(new InputEvent.Key(87, 0, InputEvent.InputAction.PRESS, 0));
        runTick(window, driver, world, 1);
        assertMove(world, player, 0f, 1f);

        runTick(window, driver, world, 2);
        assertMove(world, player, 0f, 1f);

        window.pushInputEvent(new InputEvent.Key(87, 0, InputEvent.InputAction.RELEASE, 0));
        runTick(window, driver, world, 3);
        assertMove(world, player, 0f, 0f);
    }

    @Test
    void replayProducesIdenticalMoveIntentSequence() {
        List<ScriptedEvent> script = List.of(
                new ScriptedEvent(1L, new InputEvent.Key(87, 0, InputEvent.InputAction.PRESS, 0)),
                new ScriptedEvent(3L, new InputEvent.Key(87, 0, InputEvent.InputAction.RELEASE, 0))
        );

        List<MoveIntent> live = runLive(script, 4);
        List<MoveIntent> replay = runReplay(script, 4);

        assertEquals(live, replay);
    }

    private static List<MoveIntent> runLive(List<ScriptedEvent> script, int ticks) {
        FakeWindow window = (FakeWindow) new FakeWindowSystem().create(WindowConfig.defaults());
        DefaultWorld world = new DefaultWorld();
        EntityId player = world.createEntity();
        WorldInputDriver driver = new WorldInputDriver(player, createRuntime());

        List<MoveIntent> sequence = new ArrayList<>();
        for (long tick = 1; tick <= ticks; tick++) {
            for (ScriptedEvent event : script) {
                if (event.tick() == tick) {
                    window.pushInputEvent(event.event());
                }
            }
            runTick(window, driver, world, tick);
            sequence.add(world.get(player, InputKeys.MOVE_INTENT).orElseThrow());
        }
        return sequence;
    }

    private static List<MoveIntent> runReplay(List<ScriptedEvent> script, int ticks) {
        DefaultWorld world = new DefaultWorld();
        EntityId player = world.createEntity();

        InputRecorder recorder = new InputRecorder();
        for (ScriptedEvent event : script) {
            recorder.record(event.event(), event.tick());
        }

        InputRuntime runtime = createRuntime();
        WorldInputDriver driver = new WorldInputDriver(player, runtime);

        List<MoveIntent> sequence = new ArrayList<>();
        for (long tick = 1; tick <= ticks; tick++) {
            for (var entry : recorder.toRecording().entries()) {
                if (entry.tick() == tick) {
                    driver.collect(List.of(entry.event()));
                }
            }
            driver.tick(world, tick);
            sequence.add(world.get(player, InputKeys.MOVE_INTENT).orElseThrow());
        }
        return sequence;
    }

    private static void runTick(FakeWindow window, WorldInputDriver driver, DefaultWorld world, long tick) {
        var events = window.pollEvents();
        driver.collect(events.inputEvents());
        driver.tick(world, tick);
    }

    private static InputRuntime createRuntime() {
        InputMap gameplay = new InputMap(
                GAMEPLAY,
                Map.of(),
                Map.of(
                        MOVE_X, List.of(new AxisComposite2D(MOVE_X, MOVE_Y, 65, 68, 83, 87, 1.0f)),
                        MOVE_Y, List.of(new AxisComposite2D(MOVE_X, MOVE_Y, 65, 68, 83, 87, 1.0f))
                ),
                false
        );

        return InputRuntime.builder()
                .processor(new DefaultInputProcessor(Map.of(GAMEPLAY, gameplay)))
                .initialContext(GAMEPLAY)
                .build();
    }

    private static void assertMove(DefaultWorld world, EntityId player, float x, float y) {
        MoveIntent intent = world.get(player, InputKeys.MOVE_INTENT).orElseThrow();
        assertEquals(x, intent.x());
        assertEquals(y, intent.y());
    }

    private record ScriptedEvent(long tick, InputEvent event) {
    }
}
