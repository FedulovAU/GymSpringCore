package gym.service.mapper;

import gym.data.entity.TraineeEntity;
import gym.data.entity.TrainerEntity;
import gym.data.entity.TrainingEntity;
import gym.data.entity.UserEntity;
import gym.data.enums.TrainingType;
import gym.dto.request.CreateTrainee;
import gym.dto.request.CreateTrainer;
import gym.dto.request.ScheduleTraining;
import gym.dto.request.UpdateTrainee;
import gym.dto.request.UpdateTrainer;
import gym.dto.response.Trainee;
import gym.dto.response.Trainer;
import gym.dto.response.Training;
import org.springframework.stereotype.Component;


@Component
public final class GymMapper {

    public UserEntity toUserEntity(CreateTrainee request, String password) {
        return new UserEntity(
                null,
                request.firstName(),
                request.lastName(),
                null,
                null,
                password,
                true);
    }

    public UserEntity toUserEntity(CreateTrainer request, String password) {
        return new UserEntity(
                null,
                request.firstName(),
                request.lastName(),
                null,
                null,
                password,
                true);
    }

    public TraineeEntity toTraineeEntity(CreateTrainee request, UserEntity userEntity) {
        return new TraineeEntity(userEntity.getId(), request.birthDate());
    }

    public TrainerEntity toTrainerEntity(CreateTrainer request, UserEntity userEntity) {
        return new TrainerEntity(userEntity.getId(), toTrainingType(request.specialization()));
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
        return traineeEntity;
    }

    public UserEntity toUpdatedUser(UpdateTrainee request, UserEntity userEntity) {
        if (request.firstName() != null) {
            userEntity = userEntity.withFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            userEntity = userEntity.withLastName(request.lastName());
        }
        return userEntity;
    }

    public UserEntity toUpdatedUser(UpdateTrainer request, UserEntity userEntity) {
        if (request.firstName() != null) {
            userEntity = userEntity.withFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            userEntity = userEntity.withLastName(request.lastName());
        }
        return userEntity;
    }



    public TrainerEntity toUpdatedTrainer(UpdateTrainer request, TrainerEntity trainerEntity) {
        if (request.specialization() != null) {
            trainerEntity = trainerEntity.withSpecialization(toTrainingType(request.specialization()));
        }
        return trainerEntity;
    }

    public Trainee toTrainee(TraineeEntity trainee, UserEntity userEntity) {
        return new Trainee(
                trainee.getId(),
                userEntity.getFirstName(),
                userEntity.getLastName(),
                userEntity.getUserName(),
                trainee.getBirthDate());
    }

    public Trainer toTrainer(TrainerEntity trainer, UserEntity userEntity) {
        return new Trainer(
                trainer.getId(),
                userEntity.getFirstName(),
                userEntity.getLastName(),
                userEntity.getUserName(),
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

    public TrainingType toTrainingType(gym.dto.enums.TrainingType trainingType) {
        if (trainingType == null) {
            return null;
        }
        return switch (trainingType) {
            case gym.dto.enums.TrainingType.BODY_BUILDING -> TrainingType.BODY_BUILDING;
            case gym.dto.enums.TrainingType.CROSSFIT -> TrainingType.CROSSFIT;
            case gym.dto.enums.TrainingType.FITNESS -> TrainingType.FITNESS;
        };
    }

    public gym.dto.enums.TrainingType toTrainingType(TrainingType trainingTypeEntity) {
        if (trainingTypeEntity == null) {
            return null;
        }
        return switch (trainingTypeEntity) {
            case BODY_BUILDING -> gym.dto.enums.TrainingType.BODY_BUILDING;
            case CROSSFIT -> gym.dto.enums.TrainingType.CROSSFIT;
            case FITNESS -> gym.dto.enums.TrainingType.FITNESS;
        };
    }
}
