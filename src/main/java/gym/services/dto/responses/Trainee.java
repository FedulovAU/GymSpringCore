package gym.services.dto.responses;

import java.time.LocalDate;

/**
 * {@link #toString()} masks personal data (names, username, birth date), so the object is safe to log.
 */
public record Trainee(
        Long id,
        String firstName,
        String lastName,
        String userName,
        boolean isActive,
        LocalDate birthDate
) {
    @Override
    public String toString() {
        return "Trainee[id=" + id + ", firstName=***, lastName=***, userName=***, isActive=" + isActive
                + ", birthDate=***]";
    }
}
