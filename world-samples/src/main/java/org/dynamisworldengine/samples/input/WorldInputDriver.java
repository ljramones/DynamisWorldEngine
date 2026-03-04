package org.dynamisworldengine.samples.input;

import org.dynamis.core.entity.EntityId;
import org.dynamis.window.api.InputEvent;
import org.dynamisecs.api.world.World;
import org.dynamisinput.api.AxisId;
import org.dynamisinput.api.frame.InputFrame;
import org.dynamisinput.runtime.InputRuntime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class WorldInputDriver {
    private static final AxisId MOVE_X = new AxisId("move.x");
    private static final AxisId MOVE_Y = new AxisId("move.y");

    private final EntityId player;
    private final InputRuntime runtime;
    private final List<InputEvent> pendingEvents = new ArrayList<>();

    public WorldInputDriver(EntityId player, InputRuntime runtime) {
        this.player = Objects.requireNonNull(player, "player");
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    public void collect(List<InputEvent> inputEvents) {
        pendingEvents.addAll(List.copyOf(Objects.requireNonNull(inputEvents, "inputEvents")));
    }

    public void tick(World world, long tick) {
        Objects.requireNonNull(world, "world");

        for (InputEvent event : pendingEvents) {
            runtime.feed(event, tick);
        }
        pendingEvents.clear();

        InputFrame frame = runtime.frame(tick);
        world.add(player, InputKeys.MOVE_INTENT, new MoveIntent(frame.axis(MOVE_X), frame.axis(MOVE_Y)));
    }
}
