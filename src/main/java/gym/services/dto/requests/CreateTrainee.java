package gym.services.dto.requests;

import java.time.LocalDate;

/**
 * Represents a request to create a new trainee.
 * All fields are required and must be provided when creating a new trainee.
 */
public record CreateTrainee(
        String firstName,
        String lastName,
        LocalDate birthDate) {
}
