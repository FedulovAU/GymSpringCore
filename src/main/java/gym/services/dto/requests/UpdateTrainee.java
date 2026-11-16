package gym.services.dto.requests;

import java.time.LocalDate;

public record UpdateTrainee(
        long id,
        String firstName,
        String lastName,
        LocalDate birthDate) {
}
