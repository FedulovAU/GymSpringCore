package gym.dto.responses;

import gym.dto.enums.TrainingType;

import java.time.Duration;
import java.time.LocalDateTime;

public record Training(
        Long id,
        Long traineeId,
        Long trainerId,
        String trainingName,
        TrainingType trainingType,
        LocalDateTime trainingDate,
        Duration trainingDuration
) {}
