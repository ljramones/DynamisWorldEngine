package org.dynamisworldengine.samples.render;

import org.dynamislight.api.error.EngineException;
import org.dynamislight.api.runtime.EngineRuntime;
import org.dynamisscenegraph.api.extract.BatchedRenderScene;
import org.dynamisscenegraph.api.extract.InstanceBatch;
import org.dynamisscenegraph.api.extract.RenderKey;
import org.vectrix.core.Matrix4f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class SceneGraphToLightEngineAdapter {

    private final Map<RenderKey, Integer> batchHandleByKey = new HashMap<>();

    public void sync(EngineRuntime engine, BatchedRenderScene scene) throws EngineException {
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(scene, "scene");

        Set<RenderKey> seen = new HashSet<>();

        for (InstanceBatch batch : scene.batches()) {
            RenderKey key = batch.key();
            seen.add(key);

            int meshHandle = meshHandle(key);
            float[][] matrices = toMatrixArray(batch);

            Integer handle = batchHandleByKey.get(key);
            if (handle == null) {
                int created = engine.registerInstanceBatch(meshHandle, matrices);
                batchHandleByKey.put(key, created);
            } else {
                engine.updateInstanceBatch(handle, matrices);
            }
        }

        batchHandleByKey.entrySet().removeIf(entry -> {
            if (seen.contains(entry.getKey())) {
                return false;
            }
            try {
                engine.removeInstanceBatch(entry.getValue());
                return true;
            } catch (EngineException e) {
                throw new RuntimeEngineException(e);
            }
        });
    }

    public void removeAll(EngineRuntime engine) throws EngineException {
        Objects.requireNonNull(engine, "engine");

        for (Integer handle : batchHandleByKey.values()) {
            engine.removeInstanceBatch(handle);
        }
        batchHandleByKey.clear();
    }

    private static int meshHandle(RenderKey key) {
        Object value = key.meshHandle();
        if (!(value instanceof Integer mesh)) {
            throw new IllegalArgumentException("Expected RenderKey meshHandle to be Integer but was: " + value);
        }
        return mesh;
    }

    private static float[][] toMatrixArray(InstanceBatch batch) {
        float[][] out = new float[batch.worldMatrices().size()][16];
        for (int i = 0; i < batch.worldMatrices().size(); i++) {
            Matrix4f matrix = batch.worldMatrices().get(i);
            out[i] = matrix.get(new float[16]);
        }
        return out;
    }

    private static final class RuntimeEngineException extends RuntimeException {
        private RuntimeEngineException(Throwable cause) {
            super(cause);
        }
    }
}
