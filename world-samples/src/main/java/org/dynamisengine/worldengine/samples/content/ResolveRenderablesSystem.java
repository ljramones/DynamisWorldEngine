package org.dynamisengine.worldengine.samples.content;

import org.dynamisengine.ecs.api.world.World;

import java.util.Objects;

public final class ResolveRenderablesSystem {
    private final RenderableResolver resolver;

    public ResolveRenderablesSystem(RenderableResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    public void run(World world) {
        Objects.requireNonNull(world, "world");

        for (var entity : world.entities()) {
            var ref = world.get(entity, ContentKeys.RENDERABLE_ASSET_REF);
            if (ref.isEmpty()) {
                continue;
            }
            world.add(entity, ResolvedKeys.RESOLVED_RENDERABLE, resolver.resolve(ref.orElseThrow()));
        }
    }
}
