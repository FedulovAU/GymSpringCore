package gym.service;

import gym.data.entity.TraineeEntity;
import gym.data.entity.TrainerEntity;
import gym.data.entity.TrainingEntity;
import gym.data.entity.UserEntity;
import gym.data.entity.TrainingTypeEntity;
import gym.dto.enums.TrainingType;
import gym.dto.request.ScheduleTraining;
import gym.dto.response.Training;
import gym.repository.trainee.TraineeRepository;
import gym.repository.trainer.TrainerRepository;
import gym.repository.TrainingRepository;
import gym.service.exception.CannotScheduleTrainingException;
import gym.service.mapper.GymMapper;
import gym.service.validation.RequestValidator;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    private static final long TRAINEE_ID = 1L;
    private static final long TRAINER_ID = 2L;
    /** 10:00 a week from now: the validator rejects trainings in the past. */
    private static final LocalDateTime START = LocalDate.now().plusWeeks(1).atTime(10, 0);
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
        service.setGymMapper(new GymMapper());
        service.setRequestValidator(new RequestValidator());
        service.setTrainingRepository(trainingRepository);
        service.setTrainerRepository(trainerRepository);
        service.setTraineeRepository(traineeRepository);
    }

    private static UserEntity user(boolean isActive) {
        return new UserEntity("Name", "Surname", "Name_Surname", 0L, "secret12345", isActive);
    }

    private static TrainingEntity existingTraining(LocalDateTime start, Duration duration) {
        return new TrainingEntity(TRAINEE_ID, TRAINER_ID, "Existing", TrainingTypeEntity.of(TrainingTypeEntity.TrainingType.CROSSFIT), start, duration);
    }

    private static ScheduleTraining request(LocalDateTime start, Duration duration) {
        return new ScheduleTraining(TRAINEE_ID, TRAINER_ID, "Morning", start, duration);
    }

    private void stubTrainer(boolean isActive) {
        when(trainerRepository.getById(TRAINER_ID))
                .thenReturn(Optional.of(new TrainerEntity(TRAINER_ID, user(isActive), TrainingTypeEntity.of(TrainingTypeEntity.TrainingType.CROSSFIT))));
    }

    private void stubTrainee(boolean isActive) {
        when(traineeRepository.getById(TRAINEE_ID))
                .thenReturn(Optional.of(new TraineeEntity(TRAINEE_ID, user(isActive), LocalDate.of(1990, 5, 17))));
    }

    private void stubActiveTrainerAndTrainee() {
        stubTrainer(true);
        stubTrainee(true);
    }

    private void verifyTrainerLoaded() {
        verify(trainerRepository).getById(TRAINER_ID);
    }

    private void verifyTraineeLoaded() {
        verify(traineeRepository).getById(TRAINEE_ID);
    }


    @Test
    void scheduleTraining_noConflicts_addsTrainingWithTrainerSpecialization() {
        stubActiveTrainerAndTrainee();
        when(trainingRepository.getByTrainerId(TRAINER_ID)).thenReturn(List.of());
        when(trainingRepository.getByTraineeId(TRAINEE_ID)).thenReturn(List.of());
        when(trainingRepository.add(any())).then(returnsFirstArg());

        Training result = service.scheduleTraining(request(START, ONE_HOUR));

        verifyTrainerLoaded();
        verifyTraineeLoaded();
        verify(trainingRepository).getByTrainerId(TRAINER_ID);
        verify(trainingRepository).getByTraineeId(TRAINEE_ID);
        verify(trainingRepository).add(argThat(t ->
                t.getTraineeId() == TRAINEE_ID
                && t.getTrainerId() == TRAINER_ID
                && t.getTrainingType().getTrainingType() == TrainingTypeEntity.TrainingType.CROSSFIT
                && t.getTrainingDate().equals(START)));
        assertEquals(new Training(TRAINEE_ID, TRAINER_ID, "Morning",
                TrainingType.CROSSFIT, START, ONE_HOUR), result);
    }

    @Test
    void scheduleTraining_dateInPast_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.scheduleTraining(request(LocalDateTime.now().minusDays(1), ONE_HOUR)));

        verifyNoInteractions(trainingRepository, trainerRepository, traineeRepository);
    }

    @Test
    void scheduleTraining_inactiveTrainer_throwsCannotScheduleTraining() {
        stubTrainer(false);

        assertThrows(CannotScheduleTrainingException.class,
                () -> service.scheduleTraining(request(START, ONE_HOUR)));

        verifyTrainerLoaded();
        verifyNoInteractions(trainingRepository, traineeRepository);
    }

    @Test
    void scheduleTraining_inactiveTrainee_throwsCannotScheduleTraining() {
        stubTrainer(true);
        stubTrainee(false);

        assertThrows(CannotScheduleTrainingException.class,
                () -> service.scheduleTraining(request(START, ONE_HOUR)));

        verifyTrainerLoaded();
        verifyTraineeLoaded();
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void scheduleTraining_traineeNotFound_throwsCannotScheduleTraining() {
        stubTrainer(true);
        when(traineeRepository.getById(TRAINEE_ID)).thenReturn(Optional.empty());

        assertThrows(CannotScheduleTrainingException.class,
                () -> service.scheduleTraining(request(START, ONE_HOUR)));

        verifyTrainerLoaded();
        verify(traineeRepository).getById(TRAINEE_ID);
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void scheduleTraining_trainerScheduleOverlaps_throwsCannotScheduleTraining() {
        stubActiveTrainerAndTrainee();
        // Existing 10:30 - 11:30 overlaps the requested 10:00 - 11:00.
        when(trainingRepository.getByTrainerId(TRAINER_ID))
                .thenReturn(List.of(existingTraining(START.plusMinutes(30), ONE_HOUR)));

        assertThrows(CannotScheduleTrainingException.class,
                () -> service.scheduleTraining(request(START, ONE_HOUR)));

        verifyTrainerLoaded();
        verifyTraineeLoaded();
        verify(trainingRepository).getByTrainerId(TRAINER_ID);
        verify(trainingRepository, never()).add(any());
    }

    @Test
    void scheduleTraining_traineeScheduleOverlaps_throwsCannotScheduleTraining() {
        stubActiveTrainerAndTrainee();
        when(trainingRepository.getByTrainerId(TRAINER_ID)).thenReturn(List.of());
        // Existing 09:30 - 10:30 overlaps the requested 10:00 - 11:00.
        when(trainingRepository.getByTraineeId(TRAINEE_ID))
                .thenReturn(List.of(existingTraining(START.minusMinutes(30), ONE_HOUR)));

        assertThrows(CannotScheduleTrainingException.class,
                () -> service.scheduleTraining(request(START, ONE_HOUR)));

        verifyTrainerLoaded();
        verifyTraineeLoaded();
        verify(trainingRepository).getByTrainerId(TRAINER_ID);
        verify(trainingRepository).getByTraineeId(TRAINEE_ID);
        verify(trainingRepository, never()).add(any());
    }

    @Test
    void scheduleTraining_backToBackWithExisting_addsTraining() {
        stubActiveTrainerAndTrainee();
        // Existing 09:00 - 10:00 ends exactly when the requested 10:00 - 11:00 starts.
        when(trainingRepository.getByTrainerId(TRAINER_ID))
                .thenReturn(List.of(existingTraining(START.minusHours(1), ONE_HOUR)));
        when(trainingRepository.getByTraineeId(TRAINEE_ID)).thenReturn(List.of());
        when(trainingRepository.add(any())).then(returnsFirstArg());

        service.scheduleTraining(request(START, ONE_HOUR));

        verifyTrainerLoaded();
        verifyTraineeLoaded();
        verify(trainingRepository).getByTrainerId(TRAINER_ID);
        verify(trainingRepository).getByTraineeId(TRAINEE_ID);
        verify(trainingRepository).add(argThat(t -> t.getTrainingDate().equals(START)));
    }


    @Test
    void getAll_returnsAllTrainings() {
        var tomorrow = START.plusDays(1);
        when(trainingRepository.getAll()).thenReturn(List.of(
                existingTraining(START, ONE_HOUR),
                existingTraining(tomorrow, ONE_HOUR)));

        List<Training> result = service.getAll();

        assertEquals(List.of(
                new Training(TRAINEE_ID, TRAINER_ID, "Existing", TrainingType.CROSSFIT, START, ONE_HOUR),
                new Training(TRAINEE_ID, TRAINER_ID, "Existing", TrainingType.CROSSFIT, tomorrow, ONE_HOUR)),
                result);
        verify(trainingRepository).getAll();
    }
}
