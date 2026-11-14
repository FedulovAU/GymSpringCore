package gym.data.entities;

import gym.data.entities.common.UserData;
import gym.data.storage.EntityWithId;
import lombok.With;

import java.time.LocalDate;

@With
public record TraineeEntity(
        Long id,
        UserData user,
        LocalDate birthDate) implements EntityWithId<TraineeEntity> {
}
