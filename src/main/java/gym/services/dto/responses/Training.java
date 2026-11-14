package gym.services.dto.responses;

import gym.services.dto.enums.TrainingType;

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
