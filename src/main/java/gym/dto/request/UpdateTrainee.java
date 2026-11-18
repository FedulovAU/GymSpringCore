package gym.dto.request;

import java.time.LocalDate;

public record UpdateTrainee(
        long id,
        String firstName,
        String lastName,
        LocalDate birthDate) {
}
