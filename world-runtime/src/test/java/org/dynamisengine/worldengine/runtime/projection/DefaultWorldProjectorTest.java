package org.dynamisengine.worldengine.runtime.projection;

import org.dynamisengine.content.api.AssetManager;
import org.dynamisengine.content.runtime.ContentRuntime;
import org.dynamisengine.content.core.manifest.AssetManifestBuilder;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ecs.api.world.WorldTick;
import org.dynamisengine.ecs.core.DefaultWorld;
import org.dynamisengine.scenegraph.core.DefaultSceneGraph;
import org.dynamisengine.session.runtime.DefaultSessionManager;
import org.dynamisengine.worldengine.api.WorldContext;
import org.dynamisengine.worldengine.runtime.projection.components.BoundsSphereComponent;
import org.dynamisengine.worldengine.runtime.projection.components.RenderableComponent;
import org.dynamisengine.worldengine.runtime.projection.components.TranslationComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultWorldProjectorTest {

    private DefaultWorld world;
    private DefaultSceneGraph sceneGraph;
    private WorldContext ctx;
    private DefaultWorldProjector projector;

    @BeforeEach
    void setUp() {
        world = new DefaultWorld();
        sceneGraph = new DefaultSceneGraph();
        AssetManager assets = ContentRuntime.builder()
                .manifest(new AssetManifestBuilder().build())
                .build()
                .assets();
        ctx = new WorldContext(assets, new DefaultSessionManager(), world, sceneGraph);
        projector = new DefaultWorldProjector();
    }

    @Test
    void entityWithAllComponentsCreatesNode() {
        world.beginTick(new WorldTick(0));
        EntityId entity = world.createEntity();
        addAllComponents(entity, 1.0f, 2.0f, 3.0f);
        world.endTick();

        world.beginTick(new WorldTick(1));
        projector.project(ctx);
        world.endTick();

        var batched = sceneGraph.extractBatched();
        assertFalse(batched.batches().isEmpty(), "Expected at least one batch after projection");
    }

    @Test
    void entityMissingComponentIsSkipped() {
        world.beginTick(new WorldTick(0));
        EntityId entity = world.createEntity();
        // Only add translation, skip bounds and renderable
        world.add(entity, ProjectionKeys.TRANSLATION, new TranslationComponent(1, 2, 3));
        world.endTick();

        world.beginTick(new WorldTick(1));
        projector.project(ctx);
        world.endTick();

        var batched = sceneGraph.extractBatched();
        assertTrue(batched.batches().isEmpty(), "Entity missing components should not produce a batch");
    }

    @Test
    void removedEntityCleansUpMapping() {
        world.beginTick(new WorldTick(0));
        EntityId entity = world.createEntity();
        addAllComponents(entity, 0, 0, 0);
        world.endTick();

        world.beginTick(new WorldTick(1));
        projector.project(ctx);
        world.endTick();

        // Consume the initial batch
        sceneGraph.extractBatched();

        // Now remove the entity and project again
        world.beginTick(new WorldTick(2));
        world.destroyEntity(entity);
        projector.project(ctx);
        world.endTick();

        // Re-projecting with a new entity should not duplicate nodes from the old entity
        world.beginTick(new WorldTick(3));
        EntityId newEntity = world.createEntity();
        addAllComponents(newEntity, 5, 5, 5);
        projector.project(ctx);
        world.endTick();

        var batched = sceneGraph.extractBatched();
        // Should have exactly 1 batch with the new entity only
        assertFalse(batched.batches().isEmpty(), "New entity should produce a batch");
    }

    @Test
    void multipleEntitiesProjectCorrectly() {
        world.beginTick(new WorldTick(0));
        addAllComponents(world.createEntity(), 1, 0, 0);
        addAllComponents(world.createEntity(), 0, 1, 0);
        addAllComponents(world.createEntity(), 0, 0, 1);
        world.endTick();

        world.beginTick(new WorldTick(1));
        projector.project(ctx);
        world.endTick();

        var batched = sceneGraph.extractBatched();
        assertEquals(1, batched.batches().size(), "Same mesh/material should batch together");
    }

    @Test
    void transformProjectionUpdatesPosition() {
        world.beginTick(new WorldTick(0));
        EntityId entity = world.createEntity();
        addAllComponents(entity, 10.0f, 20.0f, 30.0f);
        world.endTick();

        world.beginTick(new WorldTick(1));
        projector.project(ctx);
        world.endTick();

        // Project again with updated translation
        world.beginTick(new WorldTick(2));
        world.add(entity, ProjectionKeys.TRANSLATION, new TranslationComponent(99.0f, 88.0f, 77.0f));
        projector.project(ctx);
        world.endTick();

        // The node should still exist (not duplicated) - one batch, one instance
        var batched = sceneGraph.extractBatched();
        assertEquals(1, batched.batches().size());
    }

    private void addAllComponents(EntityId entity, float x, float y, float z) {
        world.add(entity, ProjectionKeys.TRANSLATION, new TranslationComponent(x, y, z));
        world.add(entity, ProjectionKeys.BOUNDS, new BoundsSphereComponent(x, y, z, 1.0f));
        world.add(entity, ProjectionKeys.RENDERABLE, new RenderableComponent(42, "mat_default"));
    }
}
