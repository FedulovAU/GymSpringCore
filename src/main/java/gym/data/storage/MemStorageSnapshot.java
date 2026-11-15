package gym.data.storage;

import gym.data.entities.TraineeEntity;
import gym.data.entities.TrainerEntity;
import gym.data.entities.TrainingEntity;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Stores the storage content to JSON files and loads it back, keeping entity IDs.
 * Each entity type is kept in its own file named by the type's fully qualified name, e.g.
 * "gym.data.entities.TraineeEntity.json", containing a JSON array of entities.
 * Only the types listed here are stored and loaded; a missing file means no items of that type.
 * Loading doesn't validate the data, files are expected to be produced by {@link #store}.
 */
public class MemStorageSnapshot {

    private final SnapshotOptions options;
    private final ObjectMapper mapper = JsonMapper.builder().build();

    public MemStorageSnapshot(SnapshotOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("Snapshot options cannot be null.");
        }
        this.options = options;
    }

    /**
     * Creates a storage filled from the configured input source.
     * @return a new storage; empty for {@link SnapshotOptions.None}.
     * @throws UncheckedIOException if a snapshot file can't be read.
     * @throws IllegalStateException if a snapshot file isn't valid JSON for its entity type.
     */
    public MemStorage load() {
        MemStorage storage = new MemStorage();
        if (options instanceof SnapshotOptions.None) {
            return storage;
        }
        loadType(storage, TraineeEntity.class);
        loadType(storage, TrainerEntity.class);
        loadType(storage, TrainingEntity.class);
        return storage;
    }

    /**
     * Writes all items of the snapshot types to JSON files in the given directory, creating it if needed.
     * Existing snapshot files in the directory are overwritten.
     * @param storage the storage to store.
     * @param outputDirectory the directory to write the files to.
     * @throws UncheckedIOException if a file can't be written.
     */
    public void store(MemStorage storage, Path outputDirectory) {
        try {
            Files.createDirectories(outputDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create snapshot directory " + outputDirectory + ".", e);
        }
        storeType(storage, TraineeEntity.class, outputDirectory);
        storeType(storage, TrainerEntity.class, outputDirectory);
        storeType(storage, TrainingEntity.class, outputDirectory);
    }

    private <T extends EntityWithId<T>> void loadType(MemStorage storage, Class<T> type) {
        String fileName = fileName(type);
        try (InputStream in = open(fileName)) {
            if (in == null) {
                return; // no file: no items of this type
            }
            List<T> items = mapper.readerForListOf(type).readValue(in);
            storage.restoreItems(type, items);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read snapshot file " + fileName + ".", e);
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot parse snapshot file " + fileName + ".", e);
        }
    }

    private <T extends EntityWithId<T>> void storeType(MemStorage storage, Class<T> type, Path outputDirectory) {
        Path file = outputDirectory.resolve(fileName(type));
        try (OutputStream out = Files.newOutputStream(file)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(out, storage.getAllItems(type));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write snapshot file " + file + ".", e);
        }
    }

    /**
     * Opens a snapshot file from the configured source.
     * @return the file's stream, or null if the file doesn't exist.
     */
    private InputStream open(String fileName) throws IOException {
        return switch (options) {
            case SnapshotOptions.FromDirectory directory -> {
                Path file = directory.path().resolve(fileName);
                yield Files.exists(file) ? Files.newInputStream(file) : null;
            }
            case SnapshotOptions.FromResources resources -> {
                String folder = stripSlashes(resources.classPath());
                String resource = folder.isEmpty() ? fileName : folder + "/" + fileName;
                yield MemStorageSnapshot.class.getClassLoader().getResourceAsStream(resource);
            }
            case SnapshotOptions.None none -> null;
        };
    }

    private static String fileName(Class<?> type) {
        return type.getName() + ".json";
    }

    private static String stripSlashes(String path) {
        String result = path.strip();
        while (result.startsWith("/")) result = result.substring(1);
        while (result.endsWith("/")) result = result.substring(0, result.length() - 1);
        return result;
    }
}
