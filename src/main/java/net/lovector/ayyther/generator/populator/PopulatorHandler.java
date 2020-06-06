package net.lovector.ayyther.generator.populator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;

import net.lovector.ayyther.utils.Pos2D;
import net.lovector.ayyther.world_data.WorldData;

// Autosaving of some sort is probably needed here
/**
 * Calls block populators on chunks when all of the surrounding chunks have been
 * generated.
 * <p>
 * The surrounding chunks include not only the chunks above, below, and to the
 * side, but also those in the corners.
 */
public class PopulatorHandler extends BlockPopulator {
    private Set<Pos2D> emptyChunks = ConcurrentHashMap.newKeySet();
    private Set<Pos2D> cachedChunks = ConcurrentHashMap.newKeySet();
    private Set<Pos2D> unpopulatedChunks = ConcurrentHashMap.newKeySet();
    private List<AyytherBlockPopulator> populators = new ArrayList<>();

    /**
     * Creates a new instance without any {@link WorldData}.
     * This should only be used when no WorldData exists.
     */
    public PopulatorHandler() {
    }

    /**
     * @param worldData The world data to initialize the
     *                  unpopulated chunks with
     */
    public PopulatorHandler(WorldData worldData) {
        if (worldData != null)
            for (Pos2D unpopulatedChunk : worldData.unpopulatedChunks)
                unpopulatedChunks.add(unpopulatedChunk);
    }

    @Override
    public void populate(World world, Random random, Chunk addedChunk) {
        Pos2D chunkPos = new Pos2D(addedChunk.getX(), addedChunk.getZ());

        if (!emptyChunks.remove(chunkPos)) {
            cachedChunks.add(chunkPos);
            unpopulatedChunks.add(chunkPos);

            Pos2D[] changedChunks = chunkPos.surrounding();

            for (Pos2D changedChunk : changedChunks) {
                if (unpopulatedChunks.contains(changedChunk) && surroundingChunksExist(world, changedChunk)) {
                    Chunk chunk = world.getChunkAt(changedChunk.x, changedChunk.y);

                    // Get the biome at each four corners of the chunk.
                    // This is important for chunks at biome boundaries.
                    // There should never be a biome that is less than
                    // the size of a chunk, so just the corners works.
                    List<Biome> chunkBiomes = new ArrayList<>(4);
                    {
                        Biome topLeft = chunk.getBlock(0, 150, 0).getBiome();
                        if (!chunkBiomes.contains(topLeft))
                            chunkBiomes.add(topLeft);
                        Biome topRight = chunk.getBlock(15, 150, 0).getBiome();
                        if (!chunkBiomes.contains(topRight))
                            chunkBiomes.add(topRight);
                        Biome bottomLeft = chunk.getBlock(0, 150, 15).getBiome();
                        if (!chunkBiomes.contains(bottomLeft))
                            chunkBiomes.add(bottomLeft);
                        Biome bottomRight = chunk.getBlock(15, 150, 15).getBiome();
                        if (!chunkBiomes.contains(bottomRight))
                            chunkBiomes.add(bottomRight);
                    }

                    for (AyytherBlockPopulator populator : populators) {
                        boolean shouldPopulate = false;
                        for (Biome b : chunkBiomes) {
                            if (populator.shouldPopulateIn(b)) {
                                shouldPopulate = true;
                                break;
                            }
                        }

                        if (shouldPopulate)
                            populator.populate(world, random, chunk);
                    }

                    unpopulatedChunks.remove(changedChunk);
                    cachedChunks.remove(changedChunk);
                }
            }
        }
    }

    private boolean surroundingChunksExist(World world, Pos2D position) {
        for (Pos2D pos : position.surrounding())
            if (!chunkExists(world, pos))
                return false;

        return true;
    }

    private boolean chunkExists(World world, Pos2D pos) {
        return cachedChunks.contains(pos) || world.isChunkGenerated(pos.x, pos.y);
    }

    /**
     * Adds an empty chunk to the internal list of empty chunks.
     * When one is added here, it is used to facilitate skipping
     * population of chunks to increase performance.
     * 
     * @param position The position of the chunk
     */
    public void addEmptyChunk(Pos2D position) {
        emptyChunks.add(position);
    }

    /**
     * Sets the internal list of populators.
     * 
     * @param populators The new list of populators to use
     */
    public void setPopulators(AyytherBlockPopulator... populators) {
        this.populators = Arrays.asList(populators);
    }

    /**
     * Creates {@link WorldData} with the internal data.
     * This function is to be used for saving.
     * 
     * @return An instance of WorldData with the internal world data from this instance.
     */
    public WorldData createWorldData() {
        return new WorldData(unpopulatedChunks.toArray(new Pos2D[0]));
    }
}