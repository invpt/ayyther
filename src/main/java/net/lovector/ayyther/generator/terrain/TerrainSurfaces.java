package net.lovector.ayyther.generator.terrain;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Biome;

/**
 * Houses the built-in terrain surfaces.
 */
public class TerrainSurfaces {
    public static final TerrainSurface DESERT_SURFACE = new DesertSurface();
    public static final TerrainSurface GIANT_SPRUCE_SURFACE = new GiantSpruceSurface();
    public static final TerrainSurface NORMAL_SURFACE = new NormalSurface();

    /**
     * Gets the surface for a given biome
     * 
     * @param biome The biome
     * @return The corresponding surface
     */
    public static TerrainSurface getSurfaceFor(Biome biome) {
        switch (biome) {
            case DESERT:
                return DESERT_SURFACE;
            case GIANT_SPRUCE_TAIGA:
                return GIANT_SPRUCE_SURFACE;
            default:
                return NORMAL_SURFACE;
        }
    }

    private static class DesertSurface extends TerrainSurface {
        @Override
        Material getNext(Material current, int currentCount, Random random, boolean blockFilled) {
            if (blockFilled) {
                switch (current) {
                    case AIR:
                        current = Material.SAND;
                        break;
                    case SAND:
                        if (currentCount > 1 + random.nextInt(2))
                            current = Material.SANDSTONE;
                        break;
                    case SANDSTONE:
                        if (currentCount > 3 + random.nextInt(2)) {
                            current = Material.STONE;
                        }
                    default:
                }
    
                return current;
            } else {
                if (current == Material.SAND)
                    return Material.SANDSTONE;
                else
                    return Material.AIR;
            }
        }
    }
    
    private static class GiantSpruceSurface extends TerrainSurface {
        @Override
        Material getNext(Material current, int currentCount, Random random, boolean blockFilled) {
            if (blockFilled) {
                switch (current) {
                    case AIR:
                        if (random.nextInt(3) == 0)
                            current = Material.COARSE_DIRT;
                        else
                            current = Material.GRASS_BLOCK;
                        break;
                    case GRASS_BLOCK:
                        current = Material.DIRT;
                        break;
                    case COARSE_DIRT:
                        current = Material.DIRT;
                        break;
                    case DIRT:
                        if (currentCount > 2)
                            current = Material.STONE;
                        break;
                    default:
                }

                return current;
            } else {
                return Material.AIR;
            }
        }
    }
    
    private static class NormalSurface extends TerrainSurface {
        @Override
        Material getNext(Material current, int currentCount, Random random, boolean blockFilled) {
            if (blockFilled) {
                switch (current) {
                    case AIR:
                        current = Material.GRASS_BLOCK;
                        break;
                    case GRASS_BLOCK:
                        current = Material.DIRT;
                        break;
                    case DIRT:
                        if (currentCount > 1 + random.nextInt(2)) {
                            current = Material.STONE;
                        }
                    default:
                }
    
                return current;
            } else {
                return Material.AIR;
            }
        }
    }
}