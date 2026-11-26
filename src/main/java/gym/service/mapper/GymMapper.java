package gym.service.mapper;

import gym.data.TraineeEntity;
import gym.data.TrainerEntity;
import gym.data.TrainingEntity;
import gym.data.TrainingTypeEntity;
import gym.data.UserEntity;
import gym.dto.request.UpdateTrainee;
import gym.dto.request.UpdateTrainer;
import gym.dto.response.Trainee;
import gym.dto.response.Trainer;
import gym.dto.response.Training;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GymMapper {

    public Trainee toTrainee(TraineeEntity trainee) {
        UserEntity user = trainee.getUser();
        return new Trainee(
                trainee.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress());
    }

    public Trainer toTrainer(TrainerEntity trainer) {
        UserEntity user = trainer.getUser();
        return new Trainer(
                trainer.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                trainer.getSpecialization().getTrainingType());
    }

    public Training toTraining(TrainingEntity training) {
        return new Training(
                training.getId(),
                toTrainee(training.getTrainee()),
                toTrainer(training.getTrainer()),
                training.getTrainingName(),
                training.getTrainingType().getTrainingType(),
                training.getTrainingDate(),
                training.getTrainingDurationMinutes());
    }

    public void applyUpdate(UpdateTrainee request, TraineeEntity trainee) {
        applyNames(request.firstName(), request.lastName(), trainee.getUser());
        if (request.dateOfBirth() != null) {
            trainee.setDateOfBirth(request.dateOfBirth());
        }
        if (request.address() != null) {
            trainee.setAddress(request.address());
        }
    }

    public void applyUpdate(UpdateTrainer request, TrainerEntity trainer, Optional<TrainingTypeEntity> specialization) {
        applyNames(request.firstName(), request.lastName(), trainer.getUser());
        specialization.ifPresent(trainer::setSpecialization);
    }

    private static void applyNames(String firstName, String lastName, UserEntity user) {
        if (firstName != null) {
            user.setFirstName(firstName);
        }
        if (lastName != null) {
            user.setLastName(lastName);
        }
    }
}
