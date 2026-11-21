package gym.service;

import gym.data.entity.TrainerEntity;
import gym.data.entity.UserEntity;
import gym.data.enums.TrainingType;
import gym.data.storage.exception.ItemNotFoundException;
import gym.dto.request.CreateTrainer;
import gym.dto.request.UpdateTrainer;
import gym.dto.response.Trainer;
import gym.repository.TrainerRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordGenerator passwordGenerator;

    private TrainerService service;

    @BeforeEach
    void setUp() {
        service = new TrainerService();
        service.setGymMapper(new GymMapper());
        service.setRequestValidator(new RequestValidator());
        service.setTrainerRepository(trainerRepository);
        service.setUserRepository(userRepository);
        service.setPasswordGenerator(passwordGenerator);
    }

    private static UserEntity user(long id) {
        return new UserEntity(id, "Anna", "Lee", "Anna_Lee", 0L, "secret12345", true);
    }

    private static TrainerEntity trainer(long id) {
        return new TrainerEntity(id, TrainingType.CROSSFIT);
    }

    private static Trainer trainerDto(long id) {
        return new Trainer(id, "Anna", "Lee", "Anna_Lee", gym.dto.enums.TrainingType.CROSSFIT);
    }


    @Test
    void createTrainer_validRequest_addsUserThenTrainerWithUserId() {
        when(passwordGenerator.generate()).thenReturn("secret12345");
        // The user storage assigns the ID and the user name.
        when(userRepository.add(any())).thenReturn(user(1L));
        when(trainerRepository.add(any())).then(returnsFirstArg());

        Trainer result = service.createTrainer(new CreateTrainer("Anna", "Lee", gym.dto.enums.TrainingType.CROSSFIT));

        verify(userRepository).add(argThat(u ->
                u.getId() == null
                && u.getUserName() == null
                && u.getFirstName().equals("Anna")
                && u.getLastName().equals("Lee")
                && u.getPassword().equals("secret12345")
                && u.isActive()));
        verify(trainerRepository).add(argThat(t -> t.getId() == 1L && t.getSpecialization() == TrainingType.CROSSFIT));
        assertEquals(trainerDto(1L), result);
    }

    @Test
    void createTrainer_invalidLastName_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrainer(new CreateTrainer("Anna", "Lee1", gym.dto.enums.TrainingType.CROSSFIT)));

        verifyNoInteractions(trainerRepository, userRepository, passwordGenerator);
    }


    @Test
    void updateTrainer_partialRequest_updatesUserAndTrainerFields() {
        when(trainerRepository.update(eq(1L), any())).thenAnswer(inv ->{
            return inv.getArgument(1, UnaryOperator.class).apply(trainer(1L));
        });
        when(userRepository.update(eq(1L), any())).thenAnswer(inv -> {
            return inv.getArgument(1, UnaryOperator.class).apply(user(1L));
        });

        Trainer result = service.updateTrainer(
                new UpdateTrainer(1L, "Hanna", null, gym.dto.enums.TrainingType.FITNESS));

        verify(trainerRepository).update(eq(1L), any());
        verify(userRepository).update(eq(1L), any());
        // The user name stays the same when the first name changes.
        assertEquals(new Trainer(1L, "Hanna", "Lee", "Anna_Lee", gym.dto.enums.TrainingType.FITNESS), result);
    }

    @Test
    void updateTrainer_invalidFirstName_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTrainer(new UpdateTrainer(1L, "Anna1", null, null)));

        verifyNoInteractions(trainerRepository, userRepository);
    }

    @Test
    void updateTrainer_trainerNotFound_propagatesAndLeavesUserUntouched() {
        when(trainerRepository.update(eq(1L), any())).thenThrow(new ItemNotFoundException("TrainerEntity", "1"));

        assertThrows(ItemNotFoundException.class,
                () -> service.updateTrainer(new UpdateTrainer(1L, "Anna", null, null)));

        verifyNoInteractions(userRepository);
    }


    @Test
    void findTrainerById_trainerAndUserExist_returnsTrainer() {
        when(userRepository.getById(1L)).thenReturn(Optional.of(user(1L)));
        when(trainerRepository.getById(1L)).thenReturn(Optional.of(trainer(1L)));

        assertEquals(Optional.of(trainerDto(1L)), service.findTrainerById(1L));
    }

    @Test
    void findTrainerById_userIsNotTrainer_returnsEmpty() {
        // For example, the ID of a trainee: the user exists, the trainer does not.
        when(userRepository.getById(1L)).thenReturn(Optional.of(user(1L)));
        when(trainerRepository.getById(1L)).thenReturn(Optional.empty());

        assertTrue(service.findTrainerById(1L).isEmpty());
    }


    @Test
    void getAll_joinsTrainersWithUsers_skipsTrainersWithoutUser() {
        when(trainerRepository.getAll()).thenReturn(List.of(trainer(1L), trainer(2L)));
        when(userRepository.getById(1L)).thenReturn(Optional.of(user(1L)));
        when(userRepository.getById(2L)).thenReturn(Optional.empty());

        assertEquals(List.of(trainerDto(1L)), service.getAll());
    }
}
