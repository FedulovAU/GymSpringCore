package gym.dto.request;

import java.time.Duration;
import java.time.LocalDateTime;

public record ScheduleTraining(
        long traineeId,
        long trainerId,
        String trainingName,
        LocalDateTime trainingDate,
        Duration duration) {
}
