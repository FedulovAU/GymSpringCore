package gym.services.dto.responses;

import gym.services.dto.enums.TrainingType;

/**
 * {@link #toString()} masks personal data (names, username), so the object is safe to log.
 */
public record Trainer(
        Long id,
        String firstName,
        String lastName,
        String userName,
        boolean isActive,
        TrainingType specialization
) {
    @Override
    public String toString() {
        return "Trainer[id=" + id + ", firstName=***, lastName=***, userName=***, isActive=" + isActive
                + ", specialization=" + specialization + "]";
    }
}
