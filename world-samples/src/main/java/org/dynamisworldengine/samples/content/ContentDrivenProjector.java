package org.dynamisworldengine.samples.content;

import org.dynamis.core.entity.EntityId;
import org.dynamisscenegraph.api.SceneNodeId;
import org.dynamisscenegraph.core.DefaultSceneGraph;
import org.dynamisecs.api.world.World;
import org.dynamisworldengine.runtime.projection.ProjectionKeys;
import org.vectrix.affine.Transformf;
import org.vectrix.core.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ContentDrivenProjector {
    private final Map<EntityId, SceneNodeId> nodeByEntity = new ConcurrentHashMap<>();

    public void project(World world, DefaultSceneGraph graph) {
        for (EntityId entity : world.entities()) {
            var translation = world.get(entity, ProjectionKeys.TRANSLATION);
            var bounds = world.get(entity, ProjectionKeys.BOUNDS);
            var renderable = world.get(entity, ResolvedKeys.RESOLVED_RENDERABLE);
            if (translation.isEmpty() || bounds.isEmpty() || renderable.isEmpty()) {
                continue;
            }

            SceneNodeId nodeId = nodeByEntity.computeIfAbsent(entity, ignored -> graph.createNode());

            var t = translation.orElseThrow();
            Transformf transform = new Transformf();
            transform.translation.set(t.x(), t.y(), t.z());
            graph.setLocalTransform(nodeId, transform);

            var b = bounds.orElseThrow();
            graph.setLocalBoundsSphere(nodeId, new Vector3f(b.cx(), b.cy(), b.cz()), b.radius());

            var r = renderable.orElseThrow();
            graph.bindRenderable(nodeId, Integer.valueOf(r.meshHandle()), r.materialKey());
        }

        nodeByEntity.keySet().removeIf(entity -> !world.exists(entity));
    }
}
