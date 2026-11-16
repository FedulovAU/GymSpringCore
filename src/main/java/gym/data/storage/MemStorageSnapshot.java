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


public class MemStorageSnapshot {

    private final SnapshotOptions options;
    private final ObjectMapper mapper = JsonMapper.builder().build();

    public MemStorageSnapshot(SnapshotOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("Snapshot options cannot be null.");
        }
        this.options = options;
    }

    public MemStorage load() {
        MemStorage storage = new MemStorage();
        loadInto(storage);
        return storage;
    }

    public void loadInto(MemStorage target) {
        if (target == null) {
            throw new IllegalArgumentException("Target storage cannot be null.");
        }
        if (options instanceof SnapshotOptions.FromEmptyStorage) {
            return;
        }
        loadType(target, TraineeEntity.class);
        loadType(target, TrainerEntity.class);
        loadType(target, TrainingEntity.class);
    }

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

    private InputStream open(String fileName) throws IOException {
        return switch (options) {
            case SnapshotOptions.FromDirectory directory -> {
                Path file = directory.path().resolve(fileName);
                yield Files.exists(file) ? Files.newInputStream(file) : null;
            }
            case SnapshotOptions.FromResources resources -> {
                String resource = resources.classPath() + "/" + fileName;
                yield MemStorageSnapshot.class.getClassLoader().getResourceAsStream(resource);
            }
            case SnapshotOptions.FromEmptyStorage none -> null;
        };
    }

    private static String fileName(Class<?> type) {
        return type.getName() + ".json";
    }
}
