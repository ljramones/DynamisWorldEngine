package org.dynamisengine.worldengine.api.lifecycle;

import org.dynamisengine.worldengine.api.WorldContext;

public interface WorldTickRunner {

    void runTick(WorldContext ctx, long tick);
}
