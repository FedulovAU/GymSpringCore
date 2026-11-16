package gym.services.dto.requests;

import java.time.LocalDate;

public record CreateTrainee(
        String firstName,
        String lastName,
        LocalDate birthDate) {
}
