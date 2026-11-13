package gym.dto.requests;

import java.time.LocalDate;

public record CreateTrainee(
        String firstName,
        String lastName,
        String password,
        LocalDate birthDate) {
}
