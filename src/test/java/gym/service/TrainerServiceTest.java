package gym.service;

import gym.data.TraineeEntity;
import gym.data.TrainerEntity;
import gym.data.TrainingTypeEntity;
import gym.data.UserEntity;
import gym.enums.TrainingType;
import gym.dto.request.CreateTrainer;
import gym.dto.request.UpdateTrainer;
import gym.dto.response.Trainer;
import gym.repository.TraineeRepository;
import gym.repository.TrainerRepository;
import gym.repository.TrainingTypeRepository;
import gym.service.exception.AuthenticationException;
import gym.service.exception.ProfileNotFoundException;
import gym.service.helper.UserFactory;
import gym.service.mapper.GymMapper;
import gym.service.security.AuthenticatedUser;
import gym.service.security.AuthenticationContext;
import gym.service.security.UserRole;
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
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    private static final String USERNAME = "Anna_Lee";
    private static final String TRAINEE_USERNAME = "John_Smith";
    private static final String PASSWORD = "secret12345";

    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private UserFactory userFactory;

    private AuthenticationContext authenticationContext;
    private TrainerService service;

    @BeforeEach
    void setUp() {
        authenticationContext = new AuthenticationContext();
        service = new TrainerService(
                trainerRepository,
                traineeRepository,
                trainingTypeRepository,
                userFactory,
                new RequestValidator(),
                new GymMapper(),
                authenticationContext);
    }

    private static UserEntity user() {
        return new UserEntity("Anna", "Lee", "secret12345", true, USERNAME, USERNAME, 0);
    }

    private static TrainerEntity trainer() {
        return new TrainerEntity(user(), mock(TrainingTypeEntity.class));
    }

    private void signInAsTrainer() {
        authenticationContext.signIn(new AuthenticatedUser(USERNAME, UserRole.TRAINER));
    }

    @Test
    void createTrainer_savesTrainerWithSpecialization_returnsProfile() {
        TrainingTypeEntity crossfit = mock(TrainingTypeEntity.class);

        when(crossfit.getTrainingType()).thenReturn(TrainingType.CROSSFIT);
        when(trainingTypeRepository.findByType(TrainingType.CROSSFIT)).thenReturn(Optional.of(crossfit));
        when(userFactory.createUser("Anna", "Lee", PASSWORD)).thenReturn(user());
        when(trainerRepository.save(any())).then(returnsFirstArg());

        var request = new CreateTrainer("Anna", "Lee", PASSWORD, TrainingType.CROSSFIT);
        Trainer result = service.createTrainer(request);

        var expectedTrainer = new Trainer(null, USERNAME, "Anna", "Lee", TrainingType.CROSSFIT);
        assertEquals(expectedTrainer, result);
        verify(trainerRepository).save(argThat(t -> t.getUser().getUsername().equals(USERNAME)
                && t.getUser().getPassword().equals(PASSWORD)
                && t.getSpecialization() == crossfit));
        verify(trainingTypeRepository).findByType(TrainingType.CROSSFIT);
        verify(userFactory).createUser("Anna", "Lee", PASSWORD);
        verify(crossfit).getTrainingType();
    }

    @Test
    void createTrainer_missingSpecialization_throwsBeforeTouchingDatabase() {
        var request = new CreateTrainer("Anna", "Lee", PASSWORD, null);
        assertThrows(IllegalArgumentException.class, () -> service.createTrainer(request));

        verifyNoInteractions(trainerRepository, trainingTypeRepository, userFactory);
    }

    @Test
    void matchesCredentials_correctPassword_returnsTrue() {
        var trainer = trainer();
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

        assertTrue(service.matchesCredentials(USERNAME, "secret12345"));
        verify(trainerRepository).findByUsername(USERNAME);
    }

    @Test
    void matchesCredentials_unknownUsername_returnsFalse() {
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertFalse(service.matchesCredentials(USERNAME, "secret12345"));
        verify(trainerRepository).findByUsername(USERNAME);
    }

    @Test
    void findByUsername_exists_returnsProfile() {
        signInAsTrainer();
        var trainer = trainer();
        var specializationMock = trainer.getSpecialization();

        when(specializationMock.getTrainingType()).thenReturn(TrainingType.FITNESS);
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));


        var expectedTrainer = new Trainer(null, USERNAME, "Anna", "Lee", TrainingType.FITNESS);
        assertEquals(Optional.of(expectedTrainer), service.findByUsername(USERNAME));
        verify(trainerRepository).findByUsername(USERNAME);
        verify(specializationMock).getTrainingType();
    }

    @Test
    void changePassword_setsNewPasswordOnManagedEntity() {
        signInAsTrainer();
        TrainerEntity trainer = trainer();
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

        service.changePassword("newPassword1");

        assertEquals("newPassword1", trainer.getUser().getPassword());
        verify(trainerRepository).findByUsername(USERNAME);
    }

    @Test
    void updateTrainer_withSpecialization_changesNamesAndSpecialization() {
        signInAsTrainer();
        var trainer = trainer();
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

        TrainingTypeEntity crossfit = mock(TrainingTypeEntity.class);
        when(crossfit.getTrainingType()).thenReturn(TrainingType.CROSSFIT);
        when(trainingTypeRepository.findByType(TrainingType.CROSSFIT)).thenReturn(Optional.of(crossfit));

        var request = new UpdateTrainer("Hanna", null, TrainingType.CROSSFIT);
        Trainer result = service.updateTrainer(request);

        var expectedTrainer = new Trainer(null, USERNAME, "Hanna", "Lee", TrainingType.CROSSFIT);
        assertEquals(expectedTrainer, result);
        verify(trainerRepository).findByUsername(USERNAME);
        verify(trainingTypeRepository).findByType(TrainingType.CROSSFIT);
    }

    @Test
    void updateTrainer_withoutSpecialization_keepsSpecialization() {
        signInAsTrainer();
        var trainer = trainer();
        var specialization = trainer.getSpecialization();
        when(specialization.getTrainingType()).thenReturn(TrainingType.FITNESS);
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

        var request = new UpdateTrainer(null, "Brown", null);
        Trainer result = service.updateTrainer(request);

        var expectedTrainer = new Trainer(null, USERNAME, "Anna", "Brown", TrainingType.FITNESS);
        assertEquals(expectedTrainer, result);
        verifyNoInteractions(trainingTypeRepository);
        verify(trainerRepository).findByUsername(USERNAME);
        verify(specialization).getTrainingType();
    }

    @Test
    void updateTrainer_missingTrainer_throwsProfileNotFound() {
        signInAsTrainer();
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        var request = new UpdateTrainer("Hanna", null, null);
        assertThrows(ProfileNotFoundException.class, () -> service.updateTrainer(request));
        verify(trainerRepository).findByUsername(USERNAME);
    }

    @Test
    void switchActive_activeUser_deactivates() {
        signInAsTrainer();
        TrainerEntity trainer = trainer();
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

        boolean isActive = service.switchActive();

        assertFalse(isActive);
        assertFalse(trainer.getUser().isActive());
        verify(trainerRepository).findByUsername(USERNAME);
    }

    @Test
    void switchActive_inactiveUser_activates() {
        signInAsTrainer();
        TrainerEntity trainer = trainer();
        trainer.getUser().setActive(false);
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

        boolean isActive = service.switchActive();

        assertTrue(isActive);
        assertTrue(trainer.getUser().isActive());
        verify(trainerRepository).findByUsername(USERNAME);
    }

    @Test
    void getTrainersNotAssignedToTrainee_traineeExists_returnsMappedTrainers() {
        signInAsTrainer();
        TraineeEntity trainee = mock(TraineeEntity.class);
        TrainerEntity trainer = trainer();
        var specialization = trainer.getSpecialization();

        when(specialization.getTrainingType()).thenReturn(TrainingType.FITNESS);
        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findNotAssignedToTrainee(TRAINEE_USERNAME)).thenReturn(List.of(trainer));

        var expectedTrainer = new Trainer(null, USERNAME, "Anna", "Lee", TrainingType.FITNESS);
        assertEquals(List.of(expectedTrainer), service.getTrainersNotAssignedToTrainee(TRAINEE_USERNAME));
        verify(traineeRepository).findByUsername(TRAINEE_USERNAME);
        verify(trainerRepository).findNotAssignedToTrainee(TRAINEE_USERNAME);
        verify(specialization).getTrainingType();
    }

    @Test
    void getTrainersNotAssignedToTrainee_unknownTrainee_throwsProfileNotFound() {
        signInAsTrainer();
        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class,
                () -> service.getTrainersNotAssignedToTrainee(TRAINEE_USERNAME));

        verify(trainerRepository, never()).findNotAssignedToTrainee(any());
        verify(traineeRepository).findByUsername(TRAINEE_USERNAME);
    }

    @Test
    void updateTrainer_notSignedIn_throwsBeforeTouchingDatabase() {
        var request = new UpdateTrainer("Hanna", null, null);
        assertThrows(AuthenticationException.class, () -> service.updateTrainer(request));

        verifyNoInteractions(trainerRepository, trainingTypeRepository);
    }

    @Test
    void changePassword_signedInAsTrainee_throwsBeforeTouchingDatabase() {
        authenticationContext.signIn(new AuthenticatedUser(TRAINEE_USERNAME, UserRole.TRAINEE));

        assertThrows(AuthenticationException.class, () -> service.changePassword("newPassword1"));

        verifyNoInteractions(trainerRepository);
    }

    @Test
    void getTrainersNotAssignedToTrainee_notSignedIn_throwsBeforeTouchingDatabase() {
        assertThrows(AuthenticationException.class,
                () -> service.getTrainersNotAssignedToTrainee(TRAINEE_USERNAME));

        verifyNoInteractions(trainerRepository, traineeRepository);
    }

    @Test
    void signIn_correctPassword_signsInAsTrainer() {
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer()));

        service.signIn(USERNAME, PASSWORD);

        assertEquals(USERNAME, authenticationContext.getUserByRole(UserRole.TRAINER));
        verify(trainerRepository).findByUsername(USERNAME);
    }

    @Test
    void signIn_wrongPassword_throwsAndStaysSignedOut() {
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainer()));

        assertThrows(AuthenticationException.class, () -> service.signIn(USERNAME, "wrong"));

        assertThrows(AuthenticationException.class, () -> authenticationContext.requireSignedIn());
        verify(trainerRepository).findByUsername(USERNAME);
    }
}
