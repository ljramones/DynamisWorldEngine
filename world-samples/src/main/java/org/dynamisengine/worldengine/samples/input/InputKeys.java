package org.dynamisengine.worldengine.samples.input;

import org.dynamisengine.ecs.api.component.ComponentKey;

public final class InputKeys {
    private InputKeys() {
    }

    public static final ComponentKey<MoveIntent> MOVE_INTENT =
            ComponentKey.of("world.moveIntent", MoveIntent.class);
}
