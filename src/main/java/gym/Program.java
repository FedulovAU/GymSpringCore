package gym;

import gym.configuration.AppConfig;
import gym.enums.TrainingType;
import gym.dto.request.AddTraining;
import gym.dto.request.CreateTrainee;
import gym.dto.request.CreateTrainer;
import gym.dto.request.TraineeTrainingsFilter;
import gym.dto.request.TrainerTrainingsFilter;
import gym.dto.request.UpdateTrainee;
import gym.dto.request.UpdateTrainer;
import gym.dto.response.Credentials;
import gym.service.TraineeService;
import gym.service.TrainerService;
import gym.service.TrainingService;
import gym.service.exception.CannotScheduleTrainingException;
import gym.service.exception.ProfileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
public class Program {

    private static final String SEEDED_TRAINEE = "John_Smith";
    private static final String SEEDED_TRAINER = "Mark_Brown";
    private static final String SEEDED_TRAINEE_TO_DELETE = "Mia_Wong";

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            runUseCase(
                    context.getBean(TraineeService.class),
                    context.getBean(TrainerService.class),
                    context.getBean(TrainingService.class));
        }
    }

    private static void runUseCase(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        try {
            Credentials trainee = traineeService.createTrainee(
                    new CreateTrainee("John", "Smith", LocalDate.of(1998, 4, 12), "5 Elm St"));
            Credentials trainer = trainerService.createTrainer(
                    new CreateTrainer("Tom", "Reed", TrainingType.FITNESS));
            log.info("Created trainee {} and trainer {}.", trainee.username(), trainer.username());

            log.info("Trainee credentials match: {}.", traineeService.matchesCredentials(trainee.username(), trainee.password()));
            log.info("Trainer credentials match: {}.", trainerService.matchesCredentials(trainer.username(), trainer.password()));

            traineeService.findByUsername(trainee.username()).ifPresent(t -> log.info("Trainee profile: {}.", t));
            trainerService.findByUsername(trainer.username()).ifPresent(t -> log.info("Trainer profile: {}.", t));

            traineeService.changePassword(trainee.username(), "newTraineePassword");
            trainerService.changePassword(trainer.username(), "newTrainerPassword");

            traineeService.updateTrainee(new UpdateTrainee(SEEDED_TRAINEE, null, "Smithson", null, "1 New Rd"));
            trainerService.updateTrainer(new UpdateTrainer(SEEDED_TRAINER, null, null, TrainingType.CROSSFIT));

            traineeService.setActive(SEEDED_TRAINEE, false);
            traineeService.setActive(SEEDED_TRAINEE, true);
            trainerService.setActive(SEEDED_TRAINER, false);
            trainerService.setActive(SEEDED_TRAINER, true);

            traineeService.deleteByUsername(SEEDED_TRAINEE_TO_DELETE);

            LocalDateTime nextWeek = LocalDate.now().plusWeeks(1).atTime(18, 0);
            trainingService.addTraining(new AddTraining(
                    trainee.username(), trainer.username(), "Intro session", nextWeek, 60));

            trainingService.getTraineeTrainings(SEEDED_TRAINEE, new TraineeTrainingsFilter(null, null, null, null))
                    .forEach(t -> log.info("Trainee training: {}.", t));
            trainingService.getTrainerTrainings(trainer.username(), new TrainerTrainingsFilter(null, null, "John"))
                    .forEach(t -> log.info("Trainer training: {}.", t));

            trainerService.getTrainersNotAssignedToTrainee(SEEDED_TRAINEE)
                    .forEach(t -> log.info("Trainer not assigned to {}: {}.", SEEDED_TRAINEE, t));
        } catch (CannotScheduleTrainingException e) {
            log.error("Cannot schedule training: {}", e.getMessage(), e);
        } catch (ProfileNotFoundException e) {
            log.error("Profile not found: {}", e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Unexpected error.", e);
        }
    }
}
