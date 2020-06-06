package net.lovector.ayyther.generator.biome;

import org.bukkit.block.Biome;
import org.bukkit.util.noise.SimplexNoiseGenerator;

import net.lovector.ayyther.generator.terrain.TerrainSurfaces;
import net.lovector.ayyther.generator.terrain.TerrainSurface;

/**
 * Uses the SimplexNoiseGenerator class provided by the Bukkit API to generate
 * biomes.
 * <p>
 * It is limited in that it is not possible to figure out which discrete biome
 * area any given point belongs to using Simplex noise alone. Because of this,
 * the total number of biomes possible is limited.
 * <p>
 * This implementation uses two noise generators that are both
 * divided into four hotness/wetness levels for a total of
 * 16 different possible biomes.
 */
public class SimplexBiomeGenerator extends BiomeGenerator {
    private final SimplexNoiseGenerator wetnessNoise;
    private final SimplexNoiseGenerator hotnessNoise;

    public SimplexBiomeGenerator(long seed) {
        wetnessNoise = new SimplexNoiseGenerator(seed);
        hotnessNoise = new SimplexNoiseGenerator(seed + 1);
    }

    // TODO: config options
    @Override
    public AyytherBiome generateBiome(int x, int z) {
        double wetnessValue = wetnessNoise.noise(x * 0.0007, z * 0.0007);
        int wetness;
        if (wetnessValue <= -0.5)
            wetness = 0;
        else if (wetnessValue <= 0.0)
            wetness = 1;
        else if (wetnessValue <= 0.5)
            wetness = 2;
        else
            wetness = 3;

        double hotnessValue = hotnessNoise.noise(x * 0.0007, z * 0.0007);
        int hotness;
        if (hotnessValue <= -0.5)
            hotness = 0;
        else if (hotnessValue <= 0.0)
            hotness = 1;
        else if (hotnessValue <= 0.5)
            hotness = 2;
        else
            hotness = 3;

        Biome biome;
        switch (wetness) {
            case 0:
                switch (hotness) {
                    case 0:
                        biome = Biome.WOODED_MOUNTAINS;
                        break;
                    case 1:
                        biome = Biome.PLAINS;
                    case 2:
                        biome = Biome.SAVANNA;
                        break;
                    default:
                        biome = Biome.DESERT;
                        break;
                }
                break;
            case 1:
                switch (hotness) {
                    case 0:
                        biome = Biome.SNOWY_TAIGA;
                        break;
                    case 1:
                        biome = Biome.TAIGA;
                        break;
                    case 2:
                        biome = Biome.FOREST;
                        break;
                    default:
                        biome = Biome.BIRCH_FOREST;
                        break;
                }
                break;
            case 2:
                switch (hotness) {
                    case 0:
                        biome = Biome.SNOWY_TAIGA;
                        break;
                    case 1:
                        biome = Biome.TAIGA;
                        break;
                    case 2:
                        biome = Biome.FOREST;
                        break;
                    default:
                        biome = Biome.TALL_BIRCH_FOREST;
                        break;
                }
                break;
            default:
                switch (hotness) {
                    case 0:
                        biome = Biome.GIANT_TREE_TAIGA;
                        break;
                    case 1:
                        biome = Biome.GIANT_SPRUCE_TAIGA;
                        break;
                    case 2:
                        biome = Biome.DARK_FOREST;
                        break;
                    default:
                        biome = Biome.JUNGLE;
                        break;
                }
                break;
        }

        TerrainSurface surface = TerrainSurfaces.getSurfaceFor(biome);

        return new AyytherBiome(biome, surface);
    }
}