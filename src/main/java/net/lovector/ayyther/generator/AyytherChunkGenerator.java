package net.lovector.ayyther.generator;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import net.lovector.ayyther.world_data.WorldData;
import net.lovector.ayyther.generator.biome.SimplexBiomeGenerator;
import net.lovector.ayyther.generator.populator.PopulatorHandler;
import net.lovector.ayyther.generator.populator.normal.DebrisPopulator;
import net.lovector.ayyther.generator.populator.normal.StalactitePopulator;
import net.lovector.ayyther.generator.terrain.TerrainGenerator;

public class AyytherChunkGenerator extends ChunkGenerator {
    public final String worldName;

    private final PopulatorHandler populatorHandler;
    private AyytherGenerator generator = null;

    public AyytherChunkGenerator(String worldName, WorldData worldData) {
        this.worldName = worldName;
        this.populatorHandler = new PopulatorHandler(worldData);
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid) {
        ChunkData data = createChunkData(world);

        if (generator == null || populatorHandler == null) {
            // initialize generator
            makeAyytherGenerator(world);
            // initialize populators
            setPopulators(world);
        }

        generator.generateChunk(random, data, biomeGrid, chunkX, chunkZ);

        return data;
    }

    // This is obviously not an ideal way to do things.
    // Once configuration files are a thing, this will not be needed.
    private void makeAyytherGenerator(World world) {
        if (world.getEnvironment() == Environment.NORMAL || true) {
            final int min = 65;
            final int max = 200;
            generator = new AyytherGenerator(
                new TerrainGenerator(world.getSeed(), 3, 2.0, 0.5, 0.007, 0.012, 0.007, 0.5, min, max, 32),
                new SimplexBiomeGenerator(world.getSeed()),
                Arrays.asList(
                    new Ore(Material.COAL_ORE, 1, 5, 50, min, max),
                    new Ore(Material.IRON_ORE, 1, 4, 20, min, max - 60),
                    new Ore(Material.REDSTONE_ORE, 1, 4, 15, min, min + 50),
                    new Ore(Material.GOLD_ORE, 1, 3, 5, min, min + 50),
                    new Ore(Material.DIAMOND_ORE, 1, 3, 5, min, min + 30),
                    new Ore(Material.EMERALD_ORE, 1, 2, 2, min, min + 10)
                )
            );
        }
    }

    private void setPopulators(World world) {
        if (world.getEnvironment() == Environment.NORMAL) {
            populatorHandler.setPopulators(new DebrisPopulator(65), new StalactitePopulator(65, 200));
        }
    }

    public WorldData createWorldData() {
        return populatorHandler.createWorldData();
    }

    // we don't disallow spawning anywhere
    @Override
    public boolean canSpawn(World world, int x, int y) {
        return true;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Arrays.asList((BlockPopulator) populatorHandler);
    }

    @Override
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