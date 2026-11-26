package gym.service;

import gym.data.TraineeEntity;
import gym.data.TrainerEntity;
import gym.data.TrainingEntity;
import gym.data.TrainingTypeEntity;
import gym.data.UserEntity;
import gym.enums.TrainingType;
import gym.dto.request.AddTraining;
import gym.dto.request.TraineeTrainingsFilter;
import gym.dto.request.TrainerTrainingsFilter;
import gym.dto.response.Training;
import gym.repository.TraineeRepository;
import gym.repository.TrainerRepository;
import gym.repository.TrainingRepository;
import gym.service.exception.CannotScheduleTrainingException;
import gym.service.exception.ProfileNotFoundException;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    private static final String TRAINEE = "John_Smith";
    private static final String TRAINER = "Anna_Lee";
    private static final LocalDateTime START = LocalDate.now().plusWeeks(1).atTime(10, 0);
    private static final LocalDateTime END = START.plusHours(1);
    private static final Duration ONE_HOUR = Duration.ofHours(1);

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;

    private TrainingService service;

    @BeforeEach
    void setUp() {
        service = new TrainingService(
                trainingRepository,
                traineeRepository,
                trainerRepository,
                new RequestValidator(),
                new GymMapper());
    }

    private static TrainingTypeEntity crossfit() {
        TrainingTypeEntity entity = mock(TrainingTypeEntity.class);
        lenient().when(entity.getTrainingType()).thenReturn(TrainingType.CROSSFIT);
        return entity;
    }

    private static UserEntity user(String username, boolean isActive) {
        return new UserEntity("Name", "Surname", "secret12345", isActive, username, username, 0);
    }

    private TraineeEntity stubTrainee(boolean isActive) {
        TraineeEntity trainee = new TraineeEntity(user(TRAINEE, isActive), null, null);
        when(traineeRepository.findByUsername(TRAINEE)).thenReturn(Optional.of(trainee));
        return trainee;
    }

    private TrainerEntity stubTrainer(boolean isActive) {
        TrainerEntity trainer = new TrainerEntity(user(TRAINER, isActive), crossfit());
        when(trainerRepository.findByUsername(TRAINER)).thenReturn(Optional.of(trainer));
        return trainer;
    }

    private static AddTraining request() {
        return new AddTraining(TRAINEE, TRAINER, "Morning", START, ONE_HOUR);
    }

    @Test
    void addTraining_free_savesTrainingWithTrainerSpecialization() {
        TraineeEntity trainee = stubTrainee(true);
        TrainerEntity trainer = stubTrainer(true);
        when(trainingRepository.save(any())).then(returnsFirstArg());

        Training result = service.addTraining(request());

        var expectedTraining = new Training(TRAINEE, TRAINER, "Morning", TrainingType.CROSSFIT, START, ONE_HOUR);
        assertEquals(expectedTraining, result);
        verify(trainingRepository).existsOverlappingForTrainer(trainer, START, END);
        verify(trainingRepository).existsOverlappingForTrainee(trainee, START, END);
        verify(trainingRepository).save(argThat(t -> t.getTrainee() == trainee
                && t.getTrainer() == trainer
                && t.getTrainingType() == trainer.getSpecialization()
                && t.getTrainingDurationMinutes() == 60));
        verify(traineeRepository).findByUsername(TRAINEE);
        verify(trainerRepository).findByUsername(TRAINER);
    }

    @Test
    void addTraining_trainerBusy_throwsAndSavesNothing() {
        stubTrainee(true);
        TrainerEntity trainer = stubTrainer(true);
        when(trainingRepository.existsOverlappingForTrainer(trainer, START, END)).thenReturn(true);

        assertThrows(CannotScheduleTrainingException.class, () -> service.addTraining(request()));

        verify(trainingRepository, never()).save(any());
        verify(traineeRepository).findByUsername(TRAINEE);
        verify(trainerRepository).findByUsername(TRAINER);
        verify(trainingRepository).existsOverlappingForTrainer(trainer, START, END);
    }

    @Test
    void addTraining_traineeBusy_throwsAndSavesNothing() {
        TraineeEntity trainee = stubTrainee(true);
        stubTrainer(true);
        when(trainingRepository.existsOverlappingForTrainee(trainee, START, END)).thenReturn(true);

        assertThrows(CannotScheduleTrainingException.class, () -> service.addTraining(request()));

        verify(trainingRepository, never()).save(any());
        verify(traineeRepository).findByUsername(TRAINEE);
        verify(trainerRepository).findByUsername(TRAINER);
        verify(trainingRepository).existsOverlappingForTrainee(trainee, START, END);
    }

    @Test
    void addTraining_inactiveTrainer_throwsCannotSchedule() {
        stubTrainee(true);
        stubTrainer(false);

        assertThrows(CannotScheduleTrainingException.class, () -> service.addTraining(request()));

        verify(trainingRepository, never()).save(any());
        verify(traineeRepository).findByUsername(TRAINEE);
        verify(trainerRepository).findByUsername(TRAINER);
    }

    @Test
    void addTraining_unknownTrainee_throwsProfileNotFound() {
        when(traineeRepository.findByUsername(TRAINEE)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> service.addTraining(request()));

        verify(trainingRepository, never()).save(any());
        verify(traineeRepository).findByUsername(TRAINEE);
    }

    @Test
    void getTraineeTrainings_fullFilter_passesWholeDayRangeAndEntityType() {
        LocalDate from = LocalDate.of(2026, 12, 1);
        LocalDate to = LocalDate.of(2026, 12, 3);
        when(trainingRepository.findTraineeTrainings(any(), any(), any(), any(), any())).thenReturn(List.of());

        var filter = new TraineeTrainingsFilter(from, to, "Anna", TrainingType.CROSSFIT);
        service.getTraineeTrainings(TRAINEE, filter);

        var expectedFrom = from.atStartOfDay();
        var expectedTo = LocalDate.of(2026, 12, 4).atStartOfDay();
        verify(trainingRepository).findTraineeTrainings(
                TRAINEE, expectedFrom, expectedTo, "Anna", TrainingType.CROSSFIT);
    }

    @Test
    void getTraineeTrainings_emptyFilter_passesNoCriteria() {
        when(trainingRepository.findTraineeTrainings(any(), any(), any(), any(), any())).thenReturn(List.of());

        var filter = new TraineeTrainingsFilter(null, null, null, null);
        service.getTraineeTrainings(TRAINEE, filter);

        verify(trainingRepository).findTraineeTrainings(TRAINEE, null, null, null, null);
    }

    @Test
    void getTrainerTrainings_mapsFoundTrainings() {
        var trainee = new TraineeEntity(user(TRAINEE, true), null, null);
        var trainer = new TrainerEntity(user(TRAINER, true), crossfit());
        TrainingEntity training = new TrainingEntity(trainee, trainer, "Morning", crossfit(), START, 60);
        when(trainingRepository.findTrainerTrainings(TRAINER, null, null, "John")).thenReturn(List.of(training));

        var filter = new TrainerTrainingsFilter(null, null, "John");
        List<Training> result = service.getTrainerTrainings(TRAINER, filter);

        var expectedTraining = new Training(TRAINEE, TRAINER, "Morning", TrainingType.CROSSFIT, START, ONE_HOUR);
        assertEquals(List.of(expectedTraining), result);
        verify(trainingRepository).findTrainerTrainings(TRAINER, null, null, "John");
    }
}
