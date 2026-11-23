package gym;

import gym.configuration.AppConfig;
import gym.data.storage.exception.ItemAlreadyExistsException;
import gym.data.storage.exception.ItemNotFoundException;
import gym.dto.enums.TrainingType;
import gym.dto.request.CreateTrainee;
import gym.dto.request.CreateTrainer;
import gym.dto.request.ScheduleTraining;
import gym.dto.request.UpdateTrainee;
import gym.dto.request.UpdateTrainer;
import gym.service.TraineeService;
import gym.service.TrainerService;
import gym.service.TrainingService;
import gym.service.exception.CannotScheduleTrainingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
public class Program {

    // IDs from the preloaded snapshot in src/main/resources/snapshot.
    private static final long TRAINER_MARK_BROWN_ID = 2;
    private static final long TRAINEE_JOHN_SMITH_ID = 1;
    private static final long TRAINEE_MIA_WONG_ID = 3;

    public static void main(String[] args){
        try(var context = new AnnotationConfigApplicationContext(AppConfig.class)){
            runUseCase(
                    context.getBean(TraineeService.class),
                    context.getBean(TrainerService.class),
                    context.getBean(TrainingService.class));
        }
    }

    private static void runUseCase(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        try {
            var trainee = traineeService.createTrainee(new CreateTrainee(
                    "Kate",
                    "Miller",
                    LocalDate.of(1998, 4, 12)));
            
            var trainer = trainerService.createTrainer(new CreateTrainer(
                    "Tom",
                    "Reed",
                    TrainingType.FITNESS));
            
            trainingService.scheduleTraining(new ScheduleTraining(
                    trainee.id(),
                    trainer.id(),
                    "Intro session",
                    LocalDateTime.of(2026, 12, 5, 18, 0), Duration.ofHours(1)));

            trainerService.updateTrainer(new UpdateTrainer(
                    TRAINER_MARK_BROWN_ID,
                    null,
                    null,
                    TrainingType.CROSSFIT));
            
            traineeService.updateTrainee(new UpdateTrainee(
                    TRAINEE_JOHN_SMITH_ID,
                    null,
                    "Smithson", 
                    null));


            traineeService.deleteTraineeById(TRAINEE_MIA_WONG_ID);

            trainerService.getAll().forEach(t -> log.info("Trainer: id={}, specialization={}", t.id(), t.specialization()));
            traineeService.getAll().forEach(t -> log.info("Trainee: id={}", t.id()));
            trainingService.getAll().forEach(t -> log.info("Training: traineeId={}, trainerId={}", t.traineeId(), t.trainerId()));
            
        } catch (CannotScheduleTrainingException e) {
            log.error("Cannot schedule training: {}", e.getMessage(), e);
        } catch (ItemNotFoundException e) {
            log.error("Item not found: {}", e.getMessage(), e);
        } catch (ItemAlreadyExistsException e) {
            log.error("Item already exists: {}", e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Unexpected error.", e);
        }
    }
}
