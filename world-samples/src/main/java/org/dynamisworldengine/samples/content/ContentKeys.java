package org.dynamisworldengine.samples.content;

import org.dynamisecs.api.component.ComponentKey;

public final class ContentKeys {
    private ContentKeys() {
    }

    public static final ComponentKey<RenderableAssetRef> RENDERABLE_ASSET_REF =
            ComponentKey.of("world.renderableAssetRef", RenderableAssetRef.class);
}
