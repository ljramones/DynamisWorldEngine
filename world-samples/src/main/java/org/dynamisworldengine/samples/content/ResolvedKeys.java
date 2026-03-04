package org.dynamisworldengine.samples.content;

import org.dynamisecs.api.component.ComponentKey;

public final class ResolvedKeys {
    private ResolvedKeys() {
    }

    public static final ComponentKey<ResolvedRenderable> RESOLVED_RENDERABLE =
            ComponentKey.of("world.resolvedRenderable", ResolvedRenderable.class);
}
