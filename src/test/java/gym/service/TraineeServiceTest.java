package gym.service;

import gym.data.TraineeEntity;
import gym.data.UserEntity;
import gym.dto.request.CreateTrainee;
import gym.dto.request.UpdateTrainee;
import gym.dto.response.Credentials;
import gym.dto.response.Trainee;
import gym.repository.TraineeRepository;
import gym.service.exception.ProfileNotFoundException;
import gym.service.helper.UserFactory;
import gym.service.mapper.GymMapper;
import gym.service.validation.RequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 5, 17);
    private static final String USERNAME = "John_Smith";

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private UserFactory userFactory;

    private TraineeService service;

    @BeforeEach
    void setUp() {
        service = new TraineeService(
                traineeRepository,
                userFactory,
                new RequestValidator(),
                new GymMapper());
    }

    private static UserEntity user() {
        return new UserEntity("John", "Smith", "secret12345", true, USERNAME, USERNAME, 0);
    }

    private static TraineeEntity trainee() {
        return new TraineeEntity(user(), BIRTH_DATE, "1 Main St");
    }

    @Test
    void createTrainee_savesTraineeWithNewUser_returnsCredentials() {
        when(userFactory.createUser("John", "Smith")).thenReturn(user());

        var request = new CreateTrainee("John", "Smith", BIRTH_DATE, "1 Main St");
        Credentials result = service.createTrainee(request);

        var expectedCredentials = new Credentials(USERNAME, "secret12345");
        assertEquals(expectedCredentials, result);
        verify(traineeRepository).save(argThat(t -> t.getUser().getUsername().equals(USERNAME)
                && t.getDateOfBirth().equals(BIRTH_DATE)
                && t.getAddress().equals("1 Main St")));
        verify(userFactory).createUser("John", "Smith");
    }

    @Test
    void createTrainee_invalidName_throwsBeforeTouchingDatabase() {
        var request = new CreateTrainee("John1", "Smith", BIRTH_DATE, null);
        assertThrows(IllegalArgumentException.class, () -> service.createTrainee(request));

        verifyNoInteractions(traineeRepository, userFactory);
    }

    @Test
    void matchesCredentials_correctPassword_returnsTrue() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee()));

        assertTrue(service.matchesCredentials(USERNAME, "secret12345"));
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void matchesCredentials_wrongPassword_returnsFalse() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee()));

        assertFalse(service.matchesCredentials(USERNAME, "wrong"));
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void matchesCredentials_unknownUsername_returnsFalse() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertFalse(service.matchesCredentials(USERNAME, "secret12345"));
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void findByUsername_exists_returnsProfile() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee()));

        var expectedTrainee = new Trainee(USERNAME, "John", "Smith", BIRTH_DATE, "1 Main St", true);
        assertEquals(Optional.of(expectedTrainee), service.findByUsername(USERNAME));
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void findByUsername_missing_returnsEmpty() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertTrue(service.findByUsername(USERNAME).isEmpty());
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void changePassword_setsNewPasswordOnManagedEntity() {
        TraineeEntity trainee = trainee();
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        service.changePassword(USERNAME, "newPassword1");

        assertEquals("newPassword1", trainee.getUser().getPassword());
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void changePassword_blankPassword_throwsBeforeTouchingDatabase() {
        assertThrows(IllegalArgumentException.class, () -> service.changePassword(USERNAME, " "));

        verifyNoInteractions(traineeRepository);
    }

    @Test
    void changePassword_missingTrainee_throwsProfileNotFound() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> service.changePassword(USERNAME, "newPassword1"));
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void updateTrainee_partialRequest_changesProvidedFieldsAndKeepsUsername() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee()));
        LocalDate newBirthDate = LocalDate.of(1991, 1, 1);

        var request = new UpdateTrainee(USERNAME, null, "Brown", newBirthDate, null);
        Trainee result = service.updateTrainee(request);

        var expectedTrainee = new Trainee(USERNAME, "John", "Brown", newBirthDate, "1 Main St", true);
        assertEquals(expectedTrainee, result);
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void updateTrainee_missingTrainee_throwsProfileNotFound() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        var request = new UpdateTrainee(USERNAME, "Jack", null, null, null);
        assertThrows(ProfileNotFoundException.class, () -> service.updateTrainee(request));
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void setActive_false_deactivatesUser() {
        TraineeEntity trainee = trainee();
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        service.setActive(USERNAME, false);

        assertFalse(trainee.getUser().isActive());
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void deleteByUsername_exists_deletesTrainee() {
        TraineeEntity trainee = trainee();
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        service.deleteByUsername(USERNAME);

        verify(traineeRepository).delete(trainee);
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void deleteByUsername_missing_throwsAndDeletesNothing() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> service.deleteByUsername(USERNAME));

        verify(traineeRepository, never()).delete(any());
        verify(traineeRepository).findByUsername(USERNAME);
    }
}
