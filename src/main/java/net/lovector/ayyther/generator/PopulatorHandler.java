package net.lovector.ayyther.generator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;

// Our PopulatorHandler only populates a chunk when
// all of the chunks surrounding it have been generated.

// We could do this differently, perhaps always
// populating chunks when asked to, but then always generating
// the surrounding terrain. Then, when asked to generate an
// already-generated chunk, we'd use what we generated.
// However, this method is easier, less error-prone, and takes up less space.

// TODO: this is a fat one. autosave every 5min
public class PopulatorHandler extends BlockPopulator {
    private Set<Coords2D> emptyChunks = ConcurrentHashMap.newKeySet();
    private Set<Coords2D> cachedChunks = ConcurrentHashMap.newKeySet();
    private Set<Coords2D> unpopulatedChunks = ConcurrentHashMap.newKeySet();
    private List<AyytherBlockPopulator> populators;

    public PopulatorHandler(List<AyytherBlockPopulator> populators) {
        this.populators = populators;
    }

    public PopulatorHandler(List<AyytherBlockPopulator> populators, byte[] saveData) {
        this(populators);

        if (saveData.length > 0) {
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(saveData);
            try (DataInputStream inputStream = new DataInputStream(byteInputStream)) {
                // We need 8 bytes for one coordinate
                while (inputStream.available() >= 8) {
                    int x = inputStream.readInt();
                    int z = inputStream.readInt();

                    unpopulatedChunks.add(new Coords2D(x, z));
                }
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void populate(World world, Random random, Chunk addedChunk) {
        Coords2D chunkCoordinates = new Coords2D(addedChunk.getX(), addedChunk.getZ());

        if (!emptyChunks.remove(chunkCoordinates)) {
            cachedChunks.add(chunkCoordinates);
            unpopulatedChunks.add(chunkCoordinates);

            Coords2D[] changedChunks = chunkCoordinates.surrounding();

            for (Coords2D changedChunk : changedChunks) {
                if (unpopulatedChunks.contains(changedChunk) && surroundingChunksExist(world, changedChunk)) {
                    Chunk chunk = world.getChunkAt(changedChunk.x, changedChunk.z);

                    // Get the biome at each four corners of the chunk.
                    // This is overkill for 90% of chunks, but it's important for the borders.
                    List<Biome> chunkBiomes = new ArrayList<>(4);
                    {
                        Biome topLeft = chunk.getBlock(0, 150, 0).getBiome();
                        if (!chunkBiomes.contains(topLeft)) chunkBiomes.add(topLeft);
                        Biome topRight = chunk.getBlock(15, 150, 0).getBiome();
                        if (!chunkBiomes.contains(topRight)) chunkBiomes.add(topRight);
                        Biome bottomLeft = chunk.getBlock(0, 150, 15).getBiome();
                        if (!chunkBiomes.contains(bottomLeft)) chunkBiomes.add(bottomLeft);
                        Biome bottomRight = chunk.getBlock(15, 150, 15).getBiome();
                        if (!chunkBiomes.contains(bottomRight)) chunkBiomes.add(bottomRight);
                    }

                    for (int i = 0; i < populators.size(); ++i) {
                        AyytherBlockPopulator populator = populators.get(i);

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

    // Gets the byte[] save data for all of the unpopulated chunks.
    // This should probably be designed differently. For now, it works.
    public byte[] getSaveBytes() {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);

        try {
            for (Coords2D coordinates : unpopulatedChunks) {
                dataOut.writeInt(coordinates.x);
                dataOut.writeInt(coordinates.z);
            }
        } catch (IOException e) {
        }

        return byteOut.toByteArray();
    }

    public void addEmptyChunk(Coords2D coordinates) {
        emptyChunks.add(coordinates);
    }

    private boolean surroundingChunksExist(World world, Coords2D coordinates) {
        return chunkExists(world, coordinates.left()) && chunkExists(world, coordinates.right())
                && chunkExists(world, coordinates.above()) && chunkExists(world, coordinates.below())
                && chunkExists(world, coordinates.upperLeft()) && chunkExists(world, coordinates.upperRight())
                && chunkExists(world, coordinates.lowerLeft()) && chunkExists(world, coordinates.lowerRight());
    }

    private boolean chunkExists(World world, Coords2D coordinates) {
        return cachedChunks.contains(coordinates) || world.isChunkGenerated(coordinates.x, coordinates.z);
    }
}