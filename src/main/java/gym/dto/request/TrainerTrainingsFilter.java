package gym.dto.request;

import java.time.LocalDate;

public record TrainerTrainingsFilter(
        LocalDate fromDate,
        LocalDate toDate,
        String traineeName) {
}
