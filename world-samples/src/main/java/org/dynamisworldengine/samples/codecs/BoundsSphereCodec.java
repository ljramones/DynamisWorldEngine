package org.dynamisworldengine.samples.codecs;

import org.dynamissession.api.codec.ComponentCodec;
import org.dynamisworldengine.runtime.projection.components.BoundsSphereComponent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class BoundsSphereCodec implements ComponentCodec<BoundsSphereComponent> {

    @Override
    public String keyId() {
        return "demo.boundsSphere";
    }

    @Override
    public Class<BoundsSphereComponent> type() {
        return BoundsSphereComponent.class;
    }

    @Override
    public byte[] encode(BoundsSphereComponent value) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeFloat(value.cx());
            out.writeFloat(value.cy());
            out.writeFloat(value.cz());
            out.writeFloat(value.radius());
            out.flush();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to encode BoundsSphereComponent", e);
        }
    }

    @Override
    public BoundsSphereComponent decode(byte[] bytes) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
            return new BoundsSphereComponent(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decode BoundsSphereComponent", e);
        }
    }
}
