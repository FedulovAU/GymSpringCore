package gym.dto.response;

import java.time.LocalDate;

public record Trainee(
        String username,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        boolean isActive) {
}
