package gym.services;

import gym.data.entities.TrainerEntity;
import gym.data.entities.common.UserData;
import gym.data.entities.enums.TrainingTypeEntity;
import gym.data.storage.exceptions.ItemNotFoundException;
import gym.repositories.TrainerRepository;
import gym.services.dto.enums.TrainingType;
import gym.services.dto.requests.CreateTrainer;
import gym.services.dto.requests.UpdateTrainer;
import gym.services.dto.responses.Trainer;
import gym.services.helpers.PasswordGenerator;
import gym.services.helpers.UniqueUsernameGenerator;
import gym.services.mapper.EntityMapper;
import gym.services.synchronization.TrainerLocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private UniqueUsernameGenerator userNameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;

    private TrainerService service;

    @BeforeEach
    void setUp() {
        service = new TrainerService();
        service.setTrainerRepository(trainerRepository);
        service.setUserNameGenerator(userNameGenerator);
        service.setPasswordGenerator(passwordGenerator);
        service.setTrainerLocks(new TrainerLocks());
    }

    private static TrainerEntity trainerEntity(long id, boolean isActive) {
        return new TrainerEntity(
                id,
                new UserData("Anna", "Lee", "Anna_Lee", "secret12345", isActive),
                TrainingTypeEntity.Crossfit);
    }

    /** Makes the mocked update apply the service's updater to the given entity, like the real storage does. */
    private void stubUpdateAppliesUpdaterTo(TrainerEntity existing) {
        when(trainerRepository.update(eq(existing.id()), any())).thenAnswer(invocation -> {
            UnaryOperator<TrainerEntity> updater = invocation.getArgument(1);
            return updater.apply(existing);
        });
    }

    // ---------- createTrainer ----------

    @Test
    void createTrainer_validRequest_returnsCreatedTrainer() {
        when(userNameGenerator.generateUniqueUserName("Anna", "Lee")).thenReturn("Anna_Lee");
        when(passwordGenerator.generate()).thenReturn("secret12345");
        when(trainerRepository.add(any())).thenAnswer(invocation -> {
            TrainerEntity toAdd = invocation.getArgument(0);
            return toAdd.withId(1L);
        });

        Trainer result = service.createTrainer(new CreateTrainer("Anna", "Lee", TrainingType.Crossfit));

        ArgumentCaptor<TrainerEntity> captor = ArgumentCaptor.forClass(TrainerEntity.class);
        verify(trainerRepository).add(captor.capture());
        TrainerEntity added = captor.getValue();
        assertNull(added.id());
        assertEquals("Anna_Lee", added.user().userName());
        assertEquals("secret12345", added.user().password());
        assertTrue(added.user().isActive());
        assertEquals(TrainingTypeEntity.Crossfit, added.specialization());

        assertEquals(new Trainer(1L, "Anna", "Lee", "Anna_Lee", true, TrainingType.Crossfit), result);
    }

    @Test
    void createTrainer_nullSpecialization_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createTrainer(new CreateTrainer("Anna", "Lee", null)));

        verifyNoInteractions(trainerRepository, userNameGenerator, passwordGenerator);
    }

    // ---------- updateTrainer ----------

    @Test
    void updateTrainer_partialRequest_updatesOnlyProvidedFields() {
        stubUpdateAppliesUpdaterTo(trainerEntity(1L, true));

        Trainer result = service.updateTrainer(new UpdateTrainer(1L, null, null, TrainingType.Fitness));

        assertEquals(new Trainer(1L, "Anna", "Lee", "Anna_Lee", true, TrainingType.Fitness), result);
    }

    @Test
    void updateTrainer_nonPositiveId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTrainer(new UpdateTrainer(0L, "Anna", null, null)));

        verifyNoInteractions(trainerRepository);
    }

    @Test
    void updateTrainer_trainerNotFound_propagatesItemNotFound() {
        when(trainerRepository.update(eq(1L), any())).thenThrow(new ItemNotFoundException(TrainerEntity.class, 1L));

        assertThrows(ItemNotFoundException.class,
                () -> service.updateTrainer(new UpdateTrainer(1L, "Anna", null, null)));
    }

    // ---------- deactivateTrainer ----------

    @Test
    void deactivateTrainer_existing_returnsInactiveTrainer() {
        stubUpdateAppliesUpdaterTo(trainerEntity(1L, true));

        Trainer result = service.deactivateTrainer(1L);

        assertFalse(result.isActive());
        assertEquals(1L, result.id());
    }

    @Test
    void deactivateTrainer_trainerNotFound_propagatesItemNotFound() {
        when(trainerRepository.update(eq(1L), any())).thenThrow(new ItemNotFoundException(TrainerEntity.class, 1L));

        assertThrows(ItemNotFoundException.class, () -> service.deactivateTrainer(1L));
    }

    // ---------- findTrainerById ----------

    @Test
    void findTrainerById_existing_returnsTrainer() {
        when(trainerRepository.getById(1L)).thenReturn(Optional.of(trainerEntity(1L, true)));

        Optional<Trainer> result = service.findTrainerById(1L);

        assertEquals(Optional.of(new Trainer(1L, "Anna", "Lee", "Anna_Lee", true, TrainingType.Crossfit)), result);
    }

    @Test
    void findTrainerById_missing_returnsEmpty() {
        when(trainerRepository.getById(1L)).thenReturn(Optional.empty());

        assertTrue(service.findTrainerById(1L).isEmpty());
    }

    // ---------- getAll ----------

    @Test
    void getAll_returnsAllTrainers() {
        var storedTrainers = List.of(
                trainerEntity(1L, true),
                trainerEntity(2L, false));

        when(trainerRepository.getAll()).thenReturn(storedTrainers);

        List<Trainer> result = service.getAll();

        assertEquals(storedTrainers.size(), result.size());
        assertEquals(
                storedTrainers.stream().map(EntityMapper::toTrainer).collect(Collectors.toSet()),
                Set.copyOf(result));
    }
}
