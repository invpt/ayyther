package net.lovector.ayyther.generator;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.data.Bisected;
import org.bukkit.generator.BlockPopulator;

public abstract class AyytherBlockPopulator extends BlockPopulator {
    public abstract boolean shouldPopulateIn(Biome biome);

    protected void genBisected(Chunk chunk, int x, int y, int z, Material material) {
        Bisected bottom = (Bisected) material.createBlockData();
        bottom.setHalf(Bisected.Half.BOTTOM);
        Bisected top = (Bisected) material.createBlockData();
        top.setHalf(Bisected.Half.TOP);
        chunk.getBlock(x, y, z).setBlockData(bottom, false);
        chunk.getBlock(x, y + 1, z).setBlockData(top);
    }

    protected int getHighestAboveSurfaceY(World world, Chunk chunk, int x, int z) {
        int y = world.getHighestBlockYAt(x + chunk.getX() * 16, z + chunk.getZ() * 16);

        for (; y >= 0; --y) {
            Material blockType = chunk.getBlock(x, y, z).getType();

            if (blockType == Material.GRASS_BLOCK || blockType == Material.PODZOL) {
                ++y;
                break;
            } else if (blockType == Material.STONE || blockType == Material.DIRT || blockType == Material.COARSE_DIRT) {
                y = -1;
                break;
            }
        }

        return y;
    }
}