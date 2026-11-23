package gym.service.mapper;

import gym.data.entity.TraineeEntity;
import gym.data.entity.TrainerEntity;
import gym.data.entity.TrainingEntity;
import gym.data.entity.UserEntity;
import gym.data.entity.TrainingTypeEntity;
import gym.dto.enums.TrainingType;
import gym.dto.request.CreateTrainee;
import gym.dto.request.CreateTrainer;
import gym.dto.request.ScheduleTraining;
import gym.dto.request.UpdateTrainee;
import gym.dto.request.UpdateTrainer;
import gym.dto.response.Trainee;
import gym.dto.response.Trainer;
import gym.dto.response.Training;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GymMapperTest {

    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 5, 17);
    private static final LocalDateTime START = LocalDateTime.of(2026, 12, 1, 10, 0);
    private static final Duration DURATION = Duration.ofMinutes(90);

    private final GymMapper mapper = new GymMapper();

    private static UserEntity johnUser() {
        return new UserEntity("John", "Smith", "John_Smith", 0L, "secret12345", true);
    }

    private static UserEntity annaUser() {
        return new UserEntity("Anna", "Lee", "Anna_Lee", 0L, "secret67890", false);
    }


    @Test
    void toTraineeEntity_withoutIdAndWithActiveUser() {
        TraineeEntity result = mapper.toTraineeEntity(
                new CreateTrainee("John", "Smith", BIRTH_DATE), "John_Smith", 0L, "secret12345");

        assertThat(result).usingRecursiveComparison().isEqualTo(new TraineeEntity(null, johnUser(), BIRTH_DATE));
    }

    @Test
    void toTrainerEntity_withoutIdAndWithActiveUser_mapsSpecialization() {
        TrainerEntity result = mapper.toTrainerEntity(
                new CreateTrainer("Anna", "Lee", TrainingType.CROSSFIT), "Anna_Lee_2", 2L, "secret67890");

        assertThat(result).usingRecursiveComparison().isEqualTo(new TrainerEntity(null,
                new UserEntity("Anna", "Lee", "Anna_Lee_2", 2L, "secret67890", true),
                TrainingTypeEntity.of(TrainingTypeEntity.TrainingType.CROSSFIT)));
    }

    @Test
    void toTrainingEntity_takesIdsFromEntitiesAndTypeFromTrainerSpecialization() {
        TrainingEntity result = mapper.toTrainingEntity(
                new ScheduleTraining(100L, 200L, "Morning", START, DURATION),
                new TraineeEntity(1L, johnUser(), BIRTH_DATE),
                new TrainerEntity(2L, annaUser(), TrainingTypeEntity.of(TrainingTypeEntity.TrainingType.CROSSFIT)));

        // The IDs in the request are ignored in favour of the loaded entities' IDs.
        assertThat(result).usingRecursiveComparison().isEqualTo(new TrainingEntity(
                1L, 2L, "Morning", TrainingTypeEntity.of(TrainingTypeEntity.TrainingType.CROSSFIT), START, DURATION));
    }

    @Test
    void toUpdatedTrainee_updatesOnlyProvidedFieldsAndKeepsUserName() {
        LocalDate newBirthDate = LocalDate.of(1991, 1, 1);
        TraineeEntity existing = new TraineeEntity(1L, johnUser(), BIRTH_DATE);

        TraineeEntity result = mapper.toUpdatedTrainee(new UpdateTrainee(1L, null, "Brown", newBirthDate), existing);

        assertThat(result).usingRecursiveComparison().isEqualTo(new TraineeEntity(1L,
                new UserEntity("John", "Brown", "John_Smith", 0L, "secret12345", true), newBirthDate));
    }

    @Test
    void toUpdatedTrainee_nothingProvided_keepsEverything() {
        TraineeEntity existing = new TraineeEntity(1L, johnUser(), BIRTH_DATE);

        TraineeEntity result = mapper.toUpdatedTrainee(new UpdateTrainee(1L, null, null, null), existing);

        assertThat(result).usingRecursiveComparison().isEqualTo(existing);
    }

    @Test
    void toUpdatedTrainer_updatesOnlyProvidedFieldsAndKeepsUserName() {
        TrainerEntity existing = new TrainerEntity(2L, annaUser(), TrainingTypeEntity.of(TrainingTypeEntity.TrainingType.CROSSFIT));

        TrainerEntity result = mapper.toUpdatedTrainer(
                new UpdateTrainer(2L, "Hanna", null, TrainingType.FITNESS), existing);

        assertThat(result).usingRecursiveComparison().isEqualTo(new TrainerEntity(2L,
                new UserEntity("Hanna", "Lee", "Anna_Lee", 0L, "secret67890", false), TrainingTypeEntity.of(TrainingTypeEntity.TrainingType.FITNESS)));
    }

    @Test
    void toUpdatedTrainer_nothingProvided_keepsEverything() {
        TrainerEntity existing = new TrainerEntity(2L, annaUser(), TrainingTypeEntity.of(TrainingTypeEntity.TrainingType.CROSSFIT));

        TrainerEntity result = mapper.toUpdatedTrainer(new UpdateTrainer(2L, null, null, null), existing);

        assertThat(result).usingRecursiveComparison().isEqualTo(existing);
    }

    @Test
    void toTrainee_takesUserFields_withoutPassword() {
        assertEquals(new Trainee(1L, "John", "Smith", "John_Smith", BIRTH_DATE),
                mapper.toTrainee(new TraineeEntity(1L, johnUser(), BIRTH_DATE)));
    }

    @Test
    void toTrainer_takesUserFields_withoutPassword() {
        assertEquals(new Trainer(2L, "Anna", "Lee", "Anna_Lee", TrainingType.CROSSFIT),
                mapper.toTrainer(new TrainerEntity(2L, annaUser(), TrainingTypeEntity.of(TrainingTypeEntity.TrainingType.CROSSFIT))));
    }

    @Test
    void toTraining_mapsAllFields() {
        TrainingEntity entity = new TrainingEntity(1L, 2L, "Morning", TrainingTypeEntity.of(TrainingTypeEntity.TrainingType.FITNESS), START, DURATION);

        assertEquals(new Training(1L, 2L, "Morning", TrainingType.FITNESS, START, DURATION),
                mapper.toTraining(entity));
    }

    @Test
    void toTrainingType_fromDto_mapsEveryValueAndNull() {
        for (TrainingType dtoType : TrainingType.values()) {
            assertEquals(dtoType.name(), mapper.toTrainingType(dtoType).getTrainingType().name());
        }
        assertNull(mapper.toTrainingType((TrainingType) null));
    }

    @Test
    void toTrainingType_fromEntity_mapsEveryValueAndNull() {
        for (TrainingTypeEntity.TrainingType entityType : TrainingTypeEntity.TrainingType.values()) {
            assertEquals(entityType.name(), mapper.toTrainingType(TrainingTypeEntity.of(entityType)).name());
        }
        assertNull(mapper.toTrainingType((TrainingTypeEntity) null));
    }
}
