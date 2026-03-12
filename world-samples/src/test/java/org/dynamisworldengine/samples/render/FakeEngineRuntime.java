package org.dynamisworldengine.samples.render;

import org.dynamislight.api.config.EngineConfig;
import org.dynamislight.api.error.EngineException;
import org.dynamislight.api.input.EngineInput;
import org.dynamislight.api.mesh.MeshUploadRequest;
import org.dynamislight.api.mesh.MeshUploadResult;
import org.dynamislight.api.runtime.EngineApiVersion;
import org.dynamislight.api.runtime.EngineCapabilities;
import org.dynamislight.api.runtime.EngineFrameResult;
import org.dynamislight.api.runtime.EngineHostCallbacks;
import org.dynamislight.api.runtime.EngineRuntime;
import org.dynamislight.api.runtime.EngineStats;
import org.dynamislight.api.runtime.FrameHandle;
import org.dynamislight.api.scene.SceneDescriptor;

import java.util.List;

public final class FakeEngineRuntime implements EngineRuntime {

    private int nextHandle = 1;
    private long nextFrameId = 1;

    private int registerCalls;
    private int updateCalls;
    private int removeCalls;

    private int lastRegisteredMeshHandle;
    private int lastRegisteredRows;
    private int lastRegisteredCols;
    private int lastUpdatedHandle;
    private int lastUpdatedRows;
    private int lastUpdatedCols;

    @Override
    public EngineApiVersion apiVersion() {
        return new EngineApiVersion(0, 1, 0);
    }

    @Override
    public void initialize(EngineConfig config, EngineHostCallbacks callbacks) {
    }

    @Override
    public void loadScene(SceneDescriptor sceneDescriptor) {
    }

    @Override
    public EngineFrameResult update(double deltaSeconds, EngineInput input) {
        return frameResult();
    }

    @Override
    public EngineFrameResult render() {
        return frameResult();
    }

    @Override
    public void updateSkinnedMesh(int meshId, float[] matrices) {
    }

    @Override
    public void updateMorphWeights(int meshId, float[] weights) {
    }

    @Override
    public int registerInstanceBatch(int meshHandle, float[][] matrices) {
        registerCalls++;
        lastRegisteredMeshHandle = meshHandle;
        lastRegisteredRows = matrices.length;
        lastRegisteredCols = matrices.length == 0 ? 0 : matrices[0].length;

        return nextHandle++;
    }

    @Override
    public MeshUploadResult registerMesh(MeshUploadRequest request) {
        return new MeshUploadResult(nextHandle++, false, request.meshId());
    }

    @Override
    public void removeMesh(int meshHandle) {
    }

    @Override
    public void updateInstanceBatch(int handle, float[][] matrices) {
        updateCalls++;
        lastUpdatedHandle = handle;
        lastUpdatedRows = matrices.length;
        lastUpdatedCols = matrices.length == 0 ? 0 : matrices[0].length;
    }

    @Override
    public void removeInstanceBatch(int handle) {
        removeCalls++;
    }

    @Override
    public void resize(int width, int height, float dpiScale) {
    }

    @Override
    public EngineStats getStats() {
        return new EngineStats(0.0, 0.0, 0.0, 0L, 0L, 0L, 0L, 0.0, 0.0, 0L);
    }

    @Override
    public EngineCapabilities getCapabilities() {
        return null;
    }

    @Override
    public void shutdown() {
    }

    public int registerCalls() {
        return registerCalls;
    }

    public int updateCalls() {
        return updateCalls;
    }

    public int removeCalls() {
        return removeCalls;
    }

    public int lastRegisteredMeshHandle() {
        return lastRegisteredMeshHandle;
    }

    public int lastRegisteredRows() {
        return lastRegisteredRows;
    }

    public int lastRegisteredCols() {
        return lastRegisteredCols;
    }

    public int lastUpdatedHandle() {
        return lastUpdatedHandle;
    }

    public int lastUpdatedRows() {
        return lastUpdatedRows;
    }

    public int lastUpdatedCols() {
        return lastUpdatedCols;
    }

    private EngineFrameResult frameResult() {
        long frameId = nextFrameId++;
        return new EngineFrameResult(frameId, 0.0, 0.0, new FrameHandle(frameId, false), List.of());
    }
}
