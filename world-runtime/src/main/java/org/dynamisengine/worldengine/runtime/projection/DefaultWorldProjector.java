package org.dynamisengine.worldengine.runtime.projection;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.core.exception.DynamisException;
import org.dynamisengine.scenegraph.api.SceneNodeId;
import org.dynamisengine.scenegraph.core.DefaultSceneGraph;
import org.dynamisengine.worldengine.api.WorldContext;
import org.dynamisengine.worldengine.api.lifecycle.WorldProjector;
import org.dynamisengine.worldengine.runtime.projection.components.BoundsSphereComponent;
import org.dynamisengine.worldengine.runtime.projection.components.RenderableComponent;
import org.dynamisengine.worldengine.runtime.projection.components.TranslationComponent;
import org.vectrix.affine.Transformf;
import org.vectrix.core.Vector3f;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultWorldProjector implements WorldProjector {

    private final Map<EntityId, SceneNodeId> nodeByEntity = new ConcurrentHashMap<>();

    @Override
    public void project(WorldContext ctx) {
        if (!(ctx.sceneGraph() instanceof DefaultSceneGraph graph)) {
            throw new DynamisException("DefaultWorldProjector requires DefaultSceneGraph");
        }

        for (EntityId entity : ctx.world().entities()) {
            Optional<TranslationComponent> translation = ctx.world().get(entity, ProjectionKeys.TRANSLATION);
            Optional<BoundsSphereComponent> bounds = ctx.world().get(entity, ProjectionKeys.BOUNDS);
            Optional<RenderableComponent> renderable = ctx.world().get(entity, ProjectionKeys.RENDERABLE);
            if (translation.isEmpty() || bounds.isEmpty() || renderable.isEmpty()) {
                continue;
            }

            SceneNodeId nodeId = nodeByEntity.computeIfAbsent(entity, ignored -> graph.createNode());

            TranslationComponent t = translation.orElseThrow();
            Transformf transform = new Transformf();
            transform.translation.set(t.x(), t.y(), t.z());
            graph.setLocalTransform(nodeId, transform);

            BoundsSphereComponent b = bounds.orElseThrow();
            graph.setLocalBoundsSphere(nodeId, new Vector3f(b.cx(), b.cy(), b.cz()), b.radius());

            RenderableComponent r = renderable.orElseThrow();
            graph.bindRenderable(nodeId, Integer.valueOf(r.meshHandle()), r.materialKey());
        }

        nodeByEntity.keySet().removeIf(entity -> !ctx.world().exists(entity));
    }
}
