package gym.services;

import gym.data.entities.TraineeEntity;
import gym.data.entities.TrainerEntity;
import gym.data.entities.TrainingEntity;
import gym.data.common.UserData;
import gym.data.entities.enums.TrainingTypeEntity;
import gym.data.storage.exceptions.ItemNotFoundException;
import gym.repositories.TraineeRepository;
import gym.repositories.TrainerRepository;
import gym.repositories.TrainingRepository;
import gym.services.dto.enums.TrainingType;
import gym.services.dto.requests.ScheduleTraining;
import gym.services.dto.responses.Training;
import gym.services.exceptions.CannotScheduleTrainingException;
import gym.services.mapper.EntityMapper;
import gym.services.synchronization.TraineeLocks;
import gym.services.synchronization.TrainerLocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    private static final long TRAINEE_ID = 1L;
    private static final long TRAINER_ID = 2L;
    private static final LocalDateTime START = LocalDateTime.of(2026, 12, 1, 10, 0);
    private static final Duration ONE_HOUR = Duration.ofHours(1);

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TraineeRepository traineeRepository;

    private TrainingService service;

    @BeforeEach
    void setUp() {
        service = new TrainingService();
        service.setTrainingRepository(trainingRepository);
        service.setTrainerRepository(trainerRepository);
        service.setTraineeRepository(traineeRepository);
        service.setTrainerLocks(new TrainerLocks());
        service.setTraineeLocks(new TraineeLocks());
    }

    private static TrainerEntity trainerEntity(boolean isActive) {
        return new TrainerEntity(
                TRAINER_ID,
                new UserData("Anna", "Lee", "Anna_Lee", "secret12345", isActive),
                TrainingTypeEntity.Crossfit);
    }

    private static TraineeEntity traineeEntity(boolean isActive) {
        return new TraineeEntity(
                TRAINEE_ID,
                new UserData("John", "Smith", "John_Smith", "secret12345", isActive),
                LocalDate.of(1990, 5, 17));
    }

    private static TrainingEntity trainingEntity(long id, LocalDateTime start, Duration duration) {
        return new TrainingEntity(id, TRAINEE_ID, TRAINER_ID, "Existing", TrainingTypeEntity.Crossfit, start, duration);
    }

    private static ScheduleTraining request(LocalDateTime start, Duration duration) {
        return new ScheduleTraining(TRAINEE_ID, TRAINER_ID, "Morning", start, duration);
    }

    private void stubActiveTrainerAndTrainee() {
        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.of(trainerEntity(true)));
        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.of(traineeEntity(true)));
    }

    private void stubAddAssignsId(long id) {
        when(trainingRepository.add(any())).thenAnswer(invocation -> {
            TrainingEntity toAdd = invocation.getArgument(0);
            return toAdd.withId(id);
        });
    }


    @Test
    void scheduleTraining_noConflicts_returnsScheduledTraining() {
        stubActiveTrainerAndTrainee();
        when(trainingRepository.getByTrainerId(TRAINER_ID)).thenReturn(List.of());
        when(trainingRepository.getByTraineeId(TRAINEE_ID)).thenReturn(List.of());
        stubAddAssignsId(100L);

        Training result = service.scheduleTraining(request(START, ONE_HOUR));

        assertEquals(new Training(100L, TRAINEE_ID, TRAINER_ID, "Morning", TrainingType.Crossfit, START, ONE_HOUR), result);
    }

    @Test
    void scheduleTraining_zeroDuration_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.scheduleTraining(request(START, Duration.ZERO)));

        verifyNoInteractions(trainingRepository, trainerRepository, traineeRepository);
    }

    @Test
    void scheduleTraining_trainerScheduleOverlaps_throwsCannotScheduleTraining() {
        stubActiveTrainerAndTrainee();
        // Existing 10:30 - 11:30 overlaps the requested 10:00 - 11:00.
        when(trainingRepository.getByTrainerId(TRAINER_ID))
                .thenReturn(List.of(trainingEntity(50L, START.plusMinutes(30), ONE_HOUR)));

        assertThrows(CannotScheduleTrainingException.class,
                () -> service.scheduleTraining(request(START, ONE_HOUR)));

        verify(trainingRepository, never()).add(any());
    }

    @Test
    void scheduleTraining_traineeScheduleOverlaps_throwsCannotScheduleTraining() {
        stubActiveTrainerAndTrainee();
        when(trainingRepository.getByTrainerId(TRAINER_ID)).thenReturn(List.of());
        // Existing 09:30 - 10:30 overlaps the requested 10:00 - 11:00.
        when(trainingRepository.getByTraineeId(TRAINEE_ID))
                .thenReturn(List.of(trainingEntity(50L, START.minusMinutes(30), ONE_HOUR)));

        assertThrows(CannotScheduleTrainingException.class,
                () -> service.scheduleTraining(request(START, ONE_HOUR)));

        verify(trainingRepository, never()).add(any());
    }

    @Test
    void scheduleTraining_inactiveTrainer_throwsCannotScheduleTraining() {
        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.of(trainerEntity(false)));

        assertThrows(CannotScheduleTrainingException.class,
                () -> service.scheduleTraining(request(START, ONE_HOUR)));

        verify(trainingRepository, never()).add(any());
    }

    @Test
    void scheduleTraining_traineeNotFound_throwsCannotScheduleTraining() {
        when(trainerRepository.getById(TRAINER_ID)).thenReturn(Optional.of(trainerEntity(true)));
        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.empty());

        assertThrows(CannotScheduleTrainingException.class,
                () -> service.scheduleTraining(request(START, ONE_HOUR)));

        verify(trainingRepository, never()).add(any());
    }

    @Test
    void scheduleTraining_backToBackWithExisting_returnsScheduledTraining() {
        stubActiveTrainerAndTrainee();
        // Existing 09:00 - 10:00 ends exactly when the requested 10:00 - 11:00 starts.
        when(trainingRepository.getByTrainerId(TRAINER_ID))
                .thenReturn(List.of(trainingEntity(50L, START.minusHours(1), ONE_HOUR)));
        when(trainingRepository.getByTraineeId(TRAINEE_ID)).thenReturn(List.of());
        stubAddAssignsId(100L);

        Training result = service.scheduleTraining(request(START, ONE_HOUR));

        assertEquals(100L, result.id());
    }


    @Test
    void removeTraining_existing_returnsRemovedTraining() {
        when(trainingRepository.deleteById(50L)).thenReturn(trainingEntity(50L, START, ONE_HOUR));

        Training result = service.removeTrainingById(50L);

        assertEquals(new Training(50L, TRAINEE_ID, TRAINER_ID, "Existing", TrainingType.Crossfit, START, ONE_HOUR), result);
    }

    @Test
    void removeTraining_notFound_propagatesItemNotFound() {
        when(trainingRepository.deleteById(50L)).thenThrow(new ItemNotFoundException(TrainingEntity.class, 50L));

        assertThrows(ItemNotFoundException.class, () -> service.removeTrainingById(50L));
    }


    @Test
    void getAll_returnsAllTrainings() {

        var storedTrainings = List.of(
                trainingEntity(50L, START, ONE_HOUR),
                trainingEntity(51L, START.plusDays(1), ONE_HOUR));

        when(trainingRepository.getAll()).thenReturn(storedTrainings);

        List<Training> result = service.getAll();

        assertEquals(storedTrainings.size(), result.size());
        assertEquals(
                storedTrainings.stream().map(EntityMapper::toTraining).collect(Collectors.toSet()),
                Set.copyOf(result));
    }
}
