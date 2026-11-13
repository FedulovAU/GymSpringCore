package gym.data.entities;

import gym.data.storage.EntityWithId;
import lombok.With;

import java.util.Date;

@With
public record TraineeEntity(
        Long id,
        User user,
        Date birthDate) implements EntityWithId<TraineeEntity> {}
