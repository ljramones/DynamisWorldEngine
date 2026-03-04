package org.dynamisworldengine.samples.content;

import org.dynamiscontent.api.id.AssetType;
import org.dynamiscontent.runtime.ContentRuntime;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RenderableResolver {
    public static final AssetType<Integer> MESH_HANDLE = AssetType.of("demo.meshHandle", Integer.class);
    public static final AssetType<String> MATERIAL_KEY = AssetType.of("demo.materialKey", String.class);

    private final ContentRuntime contentRuntime;
    private final Map<RenderableAssetRef, ResolvedRenderable> cache = new HashMap<>();

    public RenderableResolver(ContentRuntime contentRuntime) {
        this.contentRuntime = Objects.requireNonNull(contentRuntime, "contentRuntime");
    }

    public ResolvedRenderable resolve(RenderableAssetRef ref) {
        Objects.requireNonNull(ref, "ref");
        return cache.computeIfAbsent(ref, ignored -> {
            int mesh = contentRuntime.assets().get(ref.meshAssetId(), MESH_HANDLE);
            String material = contentRuntime.assets().get(ref.materialAssetId(), MATERIAL_KEY);
            return new ResolvedRenderable(mesh, material);
        });
    }

    public static int parseMeshHandleUri(String uri) {
        Objects.requireNonNull(uri, "uri");
        String value = uri.startsWith("meshHandle:") ? uri.substring("meshHandle:".length()) : uri;
        return Integer.parseInt(value.trim());
    }
}
