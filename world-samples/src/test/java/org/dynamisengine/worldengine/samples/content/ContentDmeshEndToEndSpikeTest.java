package org.dynamisengine.worldengine.samples.content;

import org.dynamisengine.assetpipeline.api.CookProfile;
import org.dynamisengine.assetpipeline.api.mesh.MeshCookRequest;
import org.dynamisengine.assetpipeline.api.mesh.MeshCookResult;
import org.dynamisengine.assetpipeline.core.mesh.MeshCooker;
import org.dynamisengine.content.api.id.AssetId;
import org.dynamisengine.ecs.core.DefaultWorld;
import org.dynamisengine.scenegraph.api.extract.RenderKey;
import org.dynamisengine.scenegraph.core.DefaultSceneGraph;
import org.dynamisengine.worldengine.runtime.projection.ProjectionKeys;
import org.dynamisengine.worldengine.runtime.projection.components.BoundsSphereComponent;
import org.dynamisengine.worldengine.runtime.projection.components.TranslationComponent;
import org.dynamisengine.worldengine.samples.content.upload.FakeMeshUploadService;
import org.dynamisengine.worldengine.samples.render.FakeEngineRuntime;
import org.dynamisengine.worldengine.samples.render.SceneGraphToLightEngineAdapter;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContentDmeshEndToEndSpikeTest {

    private static final AssetId MESH_CUBE = AssetId.of("mesh/cube");
    private static final AssetId MAT_DEFAULT = AssetId.of("mat/default");

    @Test
    void contentShouldLoadCookedDmeshAndProduceDeterministicMeshHandleForBatchIngestion() throws Exception {
        RunResult first = runFlow(Files.createTempDirectory("phase8-a"), 10);
        RunResult second = runFlow(Files.createTempDirectory("phase8-b"), 10);

        assertEquals(first.meshHandle(), second.meshHandle());
        assertEquals(first.batchCount(), second.batchCount());
        assertEquals(first.instanceCount(), second.instanceCount());
        assertEquals(first.registerCalls(), second.registerCalls());
        assertEquals(first.updateCalls(), second.updateCalls());
        assertEquals(first.uploadCount(), second.uploadCount());
    }

    private static RunResult runFlow(Path tempDir, int entityCount) throws Exception {
        Path sourceObj = tempDir.resolve("cube.obj");
        Files.writeString(sourceObj, """
                v 0 0 0
                v 1 0 0
                v 0 1 0
                vn 0 0 1
                f 1//1 2//1 3//1
                """);

        Path cookedDir = tempDir.resolve("cooked");
        MeshCookResult cookResult = new MeshCooker().cook(new MeshCookRequest(
                MESH_CUBE.value(),
                sourceObj,
                CookProfile.REALTIME_FAST,
                cookedDir
        ));

        Path manifestPath = tempDir.resolve("manifest.json");
        Files.writeString(manifestPath, """
                {
                  "version": 1,
                  "entries": [
                    {
                      "id": "mesh/cube",
                      "typeId": "mesh.packed.dmesh.v0",
                      "uri": "cooked/mesh/cube.dmesh",
                      "dependencies": []
                    }
                  ]
                }
                """);

        var runtime = org.dynamisengine.content.runtime.ContentRuntime.builder()
                .baseDir(tempDir)
                .manifest(manifestPath)
                .registerDefaultLoaders()
                .build();

        FakeMeshUploadService uploader = new FakeMeshUploadService();
        RenderableResolver resolver = new RenderableResolver(runtime, uploader);
        ResolveRenderablesSystem resolveSystem = new ResolveRenderablesSystem(resolver);
        ContentDrivenProjector projector = new ContentDrivenProjector();

        DefaultWorld world = new DefaultWorld();
        for (int i = 0; i < entityCount; i++) {
            var entity = world.createEntity();
            world.add(entity, ProjectionKeys.TRANSLATION, new TranslationComponent(i, 0f, 0f));
            world.add(entity, ProjectionKeys.BOUNDS, new BoundsSphereComponent(0f, 0f, 0f, 0.5f));
            world.add(entity, ContentKeys.RENDERABLE_ASSET_REF, new RenderableAssetRef(MESH_CUBE, MAT_DEFAULT));
        }

        resolveSystem.run(world);

        int expectedMeshHandle = FakeMeshUploadService.deterministicMeshHandle(cookResult.contentHash64());
        for (var entity : world.entities()) {
            ResolvedRenderable resolved = world.get(entity, ResolvedKeys.RESOLVED_RENDERABLE).orElseThrow();
            assertEquals(expectedMeshHandle, resolved.meshHandle());
            assertEquals(MAT_DEFAULT.value(), resolved.materialKey());
        }
        assertEquals(1, uploader.uploadCount());

        DefaultSceneGraph sceneGraph = new DefaultSceneGraph();
        projector.project(world, sceneGraph);
        var batched = sceneGraph.extractBatched();

        assertEquals(1, batched.batches().size());
        var batch = batched.batches().getFirst();
        assertEquals(entityCount, batch.instanceCount());
        assertEquals(RenderKey.of(Integer.valueOf(expectedMeshHandle), MAT_DEFAULT.value()), batch.key());

        FakeEngineRuntime engine = new FakeEngineRuntime();
        SceneGraphToLightEngineAdapter adapter = new SceneGraphToLightEngineAdapter();

        adapter.sync(engine, batched);
        assertEquals(1, engine.registerCalls());
        assertEquals(0, engine.updateCalls());
        assertEquals(expectedMeshHandle, engine.lastRegisteredMeshHandle());
        assertEquals(entityCount, engine.lastRegisteredRows());
        assertEquals(16, engine.lastRegisteredCols());

        adapter.sync(engine, batched);
        assertEquals(1, engine.registerCalls());
        assertEquals(1, engine.updateCalls());
        assertEquals(1, engine.lastUpdatedHandle());
        assertEquals(entityCount, engine.lastUpdatedRows());
        assertEquals(16, engine.lastUpdatedCols());

        return new RunResult(expectedMeshHandle, batched.batches().size(), batch.instanceCount(),
                engine.registerCalls(), engine.updateCalls(), uploader.uploadCount());
    }

    private record RunResult(
            int meshHandle,
            int batchCount,
            int instanceCount,
            int registerCalls,
            int updateCalls,
            int uploadCount
    ) {
    }
}
