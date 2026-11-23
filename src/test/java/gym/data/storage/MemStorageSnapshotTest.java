package gym.data.storage;

import gym.data.entity.TraineeEntity;
import gym.data.entity.TrainerEntity;
import gym.data.entity.TrainingEntity;
import gym.data.entity.UserEntity;
import gym.data.entity.TrainingTypeEntity;
import gym.data.entity.TrainingTypeEntity.TrainingType;
import gym.data.storage.keyed.KeyStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MemStorageSnapshotTest {

    private static final String FILLED = "snapshot-fixture/filled-storage";
    private static final String INVALID = "snapshot-fixture/invalid-storage";

    private record Storages(
            Storage<TraineeEntity, Long> trainees,
            Storage<TrainerEntity, Long> trainers,
            Storage<TrainingEntity, TrainingEntity.CompositeKey> trainings) {

        static Storages empty() {
            return new Storages(
                    new KeyStorage<>(TraineeEntity.class, TraineeEntity::getId),
                    new KeyStorage<>(TrainerEntity.class, TrainerEntity::getId),
                    new KeyStorage<>(TrainingEntity.class,
                            t -> new TrainingEntity.CompositeKey(t.getTrainerId(), t.getTraineeId())));
        }

        /** The same data as the files in {@link #FILLED}. */
        static Storages filled() {
            Storages storages = empty();
            storages.trainees().put(new TraineeEntity(5L, new UserEntity("Mia", "Wong", "Mia_Wong_1", 1L, "fixturePass", true), LocalDate.of(1995, 3, 2)));
            storages.trainers().put(new TrainerEntity(1L, new UserEntity("Anna", "Lee", "Anna_Lee", 0L, "secret67890", false), TrainingTypeEntity.of(TrainingType.CROSSFIT)));
            storages.trainings().put(new TrainingEntity(5L, 1L, "Morning",
                    TrainingTypeEntity.of(TrainingType.CROSSFIT), LocalDateTime.of(2026, 12, 1, 10, 0), Duration.ofMinutes(90)));
            return storages;
        }

        List<Storage<?, ?>> all() {
            return List.of(trainees, trainers, trainings);
        }
    }

    private static <T> void assertSameItems(Storage<T, ?> expected, Storage<T, ?> actual) {
        assertThat(actual.getAll())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expected.getAll());
    }

    private static <T, K> void store(MemStorageSnapshot snapshot, Storage<T, K> storage, Path directory) {
        snapshot.store(storage.getType(), storage, directory);
    }


    @Test
    void constructor_blankOrNullLocation_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new MemStorageSnapshot(" "));
        assertThrows(IllegalArgumentException.class, () -> new MemStorageSnapshot(null));
    }


    @Test
    void loadInto_fromResources_restoresEntitiesOfEachType() {
        var snapshot = new MemStorageSnapshot(FILLED);
        Storages loaded = Storages.empty();

        loaded.all().forEach(snapshot::loadInto);

        Storages expected = Storages.filled();
        assertSameItems(expected.trainees(), loaded.trainees());
        assertSameItems(expected.trainers(), loaded.trainers());
        assertSameItems(expected.trainings(), loaded.trainings());
    }

    @Test
    void loadInto_noFileForType_leavesStorageEmpty() {
        var trainers = Storages.empty().trainers();

        new MemStorageSnapshot(INVALID).loadInto(trainers);

        assertTrue(trainers.getAll().isEmpty());
    }

    @Test
    void loadInto_invalidJson_throwsIllegalState() {
        var snapshot = new MemStorageSnapshot(INVALID);
        var trainees = Storages.empty().trainees();

        assertThrows(IllegalStateException.class, () -> snapshot.loadInto(trainees));
    }

    @Test
    void loadInto_nullTarget_throwsIllegalArgument() {
        var snapshot = new MemStorageSnapshot(FILLED);

        assertThrows(IllegalArgumentException.class, () -> snapshot.loadInto(null));
    }


    @Test
    void store_filledStorages_matchesFixture(@TempDir Path tempDir) throws IOException {
        var snapshot = new MemStorageSnapshot(FILLED);

        Storages.filled().all().forEach(storage -> store(snapshot, storage, tempDir));

        var mapper = JsonMapper.builder().build();
        for (var type : List.of(TraineeEntity.class, TrainerEntity.class, TrainingEntity.class)) {
            String fileName = type.getName() + ".json";
            String resource = FILLED + "/" + fileName;

            try (var expected = getClass().getClassLoader().getResourceAsStream(resource);
                 var actual = Files.newInputStream(tempDir.resolve(fileName))) {
                assertNotNull(expected, "Missing snapshot fixture: " + resource);
                assertEquals(mapper.readTree(expected), mapper.readTree(actual), fileName);
            }
        }
    }
}
