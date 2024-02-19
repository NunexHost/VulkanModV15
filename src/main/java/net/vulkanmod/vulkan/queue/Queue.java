package net.vulkanmod.vulkan.queue;

import net.vulkanmod.vulkan.DeviceManager;
import net.vulkanmod.vulkan.Synchronization;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.util.VUtil;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public enum Queue {
    GraphicsQueue(QueueFamilyIndices.graphicsFamily, true),
    TransferQueue(QueueFamilyIndices.transferFamily, true),
    PresentQueue(QueueFamilyIndices.presentFamily, false);
    private final int familyIndex;
    private CommandPool.CommandBuffer currentCmdBuffer;
    private final CommandPool commandPool;
    private final VkQueue queue;

    public CommandPool.CommandBuffer beginCommands() {
        return this.commandPool.beginCommands();
    }

    Queue(int familyIndex, boolean initCommandPool) {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            PointerBuffer pQueue = stack.mallocPointer(1);
            this.familyIndex = familyIndex;
            vkGetDeviceQueue(DeviceManager.device, this.familyIndex, 0, pQueue);
            this.queue = new VkQueue(pQueue.get(0), DeviceManager.device);

            this.commandPool = initCommandPool ? new CommandPool(this.familyIndex) : null;
        }
    }

    public long submitCommands(CommandPool.CommandBuffer commandBuffer) {
        return this.commandPool.submitCommands(commandBuffer, queue);
    }

    public VkQueue queue() { return this.queue; }

    public void cleanUp() {
        if(commandPool != null)
            commandPool.cleanUp();
    }

    public void waitIdle() {
        vkQueueWaitIdle(queue);
    }


    public long copyBufferCmd(long srcBuffer, long srcOffset, long dstBuffer, long dstOffset, long size) {

        try(MemoryStack stack = stackPush()) {

            CommandPool.CommandBuffer commandBuffer = beginCommands();

            VkBufferCopy.Buffer copyRegion = VkBufferCopy.malloc(1, stack);
            copyRegion.size(size);
            copyRegion.srcOffset(srcOffset);
            copyRegion.dstOffset(dstOffset);

            vkCmdCopyBuffer(commandBuffer.getHandle(), srcBuffer, dstBuffer, copyRegion);

            this.submitCommands(commandBuffer);
            Synchronization.INSTANCE.addCommandBuffer(commandBuffer);

            return commandBuffer.fence;
        }
    }

    public void uploadBufferImmediate(long srcBuffer, long srcOffset, long dstBuffer, long dstOffset, long size) {

        try(MemoryStack stack = stackPush()) {
            CommandPool.CommandBuffer commandBuffer = this.beginCommands();

//            if(Initializer.CONFIG.useGigaBarriers) this.GigaBarrier(commandBuffer.getHandle());
            VkBufferCopy.Buffer copyRegion = VkBufferCopy.malloc(1, stack);
            copyRegion.size(size);
            copyRegion.srcOffset(srcOffset);
            copyRegion.dstOffset(dstOffset);
//            this.GigaBarrier(commandBuffer.getHandle());
            vkCmdCopyBuffer(commandBuffer.getHandle(), srcBuffer, dstBuffer, copyRegion);

            this.submitCommands(commandBuffer);
            vkWaitForFences(Vulkan.getDevice(), commandBuffer.fence, true, VUtil.UINT64_MAX);
            commandBuffer.reset();
        }
    }

    public void uploadBufferCmd(VkCommandBuffer commandBuffer, long srcBuffer, long srcOffset, long dstBuffer, long dstOffset, long size) {

        try(MemoryStack stack = stackPush()) {

            VkBufferCopy.Buffer copyRegion = VkBufferCopy.malloc(1, stack);
            copyRegion.size(size);
            copyRegion.srcOffset(srcOffset);
            copyRegion.dstOffset(dstOffset);

            vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, copyRegion);
        }
    }

    public void uploadBufferCmds(CommandPool.CommandBuffer commandBuffer, long srcBuffer, long dstBuffer, VkBufferCopy.Buffer vkBufferCopies) {
        vkCmdCopyBuffer(commandBuffer.getHandle(), srcBuffer, dstBuffer, vkBufferCopies);
    }

    public void startRecording() {
        currentCmdBuffer = beginCommands();
    }

    public void endRecordingAndSubmit() {
        long fence = submitCommands(currentCmdBuffer);
        Synchronization.INSTANCE.addCommandBuffer(currentCmdBuffer);

        currentCmdBuffer = null;
    }

    public CommandPool.CommandBuffer getCommandBuffer() {
        return currentCmdBuffer != null ? currentCmdBuffer : beginCommands();
    }

    public long endIfNeeded(CommandPool.CommandBuffer commandBuffer) {
        return currentCmdBuffer != null ? VK_NULL_HANDLE : submitCommands(commandBuffer);
    }

    public void trimCmdPool()
    {
        if(commandPool==null) return;
        VK11.vkTrimCommandPool(Vulkan.getDevice(), this.commandPool.id, 0);
    }

    public static void trimCmdPools()
    {
        for(var queue : Queue.values()) {
            queue.trimCmdPool();
        }
    }

    public void fillBuffer(long id, int bufferSize, int qNaN) {
        vkCmdFillBuffer(this.getCommandBuffer().getHandle(), id, 0, bufferSize, qNaN);
    }

    public void BufferBarrier(VkCommandBuffer commandBuffer, long bufferhdle, int size_t) {

        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferMemoryBarrier.Buffer memBarrier = VkBufferMemoryBarrier.calloc(1, stack)
                    .sType$Default()
                    .buffer(bufferhdle)
                    .srcQueueFamilyIndex(this.familyIndex)
                    .dstQueueFamilyIndex(familyIndex)
                    .srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
                    .dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
                    .size(size_t);

            vkCmdPipelineBarrier(commandBuffer,
                    VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT,
                    0,
                    null,
                    memBarrier,
                    null);

        }
    }

    public void GigaBarrier(VkCommandBuffer commandBuffer) {

        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkMemoryBarrier.Buffer memBarrier = VkMemoryBarrier.calloc(1, stack);
            memBarrier.sType$Default();
            memBarrier.srcAccessMask(VK_ACCESS_MEMORY_WRITE_BIT);
            memBarrier.dstAccessMask(VK_ACCESS_MEMORY_READ_BIT|VK_ACCESS_MEMORY_WRITE_BIT);

            vkCmdPipelineBarrier(commandBuffer,
                    VK_PIPELINE_STAGE_VERTEX_INPUT_BIT|VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_VERTEX_INPUT_BIT|VK_PIPELINE_STAGE_TRANSFER_BIT,
                    0,
                    memBarrier,
                    null,
                    null);
        }
    }
}

