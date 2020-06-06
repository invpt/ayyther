package net.lovector.ayyther.generator.biome;

/**
 * Generates {@link AyytherBiome}s.
 */
public abstract class BiomeGenerator {
    /**
     * @return The biome located at the given coordinates
     */
    public abstract AyytherBiome generateBiome(int x, int z);
}