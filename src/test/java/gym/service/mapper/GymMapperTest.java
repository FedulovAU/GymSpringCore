package gym.service.mapper;

import gym.data.entity.TraineeEntity;
import gym.data.entity.TrainerEntity;
import gym.data.entity.TrainingEntity;
import gym.data.entity.UserEntity;
import gym.data.enums.TrainingType;
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
import static org.junit.jupiter.api.Assertions.*;

class GymMapperTest {

    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 5, 17);
    private static final LocalDateTime START = LocalDateTime.of(2026, 12, 1, 10, 0);
    private static final Duration DURATION = Duration.ofMinutes(90);

    private final GymMapper mapper = new GymMapper();

    private static UserEntity johnUser() {
        return new UserEntity(1L, "John", "Smith", "John_Smith", 0L, "secret12345", true);
    }

    private static UserEntity annaUser() {
        return new UserEntity(2L, "Anna", "Lee", "Anna_Lee", 0L, "secret67890", false);
    }


    @Test
    void toUserEntity_fromCreateTrainee_activeWithoutIdAndUserName() {
        UserEntity result = mapper.toUserEntity(new CreateTrainee("John", "Smith", BIRTH_DATE), "secret12345");

        assertThat(result).usingRecursiveComparison()
                .isEqualTo(new UserEntity(null, "John", "Smith", null, null, "secret12345", true));
    }

    @Test
    void toUserEntity_fromCreateTrainer_activeWithoutIdAndUserName() {
        UserEntity result = mapper.toUserEntity(
                new CreateTrainer("Anna", "Lee", gym.dto.enums.TrainingType.CROSSFIT), "secret67890");

        assertThat(result).usingRecursiveComparison()
                .isEqualTo(new UserEntity(null, "Anna", "Lee", null, null, "secret67890", true));
    }

    @Test
    void toTraineeEntity_takesIdFromUser() {
        TraineeEntity result = mapper.toTraineeEntity(new CreateTrainee("John", "Smith", BIRTH_DATE), johnUser());

        assertThat(result).usingRecursiveComparison().isEqualTo(new TraineeEntity(1L, BIRTH_DATE));
    }

    @Test
    void toTrainerEntity_takesIdFromUserAndMapsSpecialization() {
        TrainerEntity result = mapper.toTrainerEntity(
                new CreateTrainer("Anna", "Lee", gym.dto.enums.TrainingType.CROSSFIT), annaUser());

        assertThat(result).usingRecursiveComparison().isEqualTo(new TrainerEntity(2L, TrainingType.CROSSFIT));
    }

    @Test
    void toTrainingEntity_takesIdsFromEntitiesAndTypeFromTrainerSpecialization() {
        TrainingEntity result = mapper.toTrainingEntity(
                new ScheduleTraining(100L, 200L, "Morning", START, DURATION),
                new TraineeEntity(1L, BIRTH_DATE),
                new TrainerEntity(2L, TrainingType.CROSSFIT));

        // The IDs in the request are ignored in favour of the loaded entities' IDs.
        assertThat(result).usingRecursiveComparison().isEqualTo(new TrainingEntity(
                1L, 2L, "Morning", TrainingType.CROSSFIT, START, DURATION));
    }

    @Test
    void toUpdatedTrainee_withBirthDate_replacesIt_withoutBirthDate_keepsIt() {
        LocalDate newBirthDate = LocalDate.of(1991, 1, 1);
        TraineeEntity existing = new TraineeEntity(1L, BIRTH_DATE);

        assertEquals(newBirthDate,
                mapper.toUpdatedTrainee(new UpdateTrainee(1L, null, null, newBirthDate), existing).getBirthDate());
        assertEquals(BIRTH_DATE,
                mapper.toUpdatedTrainee(new UpdateTrainee(1L, "Jack", null, null), existing).getBirthDate());
    }

    @Test
    void toUpdatedUser_fromUpdateTrainee_updatesOnlyProvidedNamesAndKeepsUserName() {
        UserEntity result = mapper.toUpdatedUser(new UpdateTrainee(1L, null, "Brown", null), johnUser());

        assertThat(result).usingRecursiveComparison()
                .isEqualTo(new UserEntity(1L, "John", "Brown", "John_Smith", 0L, "secret12345", true));
    }

    @Test
    void toUpdatedUser_fromUpdateTrainer_updatesOnlyProvidedNamesAndKeepsUserName() {
        UserEntity result = mapper.toUpdatedUser(new UpdateTrainer(2L, "Hanna", null, null), annaUser());

        assertThat(result).usingRecursiveComparison()
                .isEqualTo(new UserEntity(2L, "Hanna", "Lee", "Anna_Lee", 0L, "secret67890", false));
    }

    @Test
    void toUpdatedTrainer_withSpecialization_replacesIt_withoutSpecialization_keepsIt() {
        TrainerEntity existing = new TrainerEntity(2L, TrainingType.CROSSFIT);

        assertEquals(TrainingType.FITNESS, mapper.toUpdatedTrainer(
                new UpdateTrainer(2L, null, null, gym.dto.enums.TrainingType.FITNESS), existing).getSpecialization());
        assertEquals(TrainingType.CROSSFIT, mapper.toUpdatedTrainer(
                new UpdateTrainer(2L, "Hanna", null, null), existing).getSpecialization());
    }

    @Test
    void toTrainee_combinesTraineeAndUser_withoutPassword() {
        assertEquals(new Trainee(1L, "John", "Smith", "John_Smith", BIRTH_DATE),
                mapper.toTrainee(new TraineeEntity(1L, BIRTH_DATE), johnUser()));
    }

    @Test
    void toTrainer_combinesTrainerAndUser_withoutPassword() {
        assertEquals(new Trainer(2L, "Anna", "Lee", "Anna_Lee", gym.dto.enums.TrainingType.CROSSFIT),
                mapper.toTrainer(new TrainerEntity(2L, TrainingType.CROSSFIT), annaUser()));
    }

    @Test
    void toTraining_mapsAllFields() {
        TrainingEntity entity = new TrainingEntity(1L, 2L, "Morning", TrainingType.FITNESS, START, DURATION);

        assertEquals(new Training(1L, 2L, "Morning", gym.dto.enums.TrainingType.FITNESS, START, DURATION),
                mapper.toTraining(entity));
    }

    @Test
    void toTrainingType_fromDto_mapsEveryValueAndNull() {
        for (gym.dto.enums.TrainingType dtoType : gym.dto.enums.TrainingType.values()) {
            assertEquals(dtoType.name(), mapper.toTrainingType(dtoType).name());
        }
        assertNull(mapper.toTrainingType((gym.dto.enums.TrainingType) null));
    }

    @Test
    void toTrainingType_fromEntity_mapsEveryValueAndNull() {
        for (TrainingType entityType : TrainingType.values()) {
            assertEquals(entityType.name(), mapper.toTrainingType(entityType).name());
        }
        assertNull(mapper.toTrainingType((TrainingType) null));
    }
}
