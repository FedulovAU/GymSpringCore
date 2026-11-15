package gym.services;

import gym.services.dto.requests.CreateTrainee;
import gym.services.dto.requests.CreateTrainer;
import gym.services.dto.requests.ScheduleTraining;
import gym.services.dto.requests.UpdateTrainee;
import gym.services.dto.requests.UpdateTrainer;
import gym.services.dto.responses.Trainee;
import gym.services.dto.responses.Trainer;
import gym.services.dto.responses.Training;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Single entry point to the gym's public API. Delegates to {@link TraineeService},
 * {@link TrainerService} and {@link TrainingService}; see them for details and thrown exceptions.
 */
@Service
public class GymFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(
            TraineeService traineeService,
            TrainerService trainerService,
            TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    // ---------- trainees ----------

    public Trainee createTrainee(CreateTrainee request) {
        return traineeService.createTrainee(request);
    }

    public Trainee updateTrainee(UpdateTrainee request) {
        return traineeService.updateTrainee(request);
    }

    public Trainee deactivateTrainee(long id) {
        return traineeService.deactivateTrainee(id);
    }

    /**
     * Deletes the trainee and all their trainings.
     */
    public Trainee deleteTrainee(long id) {
        return traineeService.deleteTrainee(id);
    }

    public Optional<Trainee> findTraineeById(long id) {
        return traineeService.findTraineeById(id);
    }

    public List<Trainee> getAllTrainees() {
        return traineeService.getAll();
    }

    // ---------- trainers ----------

    public Trainer createTrainer(CreateTrainer request) {
        return trainerService.createTrainer(request);
    }

    public Trainer updateTrainer(UpdateTrainer request) {
        return trainerService.updateTrainer(request);
    }

    public Trainer deactivateTrainer(long id) {
        return trainerService.deactivateTrainer(id);
    }

    public Optional<Trainer> findTrainerById(long id) {
        return trainerService.findTrainerById(id);
    }

    public List<Trainer> getAllTrainers() {
        return trainerService.getAll();
    }

    // ---------- trainings ----------

    public Training scheduleTraining(ScheduleTraining request) {
        return trainingService.scheduleTraining(request);
    }

    public Training removeTraining(long id) {
        return trainingService.removeTraining(id);
    }

    public List<Training> getAllTrainings() {
        return trainingService.getAll();
    }
}
