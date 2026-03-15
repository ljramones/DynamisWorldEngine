package org.dynamisengine.worldengine.runtime;

import org.dynamisengine.ecs.api.world.WorldTick;
import org.dynamisengine.worldengine.api.WorldContext;
import org.dynamisengine.worldengine.api.lifecycle.WorldProjector;
import org.dynamisengine.worldengine.api.lifecycle.WorldTickRunner;

import java.util.Objects;

public final class DefaultWorldTickRunner implements WorldTickRunner {

    private final WorldProjector projector;

    public DefaultWorldTickRunner(WorldProjector projector) {
        this.projector = Objects.requireNonNull(projector, "projector");
    }

    @Override
    public void runTick(WorldContext ctx, long tick) {
        Objects.requireNonNull(ctx, "ctx");

        ctx.world().beginTick(new WorldTick(tick));
        projector.project(ctx);
        ctx.world().endTick();
    }
}
