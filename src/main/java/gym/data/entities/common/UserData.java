package gym.data.entities.common;

import lombok.With;

/**
 * Not an entity, common user data used by user related entities like TraineeEntity and TrainerEntity.
 */
@With
public record UserData(
        String firstName,
        String lastName,
        String userName,
        String password,
        boolean isActive) {}
