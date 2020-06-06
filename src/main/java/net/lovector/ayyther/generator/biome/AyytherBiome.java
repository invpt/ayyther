package net.lovector.ayyther.generator.biome;

import org.bukkit.block.Biome;

import net.lovector.ayyther.generator.terrain.TerrainSurface;

/**
 * A data-holding class for biomes.
 * <p>
 * It is to be constructed by a {@link BiomeGenerator}.
 */
public final class AyytherBiome {
    public final Biome biome;
    public final TerrainSurface surface;

    AyytherBiome(Biome biome, TerrainSurface surface) {
        this.biome = biome;
        this.surface = surface;
    }
}