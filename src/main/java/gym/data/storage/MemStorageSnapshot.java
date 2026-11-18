package gym.data.storage;
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

    private final String resourceLocation;
    private final ObjectMapper mapper = JsonMapper.builder().build();

    public MemStorageSnapshot(String resourceLocation) {
        if (resourceLocation == null || resourceLocation.isBlank()) {
            throw new IllegalArgumentException("ResourceLocation can't be null or empty");
        }
        this.resourceLocation = resourceLocation;
    }

    public <TEntity, TKey>void loadInto(Storage<TEntity, TKey> target) {
        if (target == null) {
            throw new IllegalArgumentException("Target storage cannot be null.");
        }

        String fileName = fileName(target.getType());
        try (InputStream in = open(fileName)) {
            if (in == null) {
                return; // no file: no items of this type
            }
            List<TEntity> items = mapper.readerForListOf(target.getType()).readValue(in);
            items.forEach(target::put);

        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read snapshot file " + fileName + ".", e);
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot parse snapshot file " + fileName + ".", e);
        }
    }

    public <TEntity, TKey> void store(Class<TEntity> type, Storage<TEntity, TKey> storage, Path outputDirectory) {
        try {
            Files.createDirectories(outputDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create snapshot directory " + outputDirectory + ".", e);
        }

        Path file = outputDirectory.resolve(fileName(storage.getType()));
        try (OutputStream out = Files.newOutputStream(file)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(out, storage.getAll());
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write snapshot file " + file + ".", e);
        }
    }

    private InputStream open(String fileName) throws IOException {
        String resource = resourceLocation + "/" + fileName;
        return MemStorageSnapshot.class.getClassLoader().getResourceAsStream(resource);
    }

    private static String fileName(Class<?> type) {
        return type.getName() + ".json";
    }
}
