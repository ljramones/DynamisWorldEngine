package org.dynamisworldengine.samples.input;

import org.dynamisecs.api.component.ComponentKey;

public final class InputKeys {
    private InputKeys() {
    }

    public static final ComponentKey<MoveIntent> MOVE_INTENT =
            ComponentKey.of("world.moveIntent", MoveIntent.class);
}
