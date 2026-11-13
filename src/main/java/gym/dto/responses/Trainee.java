package gym.dto.responses;

import java.time.LocalDate;

public record Trainee(
        Long id,
        String firstName,
        String lastName,
        String userName,
        boolean isActive,
        LocalDate birthDate
) {}
