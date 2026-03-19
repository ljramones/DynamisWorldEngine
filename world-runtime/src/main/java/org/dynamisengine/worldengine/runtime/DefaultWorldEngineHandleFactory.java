package org.dynamisengine.worldengine.runtime;

import org.dynamisengine.worldengine.api.WorldEngine;
import org.dynamisengine.worldengine.api.WorldEngineBuilder;
import org.dynamisengine.worldengine.api.WorldEngineHandle;

/**
 * Default ServiceLoader provider for {@link WorldEngine.WorldEngineHandleFactory}.
 */
public final class DefaultWorldEngineHandleFactory implements WorldEngine.WorldEngineHandleFactory {

    @Override
    public WorldEngineHandle create(WorldEngineBuilder builder) {
        return new DefaultWorldEngineHandle(builder);
    }
}
