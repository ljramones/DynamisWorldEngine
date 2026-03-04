package org.dynamisworldengine.samples.content.upload;

import org.dynamiscontent.api.id.AssetId;
import org.dynamiscontent.api.manifest.DmeshBlob;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class FakeMeshUploadService implements MeshUploadService {
    private final Map<AssetId, Integer> handleByAssetId = new HashMap<>();
    private final Map<Long, Integer> handleByHash = new HashMap<>();
    private int uploadCount;

    @Override
    public synchronized int uploadMesh(AssetId meshAssetId, DmeshBlob blob) {
        Objects.requireNonNull(meshAssetId, "meshAssetId");
        Objects.requireNonNull(blob, "blob");

        Integer cachedById = handleByAssetId.get(meshAssetId);
        if (cachedById != null) {
            return cachedById;
        }

        long hash = blob.contentHash64();
        Integer handle = handleByHash.get(hash);
        if (handle == null) {
            handle = deterministicMeshHandle(hash);
            handleByHash.put(hash, handle);
            uploadCount++;
        }

        handleByAssetId.put(meshAssetId, handle);
        return handle;
    }

    public synchronized int uploadCount() {
        return uploadCount;
    }

    public static int deterministicMeshHandle(long contentHash64) {
        int meshHandle = (int) (contentHash64 ^ (contentHash64 >>> 32));
        return meshHandle == 0 ? 1 : meshHandle;
    }
}
