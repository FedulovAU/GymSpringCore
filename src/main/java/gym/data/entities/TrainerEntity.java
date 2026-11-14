package gym.data.entities;

import gym.data.entities.common.UserData;
import gym.data.entities.enums.TrainingType;
import gym.data.storage.EntityWithId;
import lombok.With;

@With
public record TrainerEntity(
        Long id,
        UserData user,
        TrainingType specialization) implements EntityWithId<TrainerEntity> {}
