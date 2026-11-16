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

    public Trainee deactivateTraineeById(long id) {
        return traineeService.deactivateTraineeById(id);
    }

    public Trainee deleteTraineeById(long id) {
        return traineeService.deleteTraineeById(id);
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

    public Trainer deactivateTrainerById(long id) {
        return trainerService.deactivateTrainerById(id);
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

    public Training removeTrainingById(long id) {
        return trainingService.removeTrainingById(id);
    }

    public List<Training> getAllTrainings() {
        return trainingService.getAll();
    }
}
