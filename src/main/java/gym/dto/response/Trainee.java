package gym.dto.response;

import java.time.LocalDate;

public record Trainee(
        Long id,
        String firstName,
        String lastName,
        String userName,
        LocalDate birthDate
) {}
