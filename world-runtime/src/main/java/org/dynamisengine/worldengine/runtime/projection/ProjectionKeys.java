package org.dynamisengine.worldengine.runtime.projection;

import org.dynamisengine.ecs.api.component.ComponentKey;
import org.dynamisengine.worldengine.runtime.projection.components.BoundsSphereComponent;
import org.dynamisengine.worldengine.runtime.projection.components.RenderableComponent;
import org.dynamisengine.worldengine.runtime.projection.components.TranslationComponent;

public final class ProjectionKeys {

    private ProjectionKeys() {
    }

    public static final ComponentKey<TranslationComponent> TRANSLATION =
            ComponentKey.of("demo.translation", TranslationComponent.class);

    public static final ComponentKey<BoundsSphereComponent> BOUNDS =
            ComponentKey.of("demo.boundsSphere", BoundsSphereComponent.class);

    public static final ComponentKey<RenderableComponent> RENDERABLE =
            ComponentKey.of("demo.renderable", RenderableComponent.class);
}
