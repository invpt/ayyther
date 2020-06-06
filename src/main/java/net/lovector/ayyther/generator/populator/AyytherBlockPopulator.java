package net.lovector.ayyther.generator.populator;

import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;

public abstract class AyytherBlockPopulator extends BlockPopulator {
    /**
     * Checks whether this populator should be used in the given biome.
     * 
     * @param biome The biome to check
     * @return Whether this populator should populate in the given biome
     */
    public abstract boolean shouldPopulateIn(Biome biome);
}