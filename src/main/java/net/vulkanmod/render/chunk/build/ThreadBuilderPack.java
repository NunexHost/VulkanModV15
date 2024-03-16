package net.vulkanmod.render.chunk.build;

import net.vulkanmod.render.vertex.TerrainBufferBuilder;
import net.vulkanmod.render.vertex.TerrainRenderType;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class ThreadBuilderPack {

    private static Function<TerrainRenderType, TerrainBufferBuilder> terrainBuilderConstructor;

    public static void defaultTerrainBuilderConstructor() {
        terrainBuilderConstructor = renderType -> new TerrainBufferBuilder(renderType.bufferSize);
    }

    public static void setTerrainBuilderConstructor(Function<TerrainRenderType, TerrainBufferBuilder> constructor) {
        terrainBuilderConstructor = constructor;
    }

    private final Map<TerrainRenderType, TerrainBufferBuilder> builders = new EnumMap<>(TerrainRenderType.class);

    public ThreadBuilderPack() {
        for (TerrainRenderType renderType : TerrainRenderType.ALL_RENDER_TYPES) {
            builders.put(renderType, terrainBuilderConstructor.apply(renderType));
        }
    }

    public TerrainBufferBuilder builder(TerrainRenderType renderType) {
        return builders.get(renderType);
    }

    public void clearAll() {
        builders.values().forEach(TerrainBufferBuilder::clear);
    }

    public void discardAll() {
        builders.values().forEach(TerrainBufferBuilder::discard);
    }

}

// Caching TerrainBufferBuilders

private final Map<TerrainRenderType, TerrainBufferBuilder> cachedBuilders = new HashMap<>();

public TerrainBufferBuilder builder(TerrainRenderType renderType) {
    TerrainBufferBuilder builder = cachedBuilders.get(renderType);
    if (builder == null) {
        builder = terrainBuilderConstructor.apply(renderType);
        cachedBuilders.put(renderType, builder);
    }
    return builder;
}

// Lazy Initialization

private TerrainBufferBuilder builderForRenderType(TerrainRenderType renderType) {
    if (!builders.containsKey(renderType)) {
        builders.put(renderType, terrainBuilderConstructor.apply(renderType));
    }
    return builders.get(renderType);
}

public TerrainBufferBuilder builder(TerrainRenderType renderType) {
    return builderForRenderType(renderType);
}

// Builder Pool

private final Pool<TerrainBufferBuilder> buildersPool = new Pool<>(() -> new TerrainBufferBuilder(renderType.bufferSize));

public TerrainBufferBuilder builder(TerrainRenderType renderType) {
    return buildersPool.acquire();
}

public void clearAll() {
    builders.values().forEach(TerrainBufferBuilder::clear);
    buildersPool.clear();
}

public void discardAll() {
    builders.values().forEach(TerrainBufferBuilder::discard);
    buildersPool.discardAll();
}
