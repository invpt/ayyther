package net.lovector.ayyther.world_data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import net.lovector.ayyther.utils.Pos2D;

public class WorldDataManager {
    private static byte FILE_VERSION = 0x0;

    private File worldsFolder;

    /**
     * Creates a new WorldDataManager using the given worlds folder.
     * 
     * @param worldsFolder The worlds folder
     * @throws FileNotFoundException When the worlds folder does not exist
     */
    public WorldDataManager(File worldsFolder) throws FileNotFoundException {
        if (worldsFolder.exists())
            this.worldsFolder = worldsFolder;
        else
            throw new FileNotFoundException("The provided worlds folder must exist");
    }

    /**
     * @param worldName The world file name to load data from
     * @return The world data, or null if no saved world data exists
     * @throws IOException When an IO exception is encountered while deserializing
     *                     the world data
     */
    public WorldData loadWorldData(String worldName) throws IOException {
        File worldFile = getWorldDataFile(getWorldFolder(worldName));

        if (worldFile.exists()) {
            Pos2D[] positions = null;

            try (DataInputStream inputStream = new DataInputStream(new FileInputStream(worldFile))) {
                if (!readFileVersion(inputStream)) {
                    // This will do something in the future, when
                    // there are more file versions.
                }
                positions = deserializePositions(inputStream);
            }

            return new WorldData(positions);
        } else {
            return null;
        }
    }

    /**
     * @param worldName The world file name to save data to
     * @param worldData The data to save
     * @throws IOException When an IO exception is encoutered while serializing the
     *                     world data
     */
    public void saveWorldData(String worldName, WorldData worldData) throws IOException {
        File worldFolder = getWorldFolder(worldName);
        if (!worldFolder.exists()) {
            worldFolder.mkdirs();
        }

        File worldFile = getWorldDataFile(worldFolder);

        try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(worldFile))) {
            writeFileVersion(outputStream);
            serializePositions(worldData.unpopulatedChunks, outputStream);
        }
    }

    private File getWorldFolder(String worldName) {
        return new File(worldsFolder, worldName);
    }

    private File getWorldDataFile(File worldFolder) {
        return new File(worldFolder, "data");
    }

    private void writeFileVersion(OutputStream outputStream) throws IOException {
        outputStream.write(new byte[] { FILE_VERSION });
    }

    private boolean readFileVersion(InputStream inputStream) throws IOException {
        byte version = (byte) inputStream.read();

        return version == FILE_VERSION;
    }

    private void serializePositions(Pos2D[] positions, DataOutputStream outputStream) throws IOException {
        for (Pos2D pos : positions) {
            serializePosition(pos, outputStream);
        }
    }

    private void serializePosition(Pos2D position, DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(position.x);
        outputStream.writeInt(position.y);
    }

    private Pos2D[] deserializePositions(DataInputStream inputStream) throws IOException {
        ArrayList<Pos2D> positions = new ArrayList<>(inputStream.available() / 8);

        while (inputStream.available() >= 8) {
            positions.add(deserializePosition(inputStream));
        }

        return positions.toArray(new Pos2D[0]);
    }

    private Pos2D deserializePosition(DataInputStream inputStream) throws IOException {
        return new Pos2D(inputStream.readInt(), inputStream.readInt());
    }
}