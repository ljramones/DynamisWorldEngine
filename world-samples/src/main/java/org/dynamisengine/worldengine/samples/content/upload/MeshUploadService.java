package org.dynamisengine.worldengine.samples.content.upload;

import org.dynamisengine.content.api.id.AssetId;
import org.dynamisengine.content.api.manifest.DmeshBlob;

public interface MeshUploadService {
    int uploadMesh(AssetId meshAssetId, DmeshBlob blob);
}
