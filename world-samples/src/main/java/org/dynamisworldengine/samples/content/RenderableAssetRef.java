package org.dynamisworldengine.samples.content;

import org.dynamiscontent.api.id.AssetId;

import java.util.Objects;

public record RenderableAssetRef(AssetId meshAssetId, AssetId materialAssetId) {
    public RenderableAssetRef {
        Objects.requireNonNull(meshAssetId, "meshAssetId");
        Objects.requireNonNull(materialAssetId, "materialAssetId");
    }
}
