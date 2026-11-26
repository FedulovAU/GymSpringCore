package gym.dto.request;

import java.time.LocalDate;

public record CreateTrainee(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address) {
}
