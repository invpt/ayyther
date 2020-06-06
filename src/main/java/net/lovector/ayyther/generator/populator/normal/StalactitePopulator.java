package net.lovector.ayyther.generator.populator.normal;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import net.lovector.ayyther.generator.populator.AyytherBlockPopulator;

public class StalactitePopulator extends AyytherBlockPopulator {
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 3;
    private static final int STALACTITE_DENSITY = 10;

    private int lowerBound;
    private int upperBound;

    public StalactitePopulator(int lowerBound, int upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public boolean shouldPopulateIn(Biome biome) {
        return true;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int y = lowerBound - MAX_LENGTH;

                int numLastMaterial = 1;
                Material lastMaterial = chunk.getBlock(x, y, z).getType();

                for (; y < upperBound; ++y) {
                    Material material = chunk.getBlock(x, y, z).getType();

                    if (material == Material.STONE && lastMaterial == Material.AIR && numLastMaterial >= MAX_LENGTH) {
                        // Generate the stalactite
                        genStalactite(random, chunk, x, y - 1, z);
                    }

                    if (material == lastMaterial) {
                        ++numLastMaterial;
                    } else {
                        lastMaterial = material;
                        numLastMaterial = 1;
                    }
                }
            }
        }
    }

    private void genStalactite(Random random, Chunk chunk, int x, int startY, int z) {
        if (random.nextInt(100) < STALACTITE_DENSITY) {
            int endY = startY - MIN_LENGTH - random.nextInt(MAX_LENGTH - MIN_LENGTH);

            for (int y = startY; y >= endY; --y) {
                Block block = chunk.getBlock(x, y, z);

                if (block.isEmpty()) block.setType(Material.STONE);
                else break;
            }
        }
    }
}