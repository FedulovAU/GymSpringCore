package gym;

import gym.configuration.AppConfig;
import gym.data.storage.exceptions.ItemAlreadyExistsException;
import gym.data.storage.exceptions.ItemNotFoundException;
import gym.services.GymFacade;
import gym.services.dto.enums.TrainingType;
import gym.services.dto.requests.CreateTrainee;
import gym.services.dto.requests.CreateTrainer;
import gym.services.dto.requests.ScheduleTraining;
import gym.services.dto.requests.UpdateTrainee;
import gym.services.dto.requests.UpdateTrainer;
import gym.services.exceptions.CannotScheduleTrainingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Program {
    private static final Logger log = LoggerFactory.getLogger(Program.class);

    // IDs from the preloaded snapshot in src/main/resources/snapshot.
    private static final long TRAINER_MARK_BROWN_ID = 2;
    private static final long TRAINEE_JOHN_SMITH_ID = 1;
    private static final long TRAINEE_MIA_WONG_ID = 3;
    private static final long TRAINEE_KATE_GREEN_ID = 4;

    static void main(String[] args){
        try(var context = new AnnotationConfigApplicationContext(AppConfig.class)){
            runUseCase(context.getBean(GymFacade.class));
        }
    }

    private static void runUseCase(GymFacade gym) {
        try {
            var trainee = gym.createTrainee(new CreateTrainee(
                    "Kate",
                    "Miller",
                    LocalDate.of(1998, 4, 12)));
            
            var trainer = gym.createTrainer(new CreateTrainer(
                    "Tom",
                    "Reed",
                    TrainingType.Fitness));
            
            gym.scheduleTraining(new ScheduleTraining(
                    trainee.id(),
                    trainer.id(),
                    "Intro session",
                    LocalDateTime.of(2026, 12, 5, 18, 0), Duration.ofHours(1)));

            gym.updateTrainer(new UpdateTrainer(
                    TRAINER_MARK_BROWN_ID,
                    null,
                    null,
                    TrainingType.Crossfit));
            
            gym.updateTrainee(new UpdateTrainee(
                    TRAINEE_JOHN_SMITH_ID,
                    null,
                    "Smithson", 
                    null));

            gym.deactivateTrainee(TRAINEE_KATE_GREEN_ID);
            // Mia has two preloaded trainings, they're deleted together with her.
            gym.deleteTrainee(TRAINEE_MIA_WONG_ID);

            gym.getAllTrainers().forEach(t -> log.info("Trainer: {}", t));
            gym.getAllTrainees().forEach(t -> log.info("Trainee: {}", t));
            gym.getAllTrainings().forEach(t -> log.info("Training: {}", t));
            
        } catch (CannotScheduleTrainingException e) {
            log.error("Cannot schedule training: {}", e.getMessage(), e);
        } catch (ItemNotFoundException e) {
            log.error("Item not found: {} with ID {}.", e.getType().getSimpleName(), e.getId(), e);
        } catch (ItemAlreadyExistsException e) {
            log.error("Item already exists: {} with ID {}.", e.getType().getSimpleName(), e.getId(), e);
        } catch (RuntimeException e) {
            log.error("Unexpected error.", e);
        }
    }
}
