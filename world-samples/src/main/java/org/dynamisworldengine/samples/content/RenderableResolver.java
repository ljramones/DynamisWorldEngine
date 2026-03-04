package org.dynamisworldengine.samples.content;

import org.dynamiscontent.api.id.AssetTypes;
import org.dynamiscontent.api.manifest.DmeshBlob;
import org.dynamiscontent.runtime.ContentRuntime;
import org.dynamisworldengine.samples.content.upload.FakeMeshUploadService;
import org.dynamisworldengine.samples.content.upload.MeshUploadService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RenderableResolver {
    private final ContentRuntime contentRuntime;
    private final MeshUploadService meshUploadService;
    private final Map<RenderableAssetRef, ResolvedRenderable> cache = new HashMap<>();

    public RenderableResolver(ContentRuntime contentRuntime) {
        this(contentRuntime, new FakeMeshUploadService());
    }

    public RenderableResolver(ContentRuntime contentRuntime, MeshUploadService meshUploadService) {
        this.contentRuntime = Objects.requireNonNull(contentRuntime, "contentRuntime");
        this.meshUploadService = Objects.requireNonNull(meshUploadService, "meshUploadService");
    }

    public ResolvedRenderable resolve(RenderableAssetRef ref) {
        Objects.requireNonNull(ref, "ref");
        return cache.computeIfAbsent(ref, ignored -> {
            DmeshBlob blob = contentRuntime.assets().get(ref.meshAssetId(), AssetTypes.DMESH_BLOB);
            int mesh = meshUploadService.uploadMesh(ref.meshAssetId(), blob);
            String material = ref.materialAssetId().value();
            return new ResolvedRenderable(mesh, material);
        });
    }
}
