package net.lovector.ayyther.generator;

import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import net.lovector.ayyther.generator.biome.AyytherBiome;
import net.lovector.ayyther.generator.biome.BiomeGenerator;
import net.lovector.ayyther.generator.terrain.TerrainGenerator;

public class AyytherGenerator {
    private final TerrainGenerator terrainGenerator;
    private final BiomeGenerator biomeGenerator;
    private final List<Ore> ores;

    public AyytherGenerator(TerrainGenerator terrainGenerator, BiomeGenerator biomeGenerator, List<Ore> ores) {
        this.terrainGenerator = terrainGenerator;
        this.biomeGenerator = biomeGenerator;
        this.ores = ores;
    }

    /**
     * Generates a chunk into a ChunkData
     * 
     * @param random The random generator to use
     * @param data The chunk data to modify
     * @param biomeGrid The biome grid to modify
     * @param chunkX The X coordinate of the chunk
     * @param chunkZ The Y coordinate of the chunk
     * @return Whether the chunk was modified (whether anything was generated)
     */
    public boolean generateChunk(Random random, ChunkData data, BiomeGrid biomeGrid, int chunkX, int chunkZ) {
        boolean chunkEmpty = true;

        int chunkWorldX = chunkX * 16;
        int chunkWorldZ = chunkZ * 16;

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int worldX = chunkWorldX + x;
                int worldZ = chunkWorldZ + z;

                AyytherBiome biome = biomeGenerator.generateBiome(worldX, worldZ);

                for (int y = 0; y < 256; ++y) {
                    biomeGrid.setBiome(x, y, z, biome.biome);
                }

                boolean didGenerateAnything = terrainGenerator.generateColumn(random, data, biome.surface.makeGenerator(), worldX, worldZ);

                if (didGenerateAnything)
                    chunkEmpty = false;
            }
        }

        // Generate all of the ores
        if (!chunkEmpty)
            for (Ore ore : ores)
                ore.generate(random, data);

        return !chunkEmpty;
    }
}

class Ore {
    public final Material material;
    public final int sizeMin;
    public final int sizeMax;
    public final int countPerChunk;
    public final int spawnRangeMin;
    public final int spawnRangeMax;

    public Ore(Material material, int sizeMin, int sizeMax, int countPerChunk, int spawnRangeMin, int spawnRangeMax) {
        this.material = material;
        this.sizeMin = sizeMin;
        this.sizeMax = sizeMax;
        this.countPerChunk = countPerChunk;
        this.spawnRangeMin = spawnRangeMin;
        this.spawnRangeMax = spawnRangeMax;
    }

    public void generate(Random random, ChunkData data) {
        for (int i = 0; i < countPerChunk; ++i) {
            int startX = random.nextInt(16);
            int startY = random.nextInt(spawnRangeMax - spawnRangeMin) + spawnRangeMin;
            int startZ = random.nextInt(16);

            int endX = startX + sizeMin + random.nextInt(sizeMax - sizeMin);
            int endY = startY + sizeMin + random.nextInt(sizeMax - sizeMin);
            int endZ = startZ + sizeMin + random.nextInt(sizeMax - sizeMin);

            for (int x = startX; x < endX; ++x) {
                for (int y = startY; y < endY; ++y) {
                    for (int z = startZ; z < endZ; ++z) {
                        try {
                            if (data.getType(x, y, z) == Material.STONE)
                                data.setBlock(x, y, z, material);
                        } catch (IllegalArgumentException e) {
                            // This happens when the ore lies outside of the chunk boundary.
                            break;
                        }
                    }
                }
            }
        }
    }
}