package net.vulkanmod.render.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.texture.VTextureSelector;
import net.vulkanmod.vulkan.texture.VulkanImage;
import org.joml.Matrix4f;

public class DrawUtil {

    private static final BufferBuilder builder = RenderSystem.renderThreadTesselator().getBuilder();
    private static final Matrix4f orthoMatrix = new Matrix4f().setOrtho(0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, true);

    public static void blitQuad() {
        blitQuad(0.0, 1.0, 1.0, 0.0);
    }

    public static void drawTexQuad(double x0, double y0, double x1, double y1, double z,
                                   float u0, float v0, float u1, float v1) {
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(x0, y0, z).uv(u0, v0).endVertex();
        builder.vertex(x1, y0, z).uv(u1, v0).endVertex();
        builder.vertex(x1, y1, z).uv(u1, v1).endVertex();
        builder.vertex(x0, y1, z).uv(u0, v1).endVertex();

        Renderer.getDrawer().draw(builder.end().vertexBuffer(), VertexFormat.Mode.QUADS);
    }

    public static void blitQuad(double x0, double y0, double x1, double y1) {
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(x0, y0, 0.0D).uv(0.0F, 1.0F).endVertex();
        builder.vertex(x1, y0, 0.0D).uv(1.0F, 1.0F).endVertex();
        builder.vertex(x1, y1, 0.0D).uv(1.0F, 0.0F).endVertex();
        builder.vertex(x0, y1, 0.0D).uv(0.0F, 0.0F).endVertex();

        Renderer.getDrawer().draw(builder.end().vertexBuffer(), VertexFormat.Mode.QUADS);
    }

    public static void drawFramebuffer(GraphicsPipeline pipeline, VulkanImage attachment) {

        boolean shouldUpdate = Renderer.getInstance().bindGraphicsPipeline(pipeline);

        VTextureSelector.bindTexture(attachment);

        RenderSystem.setProjectionMatrix(orthoMatrix, VertexSorting.DISTANCE_TO_ORIGIN);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.setIdentity();
        RenderSystem.applyModelViewMatrix();
        posestack.popPose();

        Renderer.getInstance().uploadAndBindUBOs(pipeline, shouldUpdate);

        blitQuad(0.0D, 0.0D, 1.0D, 1.0D);
    }
}
