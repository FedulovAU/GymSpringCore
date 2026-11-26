package gym.dto.request;

import java.time.LocalDate;

public record CreateTrainee(
        String firstName,
        String lastName,
        String password,
        LocalDate dateOfBirth,
        String address) {
}
