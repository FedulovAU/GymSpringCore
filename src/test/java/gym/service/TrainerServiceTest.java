package gym.service;

import gym.data.entity.TrainerEntity;
import gym.data.entity.UserEntity;
import gym.data.entity.TrainingTypeEntity;
import gym.data.storage.exception.ItemNotFoundException;
import gym.dto.enums.TrainingType;
import gym.dto.request.CreateTrainer;
import gym.dto.request.UpdateTrainer;
import gym.dto.response.Trainer;
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

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private PasswordGenerator passwordGenerator;

    private TrainerService service;

    @BeforeEach
    void setUp() {
        service = new TrainerService();
        service.setGymMapper(new GymMapper());
        service.setRequestValidator(new RequestValidator());
        service.setTrainerRepository(trainerRepository);
        service.setTraineeRepository(traineeRepository);
        service.setPasswordGenerator(passwordGenerator);
        service.setUsernameGenerator(new UsernameGenerator());
    }

    private static TrainerEntity trainer(long id) {
        return new TrainerEntity(
                id,
                new UserEntity("Anna", "Lee", "Anna_Lee", 0L, "secret12345", true),
                TrainingTypeEntity.of(TrainingTypeEntity.TrainingType.CROSSFIT));
    }

    private static Trainer trainerDto(long id) {
        return new Trainer(id, "Anna", "Lee", "Anna_Lee", TrainingType.CROSSFIT);
    }

    private static CreateTrainer createRequest() {
        return new CreateTrainer("Anna", "Lee", TrainingType.CROSSFIT);
    }

    private void stubCreate(Optional<Long> traineeSuffix, Optional<Long> trainerSuffix, long assignedId) {
        when(passwordGenerator.generate()).thenReturn("secret12345");
        when(traineeRepository.getLastUsernameSuffix("Anna", "Lee")).thenReturn(traineeSuffix);
        when(trainerRepository.getLastUsernameSuffix("Anna", "Lee")).thenReturn(trainerSuffix);
        when(trainerRepository.add(any())).thenAnswer(inv -> inv.getArgument(0, TrainerEntity.class).withId(assignedId));
    }

    private void verifyCreateLookups() {
        verify(passwordGenerator).generate();
        verify(traineeRepository).getLastUsernameSuffix("Anna", "Lee");
        verify(trainerRepository).getLastUsernameSuffix("Anna", "Lee");
    }


    @Test
    void createTrainer_newName_addsTrainerWithBaseUsername() {
        stubCreate(Optional.empty(), Optional.empty(), 1L);

        Trainer result = service.createTrainer(createRequest());

        verifyCreateLookups();
        verify(trainerRepository).add(argThat(t ->
                t.getId() == null
                && t.getSpecialization().getTrainingType() == TrainingTypeEntity.TrainingType.CROSSFIT
                && t.getUser().getUserName().equals("Anna_Lee")
                && t.getUser().getUserNameSuffix() == 0L
                && t.getUser().getFirstName().equals("Anna")
                && t.getUser().getLastName().equals("Lee")
                && t.getUser().getPassword().equals("secret12345")
                && t.getUser().isActive()));
        assertEquals(trainerDto(1L), result);
    }

    @Test
    void createTrainer_traineeSuffixIsHigher_usesNextAfterIt() {
        stubCreate(Optional.of(2L), Optional.of(0L), 3L);

        Trainer result = service.createTrainer(createRequest());

        verifyCreateLookups();
        verify(trainerRepository).add(argThat(t -> t.getUser().getUserName().equals("Anna_Lee_3")
                && t.getUser().getUserNameSuffix() == 3L));
        assertEquals(new Trainer(3L, "Anna", "Lee", "Anna_Lee_3", TrainingType.CROSSFIT), result);
    }

    @Test
    void createTrainer_trainerSuffixIsHigher_usesNextAfterIt() {
        stubCreate(Optional.of(0L), Optional.of(2L), 3L);

        Trainer result = service.createTrainer(createRequest());

        verifyCreateLookups();
        verify(trainerRepository).add(argThat(t -> t.getUser().getUserName().equals("Anna_Lee_3")
                && t.getUser().getUserNameSuffix() == 3L));
        assertEquals(new Trainer(3L, "Anna", "Lee", "Anna_Lee_3", TrainingType.CROSSFIT), result);
    }

    @Test
    void createTrainer_invalidLastName_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrainer(new CreateTrainer("Anna", "Lee1", TrainingType.CROSSFIT)));

        verifyNoInteractions(trainerRepository, traineeRepository, passwordGenerator);
    }


    @Test
    void updateTrainer_partialRequest_updatesUserAndTrainerFields() {
        when(trainerRepository.update(eq(1L), any()))
                .thenAnswer(inv -> inv.getArgument(1, UnaryOperator.class).apply(trainer(1L)));

        Trainer result = service.updateTrainer(
                new UpdateTrainer(1L, "Hanna", null, TrainingType.FITNESS));

        verify(trainerRepository).update(eq(1L), any());
        // The user name stays the same when the first name changes.
        assertEquals(new Trainer(1L, "Hanna", "Lee", "Anna_Lee", TrainingType.FITNESS), result);
    }

    @Test
    void updateTrainer_invalidFirstName_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTrainer(new UpdateTrainer(1L, "Anna1", null, null)));

        verifyNoInteractions(trainerRepository);
    }

    @Test
    void updateTrainer_trainerNotFound_propagates() {
        when(trainerRepository.update(eq(1L), any())).thenThrow(new ItemNotFoundException("TrainerEntity", "1"));

        assertThrows(ItemNotFoundException.class,
                () -> service.updateTrainer(new UpdateTrainer(1L, "Anna", null, null)));

        verify(trainerRepository).update(eq(1L), any());
    }


    @Test
    void findTrainerById_exists_returnsTrainer() {
        when(trainerRepository.getById(1L)).thenReturn(Optional.of(trainer(1L)));

        assertEquals(Optional.of(trainerDto(1L)), service.findTrainerById(1L));
        verify(trainerRepository).getById(1L);
    }

    @Test
    void findTrainerById_missing_returnsEmpty() {
        when(trainerRepository.getById(1L)).thenReturn(Optional.empty());

        assertTrue(service.findTrainerById(1L).isEmpty());
        verify(trainerRepository).getById(1L);
    }


    @Test
    void getAll_mapsAllTrainers() {
        when(trainerRepository.getAll()).thenReturn(List.of(trainer(1L), trainer(2L)));

        assertEquals(List.of(trainerDto(1L), trainerDto(2L)), service.getAll());
        verify(trainerRepository).getAll();
    }
}
