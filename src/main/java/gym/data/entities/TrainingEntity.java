package gym.data.entities;

import gym.data.entities.enums.TrainingTypeEntity;
import gym.data.storage.EntityWithId;
import lombok.With;

import java.time.Duration;
import java.time.LocalDateTime;

@With
public record TrainingEntity(
        Long id,
        Long traineeId,
        Long trainerId,
        String trainingName,
        TrainingTypeEntity trainingType,
        LocalDateTime trainingDate,
        Duration trainingDuration) implements EntityWithId<TrainingEntity> {}
