package gym.dto.request;

import java.time.LocalDate;

public record UpdateTrainee(
        String username,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address) {
}
