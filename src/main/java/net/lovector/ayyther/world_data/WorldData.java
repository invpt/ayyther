package net.lovector.ayyther.world_data;

import net.lovector.ayyther.utils.Pos2D;

public class WorldData {
    public final Pos2D[] unpopulatedChunks;

    public WorldData(Pos2D[] unpopulatedChunks) {
        this.unpopulatedChunks = unpopulatedChunks;
    }
}