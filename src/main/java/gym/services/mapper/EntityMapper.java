package gym.services.mapper;

import gym.data.entities.TraineeEntity;
import gym.data.entities.TrainerEntity;
import gym.data.entities.TrainingEntity;
import gym.data.entities.enums.TrainingTypeEntity;
import gym.services.dto.enums.TrainingType;
import gym.services.dto.responses.Trainee;
import gym.services.dto.responses.Trainer;
import gym.services.dto.responses.Training;


public final class EntityMapper {

    private EntityMapper() {}

    public static Trainee toTrainee(TraineeEntity trainee) {
        return new Trainee(
                trainee.id(),
                trainee.user().firstName(),
                trainee.user().lastName(),
                trainee.user().userName(),
                trainee.user().isActive(),
                trainee.birthDate());
    }

    public static Trainer toTrainer(TrainerEntity trainer) {
        return new Trainer(
                trainer.id(),
                trainer.user().firstName(),
                trainer.user().lastName(),
                trainer.user().userName(),
                trainer.user().isActive(),
                toTrainingType(trainer.specialization()));
    }

    public static Training toTraining(TrainingEntity training) {
        return new Training(
                training.id(),
                training.traineeId(),
                training.trainerId(),
                training.trainingName(),
                toTrainingType(training.trainingType()),
                training.trainingDate(),
                training.trainingDuration());
    }

    public static TrainingTypeEntity toTrainingTypeEntity(TrainingType trainingType) {
        if (trainingType == null) {
            return null;
        }
        return switch (trainingType) {
            case Bodybuilding -> TrainingTypeEntity.Bodybuilding;
            case Crossfit -> TrainingTypeEntity.Crossfit;
            case Fitness -> TrainingTypeEntity.Fitness;
        };
    }

    public static TrainingType toTrainingType(TrainingTypeEntity trainingTypeEntity) {
        if (trainingTypeEntity == null) {
            return null;
        }
        return switch (trainingTypeEntity) {
            case Bodybuilding -> TrainingType.Bodybuilding;
            case Crossfit -> TrainingType.Crossfit;
            case Fitness -> TrainingType.Fitness;
        };
    }
}
