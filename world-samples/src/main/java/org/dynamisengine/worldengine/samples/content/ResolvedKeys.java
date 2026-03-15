package org.dynamisengine.worldengine.samples.content;

import org.dynamisengine.ecs.api.component.ComponentKey;

public final class ResolvedKeys {
    private ResolvedKeys() {
    }

    public static final ComponentKey<ResolvedRenderable> RESOLVED_RENDERABLE =
            ComponentKey.of("world.resolvedRenderable", ResolvedRenderable.class);
}
