package net.vulkanmod.render.vertex;

import net.minecraft.client.renderer.RenderType;

import java.util.EnumSet;

public enum TerrainRenderType {
    SOLID(RenderType.solid(), 262144 /*BIG_BUFFER_SIZE*/),
    CUTOUT_MIPPED(RenderType.cutoutMipped(), 262144 /*MEDIUM_BUFFER_SIZE*/),
    CUTOUT(RenderType.cutout(), 131072 /*SMALL_BUFFER_SIZE*/),
    TRANSLUCENT(RenderType.translucent(), 131072 /*SMALL_BUFFER_SIZE*/),
    TRIPWIRE(RenderType.tripwire(), 131072 /*SMALL_BUFFER_SIZE*/);

    public static final TerrainRenderType[] VALUES = TerrainRenderType.values();

    public static final EnumSet<TerrainRenderType> ALL_RENDER_TYPES = EnumSet.of(CUTOUT_MIPPED, CUTOUT, TRANSLUCENT);

    public final int bufferSize;
    public final int initialSize;

    TerrainRenderType(RenderType renderType, int initialSize) {
        this.bufferSize = renderType.bufferSize();
        this.initialSize = initialSize;
    }
    public static EnumSet<TerrainRenderType> getActiveLayers() {
        return ALL_RENDER_TYPES;
    }

    public static TerrainRenderType getCompact(String renderType) {
        return switch (renderType)
        {
            case "solid", "cutout_mipped" -> CUTOUT_MIPPED;
            case "cutout" -> CUTOUT;
            default -> TRANSLUCENT;
        };


    }



    public static TerrainRenderType get(String renderType) {
        return switch (renderType)
        {
            case "solid" -> SOLID;
            case "cutout_mipped" -> CUTOUT_MIPPED;
            case "cutout" -> CUTOUT;
            case "tripwire" -> TRIPWIRE;
            default -> TRANSLUCENT;
        };
    }

    public int bitMask() {
        return 1 << this.ordinal();
    }
}
