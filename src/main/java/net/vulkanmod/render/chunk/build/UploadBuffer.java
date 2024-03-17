package net.vulkanmod.render.chunk.build;

import net.vulkanmod.render.vertex.TerrainBufferBuilder;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class UploadBuffer {

    public final int indexCount;
    public final boolean autoIndices;
    public final boolean indexOnly;
    private final ByteBuffer vertexBuffer;
    private final IntBuffer indexBuffer;

    //debug
    private boolean released = false;

    public UploadBuffer(TerrainBufferBuilder.RenderedBuffer renderedBuffer) {
        TerrainBufferBuilder.DrawState drawState = renderedBuffer.drawState();
        this.indexCount = drawState.indexCount();
        this.autoIndices = drawState.sequentialIndex();
        this.indexOnly = drawState.indexOnly();

        if(!this.indexOnly) {
            this.vertexBuffer = MemoryUtil.memAlloc(renderedBuffer.vertexBuffer().capacity());
            renderedBuffer.vertexBuffer().copyTo(this.vertexBuffer);
        } else {
            this.vertexBuffer = null;
        }

        if(!drawState.sequentialIndex()) {
            this.indexBuffer = MemoryUtil.memAlloc(renderedBuffer.indexBuffer().capacity() * 4);
            renderedBuffer.indexBuffer().copyTo(this.indexBuffer);
        } else {
            this.indexBuffer = null;
        }
    }

    public int indexCount() { return indexCount; }

    public ByteBuffer getVertexBuffer() { return vertexBuffer; }

    public IntBuffer getIndexBuffer() { return indexBuffer; }

    public void release() {
        if(vertexBuffer != null)
            MemoryUtil.memFree(vertexBuffer);
        if(indexBuffer != null)
            MemoryUtil.memFree(indexBuffer);
        this.released = true;
    }
}
