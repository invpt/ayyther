package net.lovector.ayyther.generator.populators;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;

import net.lovector.ayyther.generator.AyytherBlockPopulator;

public class DebrisPopulator extends AyytherBlockPopulator {
    private int lowerBound;

    public DebrisPopulator(int lowerBound) {
        this.lowerBound = lowerBound;
    }

    @Override
    public boolean shouldPopulateIn(Biome biome) {
        return true;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int y = world.getHighestBlockYAt(x + chunk.getX() * 16, z + chunk.getZ() * 16);

                if (y > 0) {
                    int numLastMaterial = 1;
                    Material lastMaterial, lastSolidMaterial;
                    lastMaterial = lastSolidMaterial = chunk.getBlock(x, y, z).getType();

                    for (; y >= lowerBound; --y) {
                        Material material = chunk.getBlock(x, y, z).getType();

                        // If we just transitioned to a solid block from air
                        // that continued for at more than 6 blocks and the
                        // most recent solid block was (sand)stone
                        if (material != Material.AIR && lastMaterial == Material.AIR && numLastMaterial > 6 && (lastSolidMaterial == Material.STONE || lastSolidMaterial == Material.SANDSTONE)) {
                            double debrisAmount = (1.0 - Math.min(1.0, numLastMaterial / 75D)) * 0.5D;

                            // Generate some debris
                            genDebris(random, chunk, x, y + 1, z, debrisAmount);

                            if (random.nextDouble() < debrisAmount) {
                                Material newGroundMaterial;
                                switch (random.nextInt(3)) {
                                    case 0:
                                        newGroundMaterial = Material.STONE;
                                        break;
                                    case 1:
                                        newGroundMaterial = Material.COARSE_DIRT;
                                        break;
                                    case 2:
                                        newGroundMaterial = Material.GRAVEL;
                                        break;
                                    default:
                                        newGroundMaterial = Material.COBBLESTONE;
                                        break;
                                }

                                chunk.getBlock(x, y, z).setType(newGroundMaterial);
                            }
                        }

                        if (material == lastMaterial) {
                            ++numLastMaterial;
                        } else {
                            lastMaterial = material;
                            numLastMaterial = 1;
                        }

                        if (material != Material.AIR) {
                            lastSolidMaterial = material;
                        }
                    }
                }
            }
        }
    }
    
    private void genDebris(Random random, Chunk chunk, int x, int startY, int z, double debrisAmount) {
        if (random.nextDouble() < debrisAmount) {
            int endY = startY + random.nextInt(3);

            for (int y = startY; y <= endY; ++y) {
                Material material;
                switch (random.nextInt(3)) {
                    case 0:
                        material = Material.STONE;
                        break;
                    case 1:
                        material = Material.GRAVEL;
                        break;
                    default:
                        material = Material.COBBLESTONE;
                        break;
                }

                chunk.getBlock(x, y, z).setType(material);
            }
        }
    }
}