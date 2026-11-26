package gym.dto.request;

import java.time.Duration;
import java.time.LocalDateTime;

public record AddTraining(
        String traineeUsername,
        String trainerUsername,
        String trainingName,
        LocalDateTime trainingDate,
        Duration duration) {
}
