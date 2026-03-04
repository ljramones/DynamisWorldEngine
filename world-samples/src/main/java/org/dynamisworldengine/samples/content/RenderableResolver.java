package org.dynamisworldengine.samples.content;

import org.dynamiscontent.api.id.AssetTypes;
import org.dynamiscontent.api.manifest.DmeshBlob;
import org.dynamiscontent.runtime.ContentRuntime;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RenderableResolver {
    private final ContentRuntime contentRuntime;
    private final Map<RenderableAssetRef, ResolvedRenderable> cache = new HashMap<>();

    public RenderableResolver(ContentRuntime contentRuntime) {
        this.contentRuntime = Objects.requireNonNull(contentRuntime, "contentRuntime");
    }

    public ResolvedRenderable resolve(RenderableAssetRef ref) {
        Objects.requireNonNull(ref, "ref");
        return cache.computeIfAbsent(ref, ignored -> {
            DmeshBlob blob = contentRuntime.assets().get(ref.meshAssetId(), AssetTypes.DMESH_BLOB);
            int mesh = deterministicMeshHandle(blob.contentHash64());
            String material = ref.materialAssetId().value();
            return new ResolvedRenderable(mesh, material);
        });
    }

    public static int deterministicMeshHandle(long contentHash64) {
        int meshHandle = (int) (contentHash64 ^ (contentHash64 >>> 32));
        return meshHandle == 0 ? 1 : meshHandle;
    }
}
