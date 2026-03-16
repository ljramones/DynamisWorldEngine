package org.dynamisengine.worldengine.samples.codecs;

import org.dynamisengine.worldengine.runtime.projection.components.BoundsSphereComponent;
import org.dynamisengine.worldengine.runtime.projection.components.RenderableComponent;
import org.dynamisengine.worldengine.runtime.projection.components.TranslationComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodecRoundtripTest {

    private final TranslationCodec translationCodec = new TranslationCodec();
    private final BoundsSphereCodec boundsSphereCodec = new BoundsSphereCodec();
    private final RenderableCodec renderableCodec = new RenderableCodec();

    // ── TranslationCodec ────────────────────────────────────────────

    @Test
    void translationRoundtrip() {
        var original = new TranslationComponent(1.5f, -2.3f, 100.0f);
        var decoded = translationCodec.decode(translationCodec.encode(original));
        assertEquals(original, decoded);
    }

    @Test
    void translationZeroValues() {
        var original = new TranslationComponent(0.0f, 0.0f, 0.0f);
        var decoded = translationCodec.decode(translationCodec.encode(original));
        assertEquals(original, decoded);
    }

    @Test
    void translationNegativeValues() {
        var original = new TranslationComponent(-999.9f, -0.001f, -Float.MAX_VALUE);
        var decoded = translationCodec.decode(translationCodec.encode(original));
        assertEquals(original, decoded);
    }

    @Test
    void translationLargeValues() {
        var original = new TranslationComponent(Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_NORMAL);
        var decoded = translationCodec.decode(translationCodec.encode(original));
        assertEquals(original, decoded);
    }

    @Test
    void translationNanRoundtrip() {
        var original = new TranslationComponent(Float.NaN, 0.0f, 1.0f);
        var decoded = translationCodec.decode(translationCodec.encode(original));
        assertEquals(Float.floatToRawIntBits(original.x()), Float.floatToRawIntBits(decoded.x()));
        assertEquals(original.y(), decoded.y());
        assertEquals(original.z(), decoded.z());
    }

    // ── BoundsSphereCodec ───────────────────────────────────────────

    @Test
    void boundsSphereRoundtrip() {
        var original = new BoundsSphereComponent(1.0f, 2.0f, 3.0f, 5.0f);
        var decoded = boundsSphereCodec.decode(boundsSphereCodec.encode(original));
        assertEquals(original, decoded);
    }

    @Test
    void boundsSphereZeroRadius() {
        var original = new BoundsSphereComponent(0.0f, 0.0f, 0.0f, 0.0f);
        var decoded = boundsSphereCodec.decode(boundsSphereCodec.encode(original));
        assertEquals(original, decoded);
    }

    @Test
    void boundsSphereNegativeCenter() {
        var original = new BoundsSphereComponent(-10.0f, -20.0f, -30.0f, 1.0f);
        var decoded = boundsSphereCodec.decode(boundsSphereCodec.encode(original));
        assertEquals(original, decoded);
    }

    @Test
    void boundsSphereNanHandling() {
        var original = new BoundsSphereComponent(Float.NaN, Float.NaN, 0.0f, Float.NaN);
        var decoded = boundsSphereCodec.decode(boundsSphereCodec.encode(original));
        assertEquals(Float.floatToRawIntBits(original.cx()), Float.floatToRawIntBits(decoded.cx()));
        assertEquals(Float.floatToRawIntBits(original.radius()), Float.floatToRawIntBits(decoded.radius()));
    }

    @Test
    void boundsSphereKeyId() {
        assertEquals("demo.boundsSphere", boundsSphereCodec.keyId());
        assertEquals(BoundsSphereComponent.class, boundsSphereCodec.type());
    }

    // ── RenderableCodec ─────────────────────────────────────────────

    @Test
    void renderableRoundtrip() {
        var original = new RenderableComponent(42, "stone_material");
        var decoded = renderableCodec.decode(renderableCodec.encode(original));
        assertEquals(original, decoded);
    }

    @Test
    void renderableZeroHandle() {
        var original = new RenderableComponent(0, "default");
        var decoded = renderableCodec.decode(renderableCodec.encode(original));
        assertEquals(original, decoded);
    }

    @Test
    void renderableNegativeHandle() {
        var original = new RenderableComponent(-1, "fallback");
        var decoded = renderableCodec.decode(renderableCodec.encode(original));
        assertEquals(original, decoded);
    }

    @Test
    void renderableLargeHandle() {
        var original = new RenderableComponent(Integer.MAX_VALUE, "max_mesh");
        var decoded = renderableCodec.decode(renderableCodec.encode(original));
        assertEquals(original, decoded);
    }

    @Test
    void renderableEmptyMaterialKey() {
        var original = new RenderableComponent(1, "");
        var decoded = renderableCodec.decode(renderableCodec.encode(original));
        assertEquals(original, decoded);
    }

    @Test
    void renderableKeyId() {
        assertEquals("demo.renderable", renderableCodec.keyId());
        assertEquals(RenderableComponent.class, renderableCodec.type());
    }

    @Test
    void translationKeyId() {
        assertEquals("demo.translation", translationCodec.keyId());
        assertEquals(TranslationComponent.class, translationCodec.type());
    }
}
