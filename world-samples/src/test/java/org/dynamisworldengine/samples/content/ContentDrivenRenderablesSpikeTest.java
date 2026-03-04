package org.dynamisworldengine.samples.content;

import org.dynamiscontent.api.id.AssetId;
import org.dynamiscontent.api.id.AssetType;
import org.dynamiscontent.api.id.AssetTypes;
import org.dynamiscontent.api.loader.AssetLoader;
import org.dynamiscontent.api.loader.AssetResolver;
import org.dynamiscontent.api.manifest.DmeshBlob;
import org.dynamiscontent.api.manifest.ManifestEntry;
import org.dynamiscontent.core.manifest.AssetManifestBuilder;
import org.dynamiscontent.runtime.ContentRuntime;
import org.dynamisecs.core.DefaultWorld;
import org.dynamisscenegraph.api.extract.RenderKey;
import org.dynamisscenegraph.core.DefaultSceneGraph;
import org.dynamisworldengine.runtime.projection.ProjectionKeys;
import org.dynamisworldengine.runtime.projection.components.BoundsSphereComponent;
import org.dynamisworldengine.runtime.projection.components.TranslationComponent;
import org.dynamisworldengine.samples.content.upload.FakeMeshUploadService;
import org.dynamisworldengine.samples.render.FakeEngineRuntime;
import org.dynamisworldengine.samples.render.SceneGraphToLightEngineAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContentDrivenRenderablesSpikeTest {

    private static final AssetId MESH_CUBE = AssetId.of("mesh/cube");
    private static final AssetId MAT_DEFAULT = AssetId.of("mat/default");

    @Test
    void assetIdRenderableRefsShouldResolveToSingleInstanceBatchAndSyncToEngine() throws Exception {
        ContentRuntime runtime = ContentRuntime.builder()
                .manifest(new AssetManifestBuilder()
                        .add(MESH_CUBE, AssetTypes.DMESH_BLOB.id(), "unused")
                        .build())
                .loader(new TestDmeshLoader())
                .build();

        FakeMeshUploadService uploader = new FakeMeshUploadService();
        RenderableResolver resolver = new RenderableResolver(runtime, uploader);
        ResolveRenderablesSystem resolveSystem = new ResolveRenderablesSystem(resolver);
        ContentDrivenProjector projector = new ContentDrivenProjector();

        int entityCount = 4;
        DefaultWorld world = new DefaultWorld();
        for (int i = 0; i < entityCount; i++) {
            var entity = world.createEntity();
            world.add(entity, ProjectionKeys.TRANSLATION, new TranslationComponent(i, 0f, 0f));
            world.add(entity, ProjectionKeys.BOUNDS, new BoundsSphereComponent(0f, 0f, 0f, 0.5f));
            world.add(entity, ContentKeys.RENDERABLE_ASSET_REF, new RenderableAssetRef(MESH_CUBE, MAT_DEFAULT));
        }

        resolveSystem.run(world);
        for (var entity : world.entities()) {
            ResolvedRenderable resolved = world.get(entity, ResolvedKeys.RESOLVED_RENDERABLE).orElseThrow();
            assertEquals(1, resolved.meshHandle());
            assertEquals("mat/default", resolved.materialKey());
        }
        assertEquals(1, uploader.uploadCount());

        DefaultSceneGraph sceneGraph = new DefaultSceneGraph();
        projector.project(world, sceneGraph);

        var batched = sceneGraph.extractBatched();
        assertEquals(1, batched.batches().size());
        var batch = batched.batches().getFirst();
        assertEquals(entityCount, batch.instanceCount());
        assertEquals(RenderKey.of(Integer.valueOf(1), "mat/default"), batch.key());

        FakeEngineRuntime engine = new FakeEngineRuntime();
        SceneGraphToLightEngineAdapter adapter = new SceneGraphToLightEngineAdapter();

        adapter.sync(engine, batched);
        assertEquals(1, engine.registerCalls());
        assertEquals(0, engine.updateCalls());
        assertEquals(1, engine.lastRegisteredMeshHandle());
        assertEquals(entityCount, engine.lastRegisteredRows());
        assertEquals(16, engine.lastRegisteredCols());

        adapter.sync(engine, batched);
        assertEquals(1, engine.registerCalls());
        assertEquals(1, engine.updateCalls());
        assertEquals(1, engine.lastUpdatedHandle());
        assertEquals(entityCount, engine.lastUpdatedRows());
        assertEquals(16, engine.lastUpdatedCols());
    }

    private static final class TestDmeshLoader implements AssetLoader<DmeshBlob> {
        @Override
        public AssetType<DmeshBlob> type() {
            return AssetTypes.DMESH_BLOB;
        }

        @Override
        public DmeshBlob load(AssetId id, ManifestEntry entry, AssetResolver resolver) {
            return new DmeshBlob(1, new byte[]{1, 2, 3}, 0x0000000100000000L);
        }
    }
}
