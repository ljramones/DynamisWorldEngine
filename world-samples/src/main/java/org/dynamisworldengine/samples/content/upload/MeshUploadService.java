package org.dynamisworldengine.samples.content.upload;

import org.dynamiscontent.api.id.AssetId;
import org.dynamiscontent.api.manifest.DmeshBlob;

public interface MeshUploadService {
    int uploadMesh(AssetId meshAssetId, DmeshBlob blob);
}
