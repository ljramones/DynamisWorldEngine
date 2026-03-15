package org.dynamisengine.worldengine.samples.content;

import org.dynamisengine.assetpipeline.api.CookProfile;
import org.dynamisengine.assetpipeline.api.mesh.MeshCookRequest;
import org.dynamisengine.assetpipeline.api.mesh.MeshCookResult;
import org.dynamisengine.assetpipeline.core.mesh.MeshCooker;
import org.dynamisengine.content.api.id.AssetId;
import org.dynamisengine.ecs.core.DefaultWorld;
import org.dynamisengine.worldengine.samples.content.upload.FakeMeshUploadService;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContentDmeshHashDedupeTest {

    @Test
    void uploaderShouldDedupeByHashAcrossDistinctMeshAssetIds() throws Exception {
        AssetId meshA = AssetId.of("mesh/a");
        AssetId meshB = AssetId.of("mesh/b");
        AssetId material = AssetId.of("mat/default");

        Path tempDir = Files.createTempDirectory("phase9-dedupe");
        Path sourceObj = tempDir.resolve("shared.obj");
        Files.writeString(sourceObj, """
                v 0 0 0
                v 1 0 0
                v 0 1 0
                vn 0 0 1
                f 1//1 2//1 3//1
                """);

        MeshCookResult cookResult = new MeshCooker().cook(new MeshCookRequest(
                "mesh/shared",
                sourceObj,
                CookProfile.REALTIME_FAST,
                tempDir.resolve("cooked")
        ));

        Path manifestPath = tempDir.resolve("manifest.json");
        Files.writeString(manifestPath, """
                {
                  "version": 1,
                  "entries": [
                    {
                      "id": "mesh/a",
                      "typeId": "mesh.packed.dmesh.v0",
                      "uri": "cooked/mesh/shared.dmesh",
                      "dependencies": []
                    },
                    {
                      "id": "mesh/b",
                      "typeId": "mesh.packed.dmesh.v0",
                      "uri": "cooked/mesh/shared.dmesh",
                      "dependencies": []
                    }
                  ]
                }
                """);

        var runtime = org.dynamisengine.content.runtime.ContentRuntime.builder()
                .baseDir(tempDir)
                .manifest(manifestPath)
                .registerDefaultLoaders(tempDir)
                .build();

        FakeMeshUploadService uploader = new FakeMeshUploadService();
        RenderableResolver resolver = new RenderableResolver(runtime, uploader);
        ResolveRenderablesSystem resolveSystem = new ResolveRenderablesSystem(resolver);

        DefaultWorld world = new DefaultWorld();
        var entityA = world.createEntity();
        var entityB = world.createEntity();
        world.add(entityA, ContentKeys.RENDERABLE_ASSET_REF, new RenderableAssetRef(meshA, material));
        world.add(entityB, ContentKeys.RENDERABLE_ASSET_REF, new RenderableAssetRef(meshB, material));

        resolveSystem.run(world);

        ResolvedRenderable resolvedA = world.get(entityA, ResolvedKeys.RESOLVED_RENDERABLE).orElseThrow();
        ResolvedRenderable resolvedB = world.get(entityB, ResolvedKeys.RESOLVED_RENDERABLE).orElseThrow();

        assertEquals(resolvedA.meshHandle(), resolvedB.meshHandle());
        assertEquals(1, uploader.uploadCount());

        int expectedHandle = FakeMeshUploadService.deterministicMeshHandle(cookResult.contentHash64());
        assertEquals(expectedHandle, resolvedA.meshHandle());
    }
}
