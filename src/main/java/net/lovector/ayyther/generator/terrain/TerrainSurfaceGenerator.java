package net.lovector.ayyther.generator.terrain;

import java.util.Random;

import org.bukkit.Material;

public class TerrainSurfaceGenerator {
    private final TerrainSurface surface;
    
    private Material current = Material.AIR;
    private int currentCount = 0;

    TerrainSurfaceGenerator(TerrainSurface surface) {
        this.surface = surface;
    }

    /**
     * Advances to the next block in the surface.
     * 
     * @param random The random generator to use
     * @param blockFilled Whether or not the block is filled
     * @return The resulting material
     */
    public Material advance(Random random, boolean blockFilled) {
        Material result = surface.getNext(current, currentCount, random, blockFilled);

        if (result != current)
            currentCount = 1;
        else
            ++currentCount;
        
        current = result;

        return current;
    }
}