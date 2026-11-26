package gym.dto.request;

import gym.enums.TrainingType;

public record UpdateTrainer(
        String username,
        String firstName,
        String lastName,
        TrainingType specialization) {
}
