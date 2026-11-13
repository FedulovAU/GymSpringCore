package gym.data.entities;

import gym.data.storage.EntityWithId;
import lombok.With;

import java.time.LocalDate;

@With
public record TraineeEntity(
        Long id,
        User user,
        LocalDate birthDate) implements EntityWithId<TraineeEntity> {
}
