package gym.dto.response;

import gym.enums.TrainingType;

import java.time.Duration;
import java.time.LocalDateTime;

public record Training(
        String traineeUsername,
        String trainerUsername,
        String trainingName,
        TrainingType trainingType,
        LocalDateTime trainingDate,
        Duration trainingDuration) {
}
