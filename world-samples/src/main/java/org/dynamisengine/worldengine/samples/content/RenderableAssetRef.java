package org.dynamisengine.worldengine.samples.content;

import org.dynamisengine.content.api.id.AssetId;

import java.util.Objects;

public record RenderableAssetRef(AssetId meshAssetId, AssetId materialAssetId) {
    public RenderableAssetRef {
        Objects.requireNonNull(meshAssetId, "meshAssetId");
        Objects.requireNonNull(materialAssetId, "materialAssetId");
    }
}
