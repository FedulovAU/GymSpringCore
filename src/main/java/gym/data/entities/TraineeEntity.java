package gym.data.entities;

import gym.data.entities.common.UserData;
import gym.data.storage.EntityWithId;
import lombok.With;

import java.time.LocalDate;

/**
 * {@link #toString()} masks personal data (user data, birth date), so the entity is safe to log.
 */
@With
public record TraineeEntity(
        Long id,
        UserData user,
        LocalDate birthDate) implements EntityWithId<TraineeEntity> {

    @Override
    public String toString() {
        return "TraineeEntity[id=" + id + ", user=" + user + ", birthDate=***]";
    }
}
