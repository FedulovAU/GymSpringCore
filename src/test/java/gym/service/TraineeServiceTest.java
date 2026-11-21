package gym.service;

import gym.data.entity.TraineeEntity;
import gym.data.entity.UserEntity;
import gym.data.storage.exception.ItemNotFoundException;
import gym.dto.request.CreateTrainee;
import gym.dto.request.UpdateTrainee;
import gym.dto.response.Trainee;
import gym.repository.TraineeRepository;
import gym.repository.user.UserRepository;
import gym.service.helper.PasswordGenerator;
import gym.service.mapper.GymMapper;
import gym.service.validation.RequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 5, 17);

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordGenerator passwordGenerator;

    private TraineeService service;

    @BeforeEach
    void setUp() {
        service = new TraineeService();
        service.setGymMapper(new GymMapper());
        service.setRequestValidator(new RequestValidator());
        service.setTraineeRepository(traineeRepository);
        service.setUserRepository(userRepository);
        service.setPasswordGenerator(passwordGenerator);
    }

    private static UserEntity user(long id) {
        return new UserEntity(id, "John", "Smith", "John_Smith", 0L, "secret12345", true);
    }

    private static TraineeEntity trainee(long id) {
        return new TraineeEntity(id, BIRTH_DATE);
    }

    private static Trainee traineeDto(long id) {
        return new Trainee(id, "John", "Smith", "John_Smith", BIRTH_DATE);
    }


    @Test
    void createTrainee_validRequest_addsUserThenTraineeWithUserId() {
        when(passwordGenerator.generate()).thenReturn("secret12345");
        // The user storage assigns the ID and the user name.
        when(userRepository.add(any())).thenReturn(user(1L));
        when(traineeRepository.add(any())).then(returnsFirstArg());

        Trainee result = service.createTrainee(new CreateTrainee("John", "Smith", BIRTH_DATE));

        verify(userRepository).add(argThat(u ->
                u.getId() == null
                && u.getUserName() == null
                && u.getFirstName().equals("John")
                && u.getLastName().equals("Smith")
                && u.getPassword().equals("secret12345")
                && u.isActive()));
        verify(traineeRepository).add(argThat(t -> t.getId() == 1L && t.getBirthDate().equals(BIRTH_DATE)));
        assertEquals(traineeDto(1L), result);
    }

    @Test
    void createTrainee_invalidFirstName_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrainee(new CreateTrainee("John1", "Smith", BIRTH_DATE)));

        verifyNoInteractions(traineeRepository, userRepository, passwordGenerator);
    }


    @Test
    void updateTrainee_partialRequest_updatesUserAndTraineeFields() {
        LocalDate newBirthDate = LocalDate.of(1991, 1, 1);

        when(traineeRepository.update(eq(1L), any())).thenAnswer(inv ->{
            return inv.getArgument(1, UnaryOperator.class).apply(trainee(1L));
        });
        when(userRepository.update(eq(1L), any())).thenAnswer(inv -> {
            return inv.getArgument(1, UnaryOperator.class).apply(user(1L));
        });

        Trainee result = service.updateTrainee(new UpdateTrainee(1L, null, "Brown", newBirthDate));

        verify(traineeRepository).update(eq(1L), any());
        verify(userRepository).update(eq(1L), any());
        // The user name stays the same when the last name changes.
        assertEquals(new Trainee(1L, "John", "Brown", "John_Smith", newBirthDate), result);
    }

    @Test
    void updateTrainee_futureBirthDate_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTrainee(new UpdateTrainee(1L, null, null, LocalDate.now().plusDays(1))));

        verifyNoInteractions(traineeRepository, userRepository);
    }

    @Test
    void updateTrainee_traineeNotFound_propagatesAndLeavesUserUntouched() {
        when(traineeRepository.update(eq(1L), any())).thenThrow(new ItemNotFoundException("TraineeEntity", "1"));

        assertThrows(ItemNotFoundException.class,
                () -> service.updateTrainee(new UpdateTrainee(1L, "Jack", null, null)));

        verifyNoInteractions(userRepository);
    }


    @Test
    void deleteTraineeById_deletesTraineeAndItsUser() {
        service.deleteTraineeById(1L);

        verify(traineeRepository).deleteById(1L);
        verify(userRepository).deleteById(1L);
    }


    @Test
    void findTraineeById_traineeAndUserExist_returnsTrainee() {
        when(userRepository.getById(1L)).thenReturn(Optional.of(user(1L)));
        when(traineeRepository.getById(1L)).thenReturn(Optional.of(trainee(1L)));

        assertEquals(Optional.of(traineeDto(1L)), service.findTraineeById(1L));
    }

    @Test
    void findTraineeById_userIsNotTrainee_returnsEmpty() {
        // For example, the ID of a trainer: the user exists, the trainee does not.
        when(userRepository.getById(1L)).thenReturn(Optional.of(user(1L)));
        when(traineeRepository.getById(1L)).thenReturn(Optional.empty());

        assertTrue(service.findTraineeById(1L).isEmpty());
    }

    @Test
    void findTraineeById_userMissing_returnsEmpty() {
        when(userRepository.getById(1L)).thenReturn(Optional.empty());

        assertTrue(service.findTraineeById(1L).isEmpty());
        verifyNoInteractions(traineeRepository);
    }


    @Test
    void getAll_joinsTraineesWithUsers_skipsTraineesWithoutUser() {
        when(traineeRepository.getAll()).thenReturn(List.of(trainee(1L), trainee(2L)));
        when(userRepository.getById(1L)).thenReturn(Optional.of(user(1L)));
        when(userRepository.getById(2L)).thenReturn(Optional.empty());

        assertEquals(List.of(traineeDto(1L)), service.getAll());
    }
}
