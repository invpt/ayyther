package net.lovector.ayyther.generator;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexNoiseGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import net.lovector.ayyther.generator.populators.*;

public class AyytherGenerator extends ChunkGenerator {
    public final String worldName;

    private static final int NORMAL_MAX = 200;
    private static final int NORMAL_MIN = 65;
    
    private final PopulatorHandler populatorHandler;

    private SimplexOctaveGenerator noiseGenerator;
    private SimplexNoiseGenerator wetnessNoise;
    private SimplexNoiseGenerator hotnessNoise;
    private SimplexNoiseGenerator verticalityNoise;
    private SimplexNoiseGenerator islandThresholdNoise;

    public AyytherGenerator(String worldName) {
        this(worldName, new byte[0]);
    }

    public AyytherGenerator(String worldName, byte[] saveData) {
        this.worldName = worldName;
        this.populatorHandler = new PopulatorHandler(Arrays.asList((AyytherBlockPopulator) new DebrisPopulator(NORMAL_MIN)), saveData);
    }

    @Override
    public ChunkGenerator.ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ,
            ChunkGenerator.BiomeGrid biome) {
        int chunkWorldX = chunkX * 16;
        int chunkWorldZ = chunkZ * 16;

        ChunkData data = createChunkData(world);

        if (noiseGenerator == null) {
            noiseGenerator = new SimplexOctaveGenerator(world.getSeed(), 3);

            wetnessNoise = new SimplexNoiseGenerator(world.getSeed() + 1);
            hotnessNoise = new SimplexNoiseGenerator(world.getSeed() + 2);

            verticalityNoise = new SimplexNoiseGenerator(world.getSeed() + 3);
            islandThresholdNoise = new SimplexNoiseGenerator(world.getSeed() + 4);
        }

        boolean chunkEmpty = true;

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                Biome blockBiome = decideBiome(x + chunkWorldX, z + chunkWorldZ);

                for (int y = 0; y < 256; ++y) {
                    biome.setBiome(x, y, z, blockBiome);
                }

                // 0.8 -> 1.2 range
                double verticality = verticalityNoise.noise((x + chunkWorldX) * 0.005, (z + chunkWorldZ) * 0.005) * 0.2 + 1.0;
                // 0.4 -> 0.7 range
                double islandThreshold = islandThresholdNoise.noise((x + chunkWorldX) * 0.005, (z + chunkWorldZ) * 0.005) * 0.15 + 0.55;
                
                if (generateTerrainColumn(data, random, chunkX, chunkZ, verticality, islandThreshold, blockBiome, x, z))
                    chunkEmpty = false;
            }
        }

        // Generate ores
        // We're doing it here rather than in a populator
        // because it's honestly just easier and faster here.
        if (!chunkEmpty) {
            genOre(data, random, 50, NORMAL_MIN, NORMAL_MAX, Material.COAL_ORE);
            genOre(data, random, 20, NORMAL_MIN, NORMAL_MAX - 60, Material.IRON_ORE);
            genOre(data, random, 15, NORMAL_MIN, NORMAL_MIN + 50, Material.REDSTONE_ORE);
            genOre(data, random, 5, NORMAL_MIN, NORMAL_MIN + 50, Material.GOLD_ORE);
            genOre(data, random, 5, NORMAL_MIN, NORMAL_MIN + 30, Material.DIAMOND_ORE);
            genOre(data, random, 2, NORMAL_MIN, NORMAL_MIN + 10, Material.LAPIS_ORE);
        } else {
            // If the chunk is empty, let the populator know
            // This just amounts in less work by populators
            populatorHandler.addEmptyChunk(new Coords2D(chunkX, chunkZ));
        }

        return data;
    }

    // TODO: decide biomes based on voronoi!
    private Biome decideBiome(int x, int z) {
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
                    case 1:
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
                        biome = Biome.FOREST;
                        break;
                    case 1:
                        biome = Biome.FLOWER_FOREST;
                        break;
                    case 2:
                        biome = Biome.BIRCH_FOREST;
                        break;
                    default:
                        biome = Biome.TALL_BIRCH_FOREST;
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
                        biome = Biome.FLOWER_FOREST;
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

        return biome;
    }

    private boolean generateTerrainColumn(ChunkData data, Random random, int chunkX, int chunkZ,
            double verticality, double islandThreshold, Biome biome, int x, int z) {
        boolean didAnything = false;

        ColumnStateMachine machine;
        if (biome == Biome.DESERT) {
            machine = new ColumnStateMachine() {
                @Override
                public Material transition(Random random, Material current, int numCurrent) {
                    switch (current) {
                        case AIR:
                            current = Material.SAND;
                            break;
                        case SAND:
                            if (numCurrent > 1 + random.nextInt(2))
                                current = Material.SANDSTONE;
                            break;
                        case SANDSTONE:
                            if (numCurrent > 3 + random.nextInt(2)) {
                                current = Material.STONE;
                            }
                        default:
                    }

                    return current;
                }

                @Override
                public Material transitionToAir(Random random, Material current, int numCurrent) {
                    if (current == Material.SAND)
                        return Material.SANDSTONE;
                    else
                        return Material.AIR;
                }
            };
        } else if (biome == Biome.GIANT_SPRUCE_TAIGA) {
            machine = new ColumnStateMachine() {
                @Override
                public Material transition(Random random, Material current, int numCurrent) {
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
                            if (numCurrent > 2)
                                current = Material.STONE;
                            break;
                        default:
                    }

                    return current;
                }

                @Override
                public Material transitionToAir(Random random, Material current, int numCurrent) {
                    return Material.AIR;
                }
            };
        } else {
            machine = new ColumnStateMachine() {
                @Override
                public Material transition(Random random, Material current, int numCurrent) {
                    switch (current) {
                        case AIR:
                            current = Material.GRASS_BLOCK;
                            break;
                        case GRASS_BLOCK:
                            current = Material.DIRT;
                            break;
                        case DIRT:
                            if (numCurrent > 1 + random.nextInt(2)) {
                                current = Material.STONE;
                            }
                        default:
                    }

                    return current;
                }

                @Override
                public Material transitionToAir(Random random, Material current, int numCurrent) {
                    return Material.AIR;
                }
            };
        }

        // The normal terrain generation
        Material current = Material.AIR;
        int numCurrent = 0;
        for (int y = NORMAL_MAX; y >= NORMAL_MIN; --y) {
            // the noise multiplier just fades the noise off
            // at the top and bottom, so islands don't abruptly stop
            double noiseMult = 1.0;
            if (y <= NORMAL_MIN + 15)
                noiseMult = (y - NORMAL_MIN) / 15.0;
            else if (y >= NORMAL_MAX - 15)
                noiseMult = (NORMAL_MAX - y) / 15.0;

            double noiseX = (x + chunkX * 16) * 0.007;
            double noiseY = y * 0.012 / verticality;
            double noiseZ = (z + chunkZ * 16) * 0.007;

            if (noiseGenerator.noise(noiseX, noiseY, noiseZ, 2.0, 0.4) * noiseMult > islandThreshold) {
                if (!didAnything) didAnything = true;

                Material prevCurrent = current;
                current = machine.transition(random, current, numCurrent);
                if (current != prevCurrent)
                    numCurrent = 1;
                else
                    ++numCurrent;

                data.setBlock(x, y, z, current);
            } else {
                if (current == Material.AIR) {
                    ++numCurrent;
                } else {
                    Material prevCurrent = current;
                    current = machine.transitionToAir(random, current, numCurrent);
                    if (current != prevCurrent)
                        numCurrent = 1;
                    else
                        ++numCurrent;

                    data.setBlock(x, y, z, current);
                }
            }
        }

        return didAnything;
    }

    private interface ColumnStateMachine {
        public Material transition(Random random, Material current, int numCurrent);

        public Material transitionToAir(Random random, Material current, int numCurrent);
    }

    // TODO: make this more advanced lol
    private void genOre(ChunkData data, Random random, int numTries, int spawnRangeMin, int spawnRangeMax, Material type) {
        for (int tryN = 0; tryN < numTries; ++tryN) {
            int startX = random.nextInt(16);
            int startY = random.nextInt(spawnRangeMax - spawnRangeMin) + spawnRangeMin;
            int startZ = random.nextInt(16);

            // Randomize the length, width, height
            // Adding one so that it's always at least 1 block
            // in all dimensions, so we don't generate
            // infinitely thin (non-existant) ores
            int endX = startX + 1 + random.nextInt(3);
            int endY = startY + 1 + random.nextInt(3);
            int endZ = startZ + 1 + random.nextInt(3);

            for (int x = startX; x < endX; ++x) {
                for (int y = startY; y < endY; ++y) {
                    for (int z = startZ; z < endZ; ++z) {
                        try {
                            if (data.getType(x, y, z) == Material.STONE) {
                                data.setBlock(x, y, z, type);
                            }
                        } catch (IllegalArgumentException e) {
                            // This happens when the ore lies outside of the chunk boundary.
                            // In a full generator, something would be done here to ensure that
                            // the vein generates in the adjacent chunks.
                            continue;
                        }
                    }
                }
            }
        }
    }

    public byte[] getSaveBytes() {
        return populatorHandler.getSaveBytes();
    }

    // we don't disallow spawning anywhere, who tf cares
    @Override
    public boolean canSpawn(World world, int x, int y) {
        return true;
    }

    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Arrays.asList((BlockPopulator) populatorHandler);
    }

    public Location getFixedSpawnLocation(World world, Random random) {
        return null;
    }

    @Override
    public boolean isParallelCapable() {
        return true;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return true;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return true;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }
}
