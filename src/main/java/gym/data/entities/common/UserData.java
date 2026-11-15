package gym.data.entities.common;

import lombok.With;

/**
 * Not an entity, common user data used by user related entities like TraineeEntity and TrainerEntity.
 * {@link #toString()} masks personal data, so the object is safe to log.
 */
@With
public record UserData(
        String firstName,
        String lastName,
        String userName,
        String password,
        boolean isActive) {

    static final String MASK = "***";

    @Override
    public String toString() {
        return "UserData[firstName=" + MASK + ", lastName=" + MASK + ", userName=" + MASK
                + ", password=" + MASK + ", isActive=" + isActive + "]";
    }
}
