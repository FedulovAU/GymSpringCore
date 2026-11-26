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
import gym.service.TraineeService;
import gym.service.TrainerService;
import gym.service.TrainingService;
import gym.service.exception.AuthenticationException;
import gym.service.exception.CannotScheduleTrainingException;
import gym.service.exception.ProfileNotFoundException;
import gym.service.helper.PasswordGenerator;
import gym.service.security.AuthenticationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
public class Program {

    private static final String SEEDED_TRAINEE = "John_Smith";
    private static final String SEEDED_TRAINEE_TO_DELETE = "Mia_Wong";
    private static final String SEEDED_TRAINEE_TO_DELETE_PASSWORD = "IJqEpIXZHt";

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            runUseCase(
                    context.getBean(TraineeService.class),
                    context.getBean(TrainerService.class),
                    context.getBean(TrainingService.class),
                    context.getBean(PasswordGenerator.class));
        }
    }

    private static void runUseCase(
            TraineeService traineeService,
            TrainerService trainerService,
            TrainingService trainingService,
            PasswordGenerator passwordGenerator) {
        try {
            String traineePassword = passwordGenerator.generate();
            var createTrainee = new CreateTrainee(
                    "John", "Smith", traineePassword, LocalDate.of(1998, 4, 12), "5 Elm St");
            var trainee = traineeService.createTrainee(createTrainee);
            log.info("Created trainee, id={}.", trainee.id());

            String trainerPassword = passwordGenerator.generate();
            var createTrainer = new CreateTrainer("Tom", "Reed", trainerPassword, TrainingType.FITNESS);
            var trainer = trainerService.createTrainer(createTrainer);
            log.info("Created trainer, id={}.", trainer.id());

            log.info("Trainee credentials match: {}.", traineeService.matchesCredentials(trainee.username(), traineePassword));
            log.info("Trainer credentials match: {}.", trainerService.matchesCredentials(trainer.username(), trainerPassword));

            try {
                traineeService.findByUsername(trainee.username());
            } catch (AuthenticationException e) {
                log.info("Profile lookup without sign in rejected: {}", e.getMessage());
            }

            traineeService.signIn(trainee.username(), traineePassword);

            traineeService.findByUsername(trainee.username()).ifPresent(t -> log.info("Trainee profile found, id={}.", t.id()));
            trainerService.findByUsername(trainer.username()).ifPresent(t -> log.info("Trainer profile found, id={}.", t.id()));

            String newTraineePassword = passwordGenerator.generate();
            traineeService.changePassword(newTraineePassword);
            traineeService.updateTrainee(new UpdateTrainee(null, "Smithson", null, "1 New Rd"));
            log.info("Trainee active after switch: {}.", traineeService.switchActive());
            log.info("Trainee active after switch: {}.", traineeService.switchActive());

            LocalDateTime nextWeek = LocalDate.now().plusWeeks(1).atTime(18, 0);
            var addTraining = new AddTraining(trainee.username(), trainer.username(), "Intro session", nextWeek, 60);
            trainingService.addTraining(addTraining);

            var traineeFilter = new TraineeTrainingsFilter(null, null, null, null);
            trainingService.getTraineeTrainings(SEEDED_TRAINEE, traineeFilter)
                    .forEach(t -> log.info("Trainee training, id={}.", t.id()));
            trainerService.getTrainersNotAssignedToTrainee(SEEDED_TRAINEE)
                    .forEach(t -> log.info("Trainer not assigned to trainee, trainerId={}.", t.id()));

            traineeService.signOut();

            traineeService.signIn(trainee.username(), newTraineePassword);
            log.info("Trainee signed in again with the changed password.");
            traineeService.signOut();

            trainerService.signIn(trainer.username(), trainerPassword);

            String newTrainerPassword = passwordGenerator.generate();
            trainerService.changePassword(newTrainerPassword);
            trainerService.updateTrainer(new UpdateTrainer(null, null, TrainingType.CROSSFIT));
            log.info("Trainer active after switch: {}.", trainerService.switchActive());
            log.info("Trainer active after switch: {}.", trainerService.switchActive());

            var trainerFilter = new TrainerTrainingsFilter(null, null, "John");
            trainingService.getTrainerTrainings(trainer.username(), trainerFilter)
                    .forEach(t -> log.info("Trainer training, id={}.", t.id()));

            trainerService.signOut();

            traineeService.signIn(SEEDED_TRAINEE_TO_DELETE, SEEDED_TRAINEE_TO_DELETE_PASSWORD);
            traineeService.deleteProfile();

        } catch (AuthenticationException e) {
            log.error("Authentication failed: {}", e.getMessage(), e);
        } catch (CannotScheduleTrainingException e) {
            log.error("Cannot schedule training: {}", e.getMessage(), e);
        } catch (ProfileNotFoundException e) {
            log.error("Profile not found: {}", e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Unexpected error.", e);
        }
    }
}
