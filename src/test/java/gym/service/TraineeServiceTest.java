package gym.service;

import gym.data.entity.TraineeEntity;
import gym.data.entity.UserEntity;
import gym.data.storage.exception.ItemNotFoundException;
import gym.dto.request.CreateTrainee;
import gym.dto.request.UpdateTrainee;
import gym.dto.response.Trainee;
import gym.repository.trainee.TraineeRepository;
import gym.repository.trainer.TrainerRepository;
import gym.service.helper.PasswordGenerator;
import gym.service.helper.UsernameGenerator;
import gym.service.mapper.GymMapper;
import gym.service.validation.RequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 5, 17);

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private PasswordGenerator passwordGenerator;

    private TraineeService service;

    @BeforeEach
    void setUp() {
        service = new TraineeService();
        service.setGymMapper(new GymMapper());
        service.setRequestValidator(new RequestValidator());
        service.setTraineeRepository(traineeRepository);
        service.setTrainerRepository(trainerRepository);
        service.setPasswordGenerator(passwordGenerator);
        service.setUsernameGenerator(new UsernameGenerator());
    }

    private static TraineeEntity trainee(long id) {
        return new TraineeEntity(id, new UserEntity("John", "Smith", "John_Smith", 0L, "secret12345", true), BIRTH_DATE);
    }

    private static Trainee traineeDto(long id) {
        return new Trainee(id, "John", "Smith", "John_Smith", BIRTH_DATE);
    }

    private void stubCreate(Optional<Long> traineeSuffix, Optional<Long> trainerSuffix, long assignedId) {
        when(passwordGenerator.generate()).thenReturn("secret12345");
        when(traineeRepository.getLastUsernameSuffix("John", "Smith")).thenReturn(traineeSuffix);
        when(trainerRepository.getLastUsernameSuffix("John", "Smith")).thenReturn(trainerSuffix);
        when(traineeRepository.add(any())).thenAnswer(inv -> inv.getArgument(0, TraineeEntity.class).withId(assignedId));
    }

    private void verifyCreateLookups() {
        verify(passwordGenerator).generate();
        verify(traineeRepository).getLastUsernameSuffix("John", "Smith");
        verify(trainerRepository).getLastUsernameSuffix("John", "Smith");
    }


    @Test
    void createTrainee_newName_addsTraineeWithBaseUsername() {
        stubCreate(Optional.empty(), Optional.empty(), 1L);

        Trainee result = service.createTrainee(new CreateTrainee("John", "Smith", BIRTH_DATE));

        verifyCreateLookups();
        verify(traineeRepository).add(argThat(t ->
                t.getId() == null
                && t.getBirthDate().equals(BIRTH_DATE)
                && t.getUser().getUserName().equals("John_Smith")
                && t.getUser().getUserNameSuffix() == 0L
                && t.getUser().getFirstName().equals("John")
                && t.getUser().getLastName().equals("Smith")
                && t.getUser().getPassword().equals("secret12345")
                && t.getUser().isActive()));
        assertEquals(traineeDto(1L), result);
    }

    @Test
    void createTrainee_traineeSuffixIsHigher_usesNextAfterIt() {
        stubCreate(Optional.of(2L), Optional.of(0L), 3L);

        Trainee result = service.createTrainee(new CreateTrainee("John", "Smith", BIRTH_DATE));

        verifyCreateLookups();
        verify(traineeRepository).add(argThat(t -> t.getUser().getUserName().equals("John_Smith_3")
                && t.getUser().getUserNameSuffix() == 3L));
        assertEquals(new Trainee(3L, "John", "Smith", "John_Smith_3", BIRTH_DATE), result);
    }

    @Test
    void createTrainee_trainerSuffixIsHigher_usesNextAfterIt() {
        stubCreate(Optional.of(0L), Optional.of(2L), 3L);

        Trainee result = service.createTrainee(new CreateTrainee("John", "Smith", BIRTH_DATE));

        verifyCreateLookups();
        verify(traineeRepository).add(argThat(t -> t.getUser().getUserName().equals("John_Smith_3")
                && t.getUser().getUserNameSuffix() == 3L));
        assertEquals(new Trainee(3L, "John", "Smith", "John_Smith_3", BIRTH_DATE), result);
    }

    @Test
    void createTrainee_invalidFirstName_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrainee(new CreateTrainee("John1", "Smith", BIRTH_DATE)));

        verifyNoInteractions(traineeRepository, trainerRepository, passwordGenerator);
    }


    @Test
    void updateTrainee_partialRequest_updatesUserAndTraineeFields() {
        LocalDate newBirthDate = LocalDate.of(1991, 1, 1);
        when(traineeRepository.update(eq(1L), any()))
                .thenAnswer(inv -> inv.getArgument(1, UnaryOperator.class).apply(trainee(1L)));

        Trainee result = service.updateTrainee(new UpdateTrainee(1L, null, "Brown", newBirthDate));

        verify(traineeRepository).update(eq(1L), any());
        // The user name stays the same when the last name changes.
        assertEquals(new Trainee(1L, "John", "Brown", "John_Smith", newBirthDate), result);
    }

    @Test
    void updateTrainee_futureBirthDate_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTrainee(new UpdateTrainee(1L, null, null, LocalDate.now().plusDays(1))));

        verifyNoInteractions(traineeRepository);
    }

    @Test
    void updateTrainee_traineeNotFound_propagates() {
        when(traineeRepository.update(eq(1L), any())).thenThrow(new ItemNotFoundException("TraineeEntity", "1"));

        assertThrows(ItemNotFoundException.class,
                () -> service.updateTrainee(new UpdateTrainee(1L, "Jack", null, null)));

        verify(traineeRepository).update(eq(1L), any());
    }


    @Test
    void deleteTraineeById_deletesTrainee() {
        service.deleteTraineeById(1L);

        verify(traineeRepository).deleteById(1L);
    }


    @Test
    void findTraineeById_exists_returnsTrainee() {
        when(traineeRepository.getById(1L)).thenReturn(Optional.of(trainee(1L)));

        assertEquals(Optional.of(traineeDto(1L)), service.findTraineeById(1L));
        verify(traineeRepository).getById(1L);
    }

    @Test
    void findTraineeById_missing_returnsEmpty() {
        when(traineeRepository.getById(1L)).thenReturn(Optional.empty());

        assertTrue(service.findTraineeById(1L).isEmpty());
        verify(traineeRepository).getById(1L);
    }


    @Test
    void getAll_mapsAllTrainees() {
        when(traineeRepository.getAll()).thenReturn(List.of(trainee(1L), trainee(2L)));

        assertEquals(List.of(traineeDto(1L), traineeDto(2L)), service.getAll());
        verify(traineeRepository).getAll();
    }
}
