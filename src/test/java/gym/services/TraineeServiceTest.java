package gym.services;

import gym.data.entities.TraineeEntity;
import gym.data.entities.TrainingEntity;
import gym.data.entities.common.UserData;
import gym.data.entities.enums.TrainingTypeEntity;
import gym.data.storage.exceptions.ItemNotFoundException;
import gym.repositories.TraineeRepository;
import gym.repositories.TrainingRepository;
import gym.services.dto.requests.CreateTrainee;
import gym.services.dto.requests.UpdateTrainee;
import gym.services.dto.responses.Trainee;
import gym.services.helpers.PasswordGenerator;
import gym.services.helpers.UniqueUsernameGenerator;
import gym.services.mapper.EntityMapper;
import gym.services.synchronization.TraineeLocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 5, 17);

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private UniqueUsernameGenerator userNameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;

    private TraineeService service;

    @BeforeEach
    void setUp() {
        service = new TraineeService();
        service.setTrainingRepository(trainingRepository);
        service.setTraineeRepository(traineeRepository);
        service.setUserNameGenerator(userNameGenerator);
        service.setPasswordGenerator(passwordGenerator);
        service.setTraineeLocks(new TraineeLocks());
    }

    private static TraineeEntity traineeEntity(long id, boolean isActive) {
        return new TraineeEntity(
                id,
                new UserData("John", "Smith", "John_Smith", "secret12345", isActive),
                BIRTH_DATE);
    }

    private static TrainingEntity trainingEntity(long id, long traineeId) {
        return new TrainingEntity(
                id, traineeId, 7L, "Morning", TrainingTypeEntity.Fitness,
                LocalDateTime.of(2026, 12, 1, 9, 0), Duration.ofHours(1));
    }

    /** Makes the mocked update apply the service's updater to the given entity, like the real storage does. */
    private void stubUpdateAppliesUpdaterTo(TraineeEntity existing) {
        when(traineeRepository.update(eq(existing.id()), any())).thenAnswer(invocation -> {
            UnaryOperator<TraineeEntity> updater = invocation.getArgument(1);
            return updater.apply(existing);
        });
    }

    // ---------- createTrainee ----------

    @Test
    void createTrainee_validRequest_returnsCreatedTrainee() {
        when(userNameGenerator.generateUniqueUserName("John", "Smith")).thenReturn("John_Smith");
        when(passwordGenerator.generate()).thenReturn("secret12345");
        when(traineeRepository.add(any())).thenAnswer(invocation -> {
            TraineeEntity toAdd = invocation.getArgument(0);
            return toAdd.withId(1L);
        });

        Trainee result = service.createTrainee(new CreateTrainee("John", "Smith", BIRTH_DATE));

        ArgumentCaptor<TraineeEntity> captor = ArgumentCaptor.forClass(TraineeEntity.class);
        verify(traineeRepository).add(captor.capture());
        TraineeEntity added = captor.getValue();
        assertNull(added.id());
        assertEquals("John_Smith", added.user().userName());
        assertEquals("secret12345", added.user().password());
        assertTrue(added.user().isActive());

        assertEquals(new Trainee(1L, "John", "Smith", "John_Smith", true, BIRTH_DATE), result);
    }

    @Test
    void createTrainee_invalidFirstName_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrainee(new CreateTrainee("John1", "Smith", BIRTH_DATE)));

        verifyNoInteractions(traineeRepository, userNameGenerator, passwordGenerator);
    }

    // ---------- updateTrainee ----------

    @Test
    void updateTrainee_partialRequest_updatesOnlyProvidedFields() {
        stubUpdateAppliesUpdaterTo(traineeEntity(1L, true));

        Trainee result = service.updateTrainee(new UpdateTrainee(1L, null, "Brown", null));

        assertEquals(new Trainee(1L, "John", "Brown", "John_Smith", true, BIRTH_DATE), result);
    }

    @Test
    void updateTrainee_futureBirthDate_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTrainee(new UpdateTrainee(1L, null, null, LocalDate.now().plusDays(1))));

        verifyNoInteractions(traineeRepository);
    }

    @Test
    void updateTrainee_traineeNotFound_propagatesItemNotFound() {
        when(traineeRepository.update(eq(1L), any())).thenThrow(new ItemNotFoundException(TraineeEntity.class, 1L));

        assertThrows(ItemNotFoundException.class,
                () -> service.updateTrainee(new UpdateTrainee(1L, "Jack", null, null)));
    }

    // ---------- deactivateTrainee ----------

    @Test
    void deactivateTrainee_existing_returnsInactiveTrainee() {
        stubUpdateAppliesUpdaterTo(traineeEntity(1L, true));

        Trainee result = service.deactivateTrainee(1L);

        assertFalse(result.isActive());
        assertEquals(1L, result.id());
    }

    @Test
    void deactivateTrainee_traineeNotFound_propagatesItemNotFound() {
        when(traineeRepository.update(eq(1L), any())).thenThrow(new ItemNotFoundException(TraineeEntity.class, 1L));

        assertThrows(ItemNotFoundException.class, () -> service.deactivateTrainee(1L));
    }

    // ---------- deleteTrainee ----------

    @Test
    void deleteTrainee_withTrainings_deletesTrainingsAndTrainee() {
        TraineeEntity trainee = traineeEntity(1L, true);
        when(trainingRepository.getByTraineeId(1L)).thenReturn(List.of(trainingEntity(10L, 1L), trainingEntity(11L, 1L)));
        when(traineeRepository.delete(1L)).thenReturn(trainee);

        Trainee result = service.deleteTrainee(1L);

        verify(trainingRepository).deleteById(10L);
        verify(trainingRepository).deleteById(11L);
        verify(traineeRepository).delete(1L);
        assertEquals(1L, result.id());
    }

    @Test
    void deleteTrainee_traineeNotFound_propagatesItemNotFound() {
        when(trainingRepository.getByTraineeId(1L)).thenReturn(List.of());
        when(traineeRepository.delete(1L)).thenThrow(new ItemNotFoundException(TraineeEntity.class, 1L));

        assertThrows(ItemNotFoundException.class, () -> service.deleteTrainee(1L));

        verify(trainingRepository, never()).deleteById(anyLong());
    }

    // ---------- findTraineeById ----------

    @Test
    void findTraineeById_existing_returnsTrainee() {
        when(traineeRepository.getById(1L)).thenReturn(Optional.of(traineeEntity(1L, true)));

        Optional<Trainee> result = service.findTraineeById(1L);

        assertEquals(Optional.of(new Trainee(1L, "John", "Smith", "John_Smith", true, BIRTH_DATE)), result);
    }

    @Test
    void findTraineeById_missing_returnsEmpty() {
        when(traineeRepository.getById(1L)).thenReturn(Optional.empty());

        assertTrue(service.findTraineeById(1L).isEmpty());
    }

    // ---------- getAll ----------

    @Test
    void getAll_returnsAllTrainees() {
        var storedTrainees = List.of(
                traineeEntity(1L, true),
                traineeEntity(2L, false));

        when(traineeRepository.getAll()).thenReturn(storedTrainees);

        List<Trainee> result = service.getAll();

        assertEquals(storedTrainees.size(), result.size());
        assertEquals(
                storedTrainees.stream().map(EntityMapper::toTrainee).collect(Collectors.toSet()),
                Set.copyOf(result));
    }
}
