package gym.data.storage;

import gym.data.entities.TraineeEntity;
import gym.data.entities.TrainerEntity;
import gym.data.entities.TrainingEntity;
import gym.data.entities.common.UserData;
import gym.data.entities.enums.TrainingTypeEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MemStorageSnapshotTest {

    @TempDir
    private Path tempDir;

    private static MemStorage filledStorage() {
        MemStorage storage = new MemStorage();
        TraineeEntity trainee = storage.addItem(TraineeEntity.class, new TraineeEntity(null,
                new UserData("John", "Smith", "John_Smith", "secret12345", true),
                LocalDate.of(1990, 5, 17)));
        TrainerEntity trainer = storage.addItem(TrainerEntity.class, new TrainerEntity(null,
                new UserData("Anna", "Lee", "Anna_Lee", "secret67890", false),
                TrainingTypeEntity.Crossfit));
        storage.addItem(TrainingEntity.class, new TrainingEntity(null, trainee.id(), trainer.id(), "Morning",
                TrainingTypeEntity.Crossfit, LocalDateTime.of(2026, 12, 1, 10, 0), Duration.ofMinutes(90)));
        return storage;
    }

    private static <T extends EntityWithId<T>> void assertSameItems(MemStorage expected, MemStorage actual, Class<T> type) {
        assertEquals(Set.copyOf(expected.getAllItems(type)), Set.copyOf(actual.getAllItems(type)));
    }

    // ---------- store + load ----------

    @Test
    void storeThenLoad_fromDirectory_restoresAllEntities() {
        MemStorage original = filledStorage();
        var snapshot = new MemStorageSnapshot(new SnapshotOptions.FromDirectory(tempDir));
        snapshot.store(original, tempDir);

        MemStorage loaded = snapshot.load();

        assertSameItems(original, loaded, TraineeEntity.class);
        assertSameItems(original, loaded, TrainerEntity.class);
        assertSameItems(original, loaded, TrainingEntity.class);
    }

    @Test
    void load_thenAddItem_assignsIdAboveLoaded() {
        var snapshot = new MemStorageSnapshot(new SnapshotOptions.FromDirectory(tempDir));
        snapshot.store(filledStorage(), tempDir);
        MemStorage loaded = snapshot.load();

        TraineeEntity added = loaded.addItem(TraineeEntity.class, new TraineeEntity(null,
                new UserData("Jack", "Brown", "Jack_Brown", "pass", true), LocalDate.of(2000, 1, 1)));

        assertEquals(2L, added.id());
    }

    // ---------- load sources ----------

    @Test
    void load_fromResources_restoresEntities() {
        var snapshot = new MemStorageSnapshot(new SnapshotOptions.FromResources("snapshot-fixture"));
        MemStorage loaded = snapshot.load();

        assertEquals(Set.of(new TraineeEntity(5L,
                        new UserData("Mia", "Wong", "Mia_Wong_1", "fixturePass", true),
                        LocalDate.of(1995, 3, 2))),
                Set.copyOf(loaded.getAllItems(TraineeEntity.class)));

        // The fixture has no files for these types: empty, not an error.
        assertTrue(loaded.getAllItems(TrainerEntity.class).isEmpty());
        assertTrue(loaded.getAllItems(TrainingEntity.class).isEmpty());
    }

    @Test
    void load_none_returnsEmptyStorage() {
        var snapshot = new MemStorageSnapshot(new SnapshotOptions.None());
        MemStorage loaded = snapshot.load();

        assertTrue(loaded.getAllItems(TraineeEntity.class).isEmpty());
    }

    @Test
    void load_emptyDirectory_returnsEmptyStorage() {
        var snapshot = new MemStorageSnapshot(new SnapshotOptions.FromDirectory(tempDir));
        MemStorage loaded = snapshot.load();

        assertTrue(loaded.getAllItems(TraineeEntity.class).isEmpty());
    }

    @Test
    void load_invalidJson_throwsIllegalState() throws Exception {
        Files.writeString(tempDir.resolve("gym.data.entities.TraineeEntity.json"), "{ not json");

        var snapshot = new MemStorageSnapshot(new SnapshotOptions.FromDirectory(tempDir));

        assertThrows(IllegalStateException.class, snapshot::load);
    }
}
