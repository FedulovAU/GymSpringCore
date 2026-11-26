package gym.dto.response;

import gym.enums.TrainingType;

import java.time.LocalDateTime;

public record Training(
        Long id,
        Trainee trainee,
        Trainer trainer,
        String trainingName,
        TrainingType trainingType,
        LocalDateTime trainingDate,
        int trainingDurationMinutes) {
}
