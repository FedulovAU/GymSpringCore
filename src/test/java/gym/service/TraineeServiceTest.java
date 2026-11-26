package gym.service;

import gym.data.TraineeEntity;
import gym.data.UserEntity;
import gym.dto.request.CreateTrainee;
import gym.dto.request.UpdateTrainee;
import gym.dto.response.Trainee;
import gym.repository.TraineeRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
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
    private static final String TRAINER_USERNAME = "Anna_Lee";
    private static final String PASSWORD = "secret12345";

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private UserFactory userFactory;

    private AuthenticationContext authenticationContext;
    private TraineeService service;

    @BeforeEach
    void setUp() {
        authenticationContext = new AuthenticationContext();
        service = new TraineeService(
                traineeRepository,
                userFactory,
                new RequestValidator(),
                new GymMapper(),
                authenticationContext);
    }

    private static UserEntity user() {
        return new UserEntity("John", "Smith", "secret12345", true, USERNAME, USERNAME, 0);
    }

    private static TraineeEntity trainee() {
        return new TraineeEntity(user(), BIRTH_DATE, "1 Main St");
    }

    private void signInAsTrainee() {
        authenticationContext.signIn(new AuthenticatedUser(USERNAME, UserRole.TRAINEE));
    }

    @Test
    void createTrainee_savesTraineeWithNewUser_returnsProfile() {
        when(userFactory.createUser("John", "Smith", PASSWORD)).thenReturn(user());
        when(traineeRepository.save(any())).then(returnsFirstArg());

        var request = new CreateTrainee("John", "Smith", PASSWORD, BIRTH_DATE, "1 Main St");
        Trainee result = service.createTrainee(request);

        var expectedTrainee = new Trainee(null, USERNAME, "John", "Smith", BIRTH_DATE, "1 Main St");
        assertEquals(expectedTrainee, result);
        verify(traineeRepository).save(argThat(t -> t.getUser().getUsername().equals(USERNAME)
                && t.getUser().getPassword().equals(PASSWORD)
                && t.getDateOfBirth().equals(BIRTH_DATE)
                && t.getAddress().equals("1 Main St")));
        verify(userFactory).createUser("John", "Smith", PASSWORD);
    }

    @Test
    void createTrainee_blankPassword_throwsBeforeTouchingDatabase() {
        var request = new CreateTrainee("John", "Smith", " ", BIRTH_DATE, null);
        assertThrows(IllegalArgumentException.class, () -> service.createTrainee(request));

        verifyNoInteractions(traineeRepository, userFactory);
    }

    @Test
    void createTrainee_invalidName_throwsBeforeTouchingDatabase() {
        var request = new CreateTrainee("John1", "Smith", PASSWORD, BIRTH_DATE, null);
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
        signInAsTrainee();
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee()));

        var expectedTrainee = new Trainee(null, USERNAME, "John", "Smith", BIRTH_DATE, "1 Main St");
        assertEquals(Optional.of(expectedTrainee), service.findByUsername(USERNAME));
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void findByUsername_missing_returnsEmpty() {
        signInAsTrainee();
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertTrue(service.findByUsername(USERNAME).isEmpty());
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void changePassword_setsNewPasswordOnManagedEntity() {
        signInAsTrainee();
        TraineeEntity trainee = trainee();
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        service.changePassword("newPassword1");

        assertEquals("newPassword1", trainee.getUser().getPassword());
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void changePassword_blankPassword_throwsBeforeTouchingDatabase() {
        signInAsTrainee();
        assertThrows(IllegalArgumentException.class, () -> service.changePassword(" "));

        verifyNoInteractions(traineeRepository);
    }

    @Test
    void changePassword_missingTrainee_throwsProfileNotFound() {
        signInAsTrainee();
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> service.changePassword("newPassword1"));
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void updateTrainee_partialRequest_changesProvidedFieldsAndKeepsUsername() {
        signInAsTrainee();
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee()));
        LocalDate newBirthDate = LocalDate.of(1991, 1, 1);

        var request = new UpdateTrainee(null, "Brown", newBirthDate, null);
        Trainee result = service.updateTrainee(request);

        var expectedTrainee = new Trainee(null, USERNAME, "John", "Brown", newBirthDate, "1 Main St");
        assertEquals(expectedTrainee, result);
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void updateTrainee_missingTrainee_throwsProfileNotFound() {
        signInAsTrainee();
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        var request = new UpdateTrainee("Jack", null, null, null);
        assertThrows(ProfileNotFoundException.class, () -> service.updateTrainee(request));
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void switchActive_activeUser_deactivates() {
        signInAsTrainee();
        TraineeEntity trainee = trainee();
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        boolean isActive = service.switchActive();

        assertFalse(isActive);
        assertFalse(trainee.getUser().isActive());
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void switchActive_inactiveUser_activates() {
        signInAsTrainee();
        TraineeEntity trainee = trainee();
        trainee.getUser().setActive(false);
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        boolean isActive = service.switchActive();

        assertTrue(isActive);
        assertTrue(trainee.getUser().isActive());
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void deleteProfile_exists_deletesTrainee() {
        signInAsTrainee();
        TraineeEntity trainee = trainee();
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        service.deleteProfile();

        verify(traineeRepository).delete(trainee);
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void deleteProfile_missing_throwsAndDeletesNothing() {
        signInAsTrainee();
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> service.deleteProfile());

        verify(traineeRepository, never()).delete(any());
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void changePassword_notSignedIn_throwsBeforeTouchingDatabase() {
        assertThrows(AuthenticationException.class, () -> service.changePassword("newPassword1"));

        verifyNoInteractions(traineeRepository);
    }

    @Test
    void switchActive_signedInAsTrainer_throwsBeforeTouchingDatabase() {
        authenticationContext.signIn(new AuthenticatedUser(TRAINER_USERNAME, UserRole.TRAINER));

        assertThrows(AuthenticationException.class, () -> service.switchActive());

        verifyNoInteractions(traineeRepository);
    }

    @Test
    void findByUsername_notSignedIn_throwsBeforeTouchingDatabase() {
        assertThrows(AuthenticationException.class, () -> service.findByUsername(USERNAME));

        verifyNoInteractions(traineeRepository);
    }

    @Test
    void deleteProfile_exists_signsOut() {
        signInAsTrainee();
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee()));

        service.deleteProfile();

        assertThrows(AuthenticationException.class, () -> authenticationContext.requireSignedIn());
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void signIn_correctPassword_signsInAsTrainee() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee()));

        service.signIn(USERNAME, PASSWORD);

        assertEquals(USERNAME, authenticationContext.getUserByRole(UserRole.TRAINEE));
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void signIn_wrongPassword_throwsAndStaysSignedOut() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(trainee()));

        assertThrows(AuthenticationException.class, () -> service.signIn(USERNAME, "wrong"));

        assertThrows(AuthenticationException.class, () -> authenticationContext.requireSignedIn());
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void signIn_unknownUsername_throwsAndStaysSignedOut() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> service.signIn(USERNAME, PASSWORD));

        assertThrows(AuthenticationException.class, () -> authenticationContext.requireSignedIn());
        verify(traineeRepository).findByUsername(USERNAME);
    }
}
