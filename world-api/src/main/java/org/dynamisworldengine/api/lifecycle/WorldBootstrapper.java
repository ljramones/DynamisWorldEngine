package org.dynamisworldengine.api.lifecycle;

import org.dynamisworldengine.api.WorldContext;
import org.dynamisworldengine.api.config.WorldConfig;

import java.nio.file.Path;

public interface WorldBootstrapper {

    WorldContext newGame(WorldConfig config);

    WorldContext loadGame(WorldConfig config, Path slotFile);
}
