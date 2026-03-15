package org.dynamisengine.worldengine.api.lifecycle;

import org.dynamisengine.worldengine.api.WorldContext;
import org.dynamisengine.worldengine.api.config.WorldConfig;

import java.nio.file.Path;

public interface WorldBootstrapper {

    WorldContext newGame(WorldConfig config);

    WorldContext loadGame(WorldConfig config, Path slotFile);
}
