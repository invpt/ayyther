package net.lovector.ayyther;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import net.lovector.ayyther.generator.AyytherChunkGenerator;
import net.lovector.ayyther.world_data.WorldData;
import net.lovector.ayyther.world_data.WorldDataManager;

public final class AyytherPlugin extends JavaPlugin {
    public static Logger logger = null;

    private ArrayList<AyytherChunkGenerator> generators = new ArrayList<>();
    private WorldDataManager worldDataManager = null;

    @Override
    public void onEnable() {
        try {
            worldDataManager = new WorldDataManager(getWorldsFolder());
        } catch (FileNotFoundException e) {
            getLogger().severe("Failed to find or create the worlds folder!");
            getServer().getPluginManager().disablePlugin(this);
        }

        

        logger = getLogger();

        getServer().getPluginManager().registerEvents(new EventListener(), this);

        getLogger().info("Ayyther enabled.");
    }

    private File getWorldsFolder() {
        File dataFolder = getDataFolder();
        if (!dataFolder.exists())
            dataFolder.mkdirs();

        File worldsFolder = new File(dataFolder, "worlds");
        if (!worldsFolder.exists())
            worldsFolder.mkdirs();
        
        return worldsFolder;
    }

    @Override
    public void onDisable() {
        getLogger().info("Saving unpopulated chunks for all Ayyther worlds...");

        for (AyytherChunkGenerator generator : generators) {
            WorldData generatorWorldData = generator.createWorldData();

            try {
                worldDataManager.saveWorldData(generator.worldName, generatorWorldData);
                getLogger().info("Saved " + generatorWorldData.unpopulatedChunks.length + " unpopulated chunk(s) for '" + generator.worldName + "'.");
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Failed to save unpopulated chunks for '" + generator.worldName + "'!", e);
            }
        }

        getLogger().info("Ayyther disabled.");
    }

    public static Logger logger() {
        return logger;
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        getLogger().info("Loading generator for '" + worldName + "'...");

        for (AyytherChunkGenerator generator : generators) {
            if (generator.worldName.equals(worldName)) {
                // Multiverse loads the generator a second time so we
                // have to make sure to give it the same one
                getLogger().info("Generator for '" + worldName + "' already loaded.");
                return generator;
            }
        }

        AyytherChunkGenerator generator;

        try {
            WorldData worldData = worldDataManager.loadWorldData(worldName);
    
            if (worldData != null) {
                generator = new AyytherChunkGenerator(worldName, worldData);
                getLogger().info("Loaded " + worldData.unpopulatedChunks.length + " unpopulated chunk(s) for '" + worldName + "'.");
            } else {
                generator = new AyytherChunkGenerator(worldName, worldData);
                getLogger().info("No unpopulated chunks found for '" + worldName + "'.");
            }
        } catch (IOException e) {
            generator = new AyytherChunkGenerator(worldName, null);
            getLogger().log(Level.WARNING, "Encountered an error while loading unpopulated chunks for '" + worldName + "'!", e);
        }

        getLogger().info("Generator for '" + worldName + "' loaded.");

        generators.add(generator);
        return generator;
    }
}

class EventListener implements Listener {
    private static final Vector UP_VECTOR = new Vector(0.0, 1.0, 0.0);
    private static final double DARKNESS_SPAWN_MAX_DIST = 20.0;

    // Cancels all monster spawns that are due to darkness that is
    // caused by blocks more than DARKNESS_SPAWN_MAX_DIST above
    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        World eventWorld = event.getLocation().getWorld();

        if (isAyytherWorld(eventWorld) && isDaytime(eventWorld.getTime()) && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL && event.getEntity() instanceof Monster) {
            // Raytrace straight above the monster
            RayTraceResult result = eventWorld.rayTraceBlocks(event.getLocation(), UP_VECTOR, DARKNESS_SPAWN_MAX_DIST);

            if (result != null)
                event.setCancelled(true);
        }
    }

    // Cancels all ender pearl damage in Ayyther
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof EnderPearl && isAyytherWorld(event.getEntity().getWorld()))
            event.setCancelled(true);
    }

    private boolean isAyytherWorld(World world) {
        return world.getGenerator() instanceof AyytherChunkGenerator;
    }

    private boolean isDaytime(long time) {
        return time < 12000;
    }
}