package org.dynamisworldengine.api.lifecycle;

import org.dynamisworldengine.api.WorldContext;

public interface WorldTickRunner {

    void runTick(WorldContext ctx, long tick);
}
