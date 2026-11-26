package gym.service.mapper;

import gym.data.TraineeEntity;
import gym.data.TrainerEntity;
import gym.data.TrainingEntity;
import gym.data.TrainingTypeEntity;
import gym.data.UserEntity;
import gym.enums.TrainingType;
import gym.dto.request.UpdateTrainee;
import gym.dto.request.UpdateTrainer;
import gym.dto.response.Trainee;
import gym.dto.response.Trainer;
import gym.dto.response.Training;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GymMapperTest {

    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 5, 17);
    private static final LocalDateTime START = LocalDateTime.of(2026, 12, 1, 10, 0);

    private final GymMapper mapper = new GymMapper();

    private static UserEntity johnUser() {
        return new UserEntity("John", "Smith", "secret12345", true, "John_Smith", "John_Smith", 0);
    }

    private static UserEntity annaUser() {
        return new UserEntity("Anna", "Lee", "secret67890", false, "Anna_Lee", "Anna_Lee", 0);
    }

    private static TrainingTypeEntity trainingType(TrainingType type) {
        TrainingTypeEntity entity = mock(TrainingTypeEntity.class);
        when(entity.getTrainingType()).thenReturn(type);
        return entity;
    }

    @Test
    void toTrainee_mapsUserAndTraineeFields() {
        TraineeEntity trainee = new TraineeEntity(johnUser(), BIRTH_DATE, "1 Main St");

        var expectedTrainee = new Trainee(null, "John_Smith", "John", "Smith", BIRTH_DATE, "1 Main St");
        assertEquals(expectedTrainee, mapper.toTrainee(trainee));
    }

    @Test
    void toTrainer_mapsUserAndSpecialization() {
        TrainerEntity trainer = new TrainerEntity(annaUser(), trainingType(TrainingType.CROSSFIT));

        var expectedTrainer = new Trainer(null, "Anna_Lee", "Anna", "Lee", TrainingType.CROSSFIT);
        assertEquals(expectedTrainer, mapper.toTrainer(trainer));
    }

    @Test
    void toTraining_mapsParticipantsByUsernameAndDuration() {
        var trainee = new TraineeEntity(johnUser(), BIRTH_DATE, null);
        var trainer = new TrainerEntity(annaUser(), trainingType(TrainingType.CROSSFIT));
        var crossfit = trainingType(TrainingType.CROSSFIT);
        TrainingEntity training = new TrainingEntity(trainee, trainer, "Morning WOD", crossfit, START, 90);

        var expectedTrainee = new Trainee(null, "John_Smith", "John", "Smith", BIRTH_DATE, null);
        var expectedTrainer = new Trainer(null, "Anna_Lee", "Anna", "Lee", TrainingType.CROSSFIT);
        var expectedTraining = new Training(
                null, expectedTrainee, expectedTrainer, "Morning WOD", TrainingType.CROSSFIT, START, 90);
        assertEquals(expectedTraining, mapper.toTraining(training));
    }

    @Test
    void applyUpdateTrainee_changesOnlyProvidedFields() {
        TraineeEntity trainee = new TraineeEntity(johnUser(), BIRTH_DATE, "1 Main St");

        var request = new UpdateTrainee(null, "Brown", null, "2 New Rd");
        mapper.applyUpdate(request, trainee);

        var expectedTrainee = new Trainee(null, "John_Smith", "John", "Brown", BIRTH_DATE, "2 New Rd");
        assertEquals(expectedTrainee, mapper.toTrainee(trainee));
    }

    @Test
    void applyUpdateTrainer_changesOnlyProvidedNames() {
        TrainerEntity trainer = new TrainerEntity(annaUser(), trainingType(TrainingType.FITNESS));

        var specialization = Optional.of(trainingType(TrainingType.FITNESS));
        var request = new UpdateTrainer("Hanna", null, null);
        mapper.applyUpdate(request, trainer, specialization);

        var expectedTrainer = new Trainer(null, "Anna_Lee", "Hanna", "Lee", TrainingType.FITNESS);
        assertEquals(expectedTrainer, mapper.toTrainer(trainer));
    }
}
