package gym.data.entities;

import gym.data.entities.common.UserData;
import gym.data.entities.enums.TrainingTypeEntity;
import gym.data.storage.EntityWithId;
import lombok.With;

@With
public record TrainerEntity(
        Long id,
        UserData user,
        TrainingTypeEntity specialization) implements EntityWithId<TrainerEntity> {}
