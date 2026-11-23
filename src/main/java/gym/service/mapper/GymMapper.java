package gym.service.mapper;

import gym.data.entity.TraineeEntity;
import gym.data.entity.TrainerEntity;
import gym.data.entity.TrainingEntity;
import gym.data.entity.UserEntity;
import gym.data.entity.TrainingTypeEntity;
import gym.dto.enums.TrainingType;
import gym.dto.request.CreateTrainee;
import gym.dto.request.CreateTrainer;
import gym.dto.request.ScheduleTraining;
import gym.dto.request.UpdateTrainee;
import gym.dto.request.UpdateTrainer;
import gym.dto.response.Trainee;
import gym.dto.response.Trainer;
import gym.dto.response.Training;
import gym.service.helper.UsernameGenerator;
import org.springframework.stereotype.Component;


@Component
public final class GymMapper {

    public TraineeEntity toTraineeEntity(CreateTrainee request,  String username, long suffix, String password) {
        return new TraineeEntity(
                null,
                toUserEntity(request.firstName(), request.lastName(), username, suffix, password),
                request.birthDate());
    }

    public TrainerEntity toTrainerEntity(CreateTrainer request, String username, long suffix, String password) {
        return new TrainerEntity(
                null,
                toUserEntity(request.firstName(), request.lastName(), username, suffix, password),
                toTrainingType(request.specialization()));
    }

    public TrainingEntity toTrainingEntity(
            ScheduleTraining request, TraineeEntity trainee, TrainerEntity trainer) {
        return new TrainingEntity(
                trainee.getId(),
                trainer.getId(),
                request.trainingName(),
                trainer.getSpecialization(),
                request.trainingDate(),
                request.duration());
    }

    public TraineeEntity toUpdatedTrainee(UpdateTrainee request, TraineeEntity traineeEntity) {
        if (request.birthDate() != null) {
            traineeEntity = traineeEntity.withBirthDate(request.birthDate());
        }
        return traineeEntity.withUser(toUpdatedUser(request.firstName(), request.lastName(), traineeEntity.getUser()));
    }

    public TrainerEntity toUpdatedTrainer(UpdateTrainer request, TrainerEntity trainerEntity) {
        if (request.specialization() != null) {
            trainerEntity = trainerEntity.withSpecialization(toTrainingType(request.specialization()));
        }
        return trainerEntity.withUser(toUpdatedUser(request.firstName(), request.lastName(), trainerEntity.getUser()));
    }

    private static UserEntity toUpdatedUser(String firstName, String lastName, UserEntity userEntity) {
        if (firstName != null) {
            userEntity = userEntity.withFirstName(firstName);
        }
        if (lastName != null) {
            userEntity = userEntity.withLastName(lastName);
        }
        return userEntity;
    }

    public Trainee toTrainee(TraineeEntity trainee) {
        var user = trainee.getUser();
        return new Trainee(
                trainee.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserName(),
                trainee.getBirthDate());
    }

    public Trainer toTrainer(TrainerEntity trainer) {
        var user = trainer.getUser();
        return new Trainer(
                trainer.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserName(),
                toTrainingType(trainer.getSpecialization()));
    }

    public Training toTraining(TrainingEntity training) {
        return new Training(
                training.getTraineeId(),
                training.getTrainerId(),
                training.getTrainingName(),
                toTrainingType(training.getTrainingType()),
                training.getTrainingDate(),
                training.getTrainingDuration());
    }

    public TrainingTypeEntity toTrainingType(TrainingType trainingType) {
        if (trainingType == null) {
            return null;
        }
        return TrainingTypeEntity.of(switch (trainingType) {
            case BODY_BUILDING -> TrainingTypeEntity.TrainingType.BODY_BUILDING;
            case CROSSFIT -> TrainingTypeEntity.TrainingType.CROSSFIT;
            case FITNESS -> TrainingTypeEntity.TrainingType.FITNESS;
        });
    }

    public TrainingType toTrainingType(TrainingTypeEntity trainingTypeEntity) {
        if (trainingTypeEntity == null) {
            return null;
        }
        return switch (trainingTypeEntity.getTrainingType()) {
            case BODY_BUILDING -> TrainingType.BODY_BUILDING;
            case CROSSFIT -> TrainingType.CROSSFIT;
            case FITNESS -> TrainingType.FITNESS;
        };
    }

    private UserEntity toUserEntity(String firstName, String lastName, String username, long suffix, String password) {
        return new UserEntity(firstName, lastName, username, suffix, password, true);
    }
}
