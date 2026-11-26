package gym.service;

import gym.data.TraineeEntity;
import gym.data.TrainerEntity;
import gym.data.TrainingTypeEntity;
import gym.data.UserEntity;
import gym.enums.TrainingType;
import gym.dto.request.CreateTrainer;
import gym.dto.request.UpdateTrainer;
import gym.dto.response.Credentials;
import gym.dto.response.Trainer;
import gym.repository.TraineeRepository;
import gym.repository.TrainerRepository;
import gym.repository.TrainingTypeRepository;
import gym.service.exception.ProfileNotFoundException;
import gym.service.helper.UserFactory;
import gym.service.mapper.GymMapper;
import gym.service.validation.RequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    private static final String USERNAME = "Anna_Lee";
    private static final String TRAINEE_USERNAME = "John_Smith";

    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private UserFactory userFactory;

    private TrainerService service;

    @BeforeEach
    void setUp() {
        service = new TrainerService(
                trainerRepository,
                traineeRepository,
                trainingTypeRepository,
                userFactory,
                new RequestValidator(),
                new GymMapper());
    }

    private static TrainingTypeEntity trainingType(TrainingType type) {
        TrainingTypeEntity entity = mock(TrainingTypeEntity.class);
        lenient().when(entity.getTrainingType()).thenReturn(type);
        return entity;
    }

    private static UserEntity user() {
        return new UserEntity("Anna", "Lee", "secret12345", true, USERNAME, USERNAME, 0);
    }

    private static TrainerEntity trainer() {
        return new TrainerEntity(user(), trainingType(TrainingType.FITNESS));
    }

    private void stubFound(TrainerEntity trainer) {
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
    }

    private void stubMissing() {
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
    }

    @Test
    void createTrainer_savesTrainerWithSpecialization_returnsCredentials() {
        TrainingTypeEntity crossfit = trainingType(TrainingType.CROSSFIT);
        when(trainingTypeRepository.findByType(TrainingType.CROSSFIT)).thenReturn(Optional.of(crossfit));
        when(userFactory.createUser("Anna", "Lee")).thenReturn(user());

        var request = new CreateTrainer("Anna", "Lee", TrainingType.CROSSFIT);
        Credentials result = service.createTrainer(request);

        var expectedCredentials = new Credentials(USERNAME, "secret12345");
        assertEquals(expectedCredentials, result);
        verify(trainerRepository).save(argThat(t -> t.getUser().getUsername().equals(USERNAME)
                && t.getSpecialization() == crossfit));
    }

    @Test
    void createTrainer_missingSpecialization_throwsBeforeTouchingDatabase() {
        var request = new CreateTrainer("Anna", "Lee", null);
        assertThrows(IllegalArgumentException.class, () -> service.createTrainer(request));

        verifyNoInteractions(trainerRepository, trainingTypeRepository, userFactory);
    }

    @Test
    void matchesCredentials_correctPassword_returnsTrue() {
        stubFound(trainer());

        assertTrue(service.matchesCredentials(USERNAME, "secret12345"));
    }

    @Test
    void matchesCredentials_unknownUsername_returnsFalse() {
        stubMissing();

        assertFalse(service.matchesCredentials(USERNAME, "secret12345"));
    }

    @Test
    void findByUsername_exists_returnsProfile() {
        stubFound(trainer());

        var expectedTrainer = new Trainer(USERNAME, "Anna", "Lee", TrainingType.FITNESS, true);
        assertEquals(Optional.of(expectedTrainer), service.findByUsername(USERNAME));
    }

    @Test
    void changePassword_setsNewPasswordOnManagedEntity() {
        TrainerEntity trainer = trainer();
        stubFound(trainer);

        service.changePassword(USERNAME, "newPassword1");

        assertEquals("newPassword1", trainer.getUser().getPassword());
    }

    @Test
    void updateTrainer_withSpecialization_changesNamesAndSpecialization() {
        stubFound(trainer());
        TrainingTypeEntity crossfit = trainingType(TrainingType.CROSSFIT);
        when(trainingTypeRepository.findByType(TrainingType.CROSSFIT)).thenReturn(Optional.of(crossfit));

        var request = new UpdateTrainer(USERNAME, "Hanna", null, TrainingType.CROSSFIT);
        Trainer result = service.updateTrainer(request);

        var expectedTrainer = new Trainer(USERNAME, "Hanna", "Lee", TrainingType.CROSSFIT, true);
        assertEquals(expectedTrainer, result);
    }

    @Test
    void updateTrainer_withoutSpecialization_keepsSpecialization() {
        stubFound(trainer());

        var request = new UpdateTrainer(USERNAME, null, "Brown", null);
        Trainer result = service.updateTrainer(request);

        var expectedTrainer = new Trainer(USERNAME, "Anna", "Brown", TrainingType.FITNESS, true);
        assertEquals(expectedTrainer, result);
        verifyNoInteractions(trainingTypeRepository);
    }

    @Test
    void updateTrainer_missingTrainer_throwsProfileNotFound() {
        stubMissing();

        var request = new UpdateTrainer(USERNAME, "Hanna", null, null);
        assertThrows(ProfileNotFoundException.class, () -> service.updateTrainer(request));
    }

    @Test
    void setActive_false_deactivatesUser() {
        TrainerEntity trainer = trainer();
        stubFound(trainer);

        service.setActive(USERNAME, false);

        assertFalse(trainer.getUser().isActive());
    }

    @Test
    void getTrainersNotAssignedToTrainee_traineeExists_returnsMappedTrainers() {
        TraineeEntity trainee = mock(TraineeEntity.class);
        TrainerEntity trainer = trainer();
        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findNotAssignedToTrainee(TRAINEE_USERNAME)).thenReturn(List.of(trainer));

        var expectedTrainer = new Trainer(USERNAME, "Anna", "Lee", TrainingType.FITNESS, true);
        assertEquals(List.of(expectedTrainer), service.getTrainersNotAssignedToTrainee(TRAINEE_USERNAME));
    }

    @Test
    void getTrainersNotAssignedToTrainee_unknownTrainee_throwsProfileNotFound() {
        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class,
                () -> service.getTrainersNotAssignedToTrainee(TRAINEE_USERNAME));

        verify(trainerRepository, never()).findNotAssignedToTrainee(any());
    }
}
