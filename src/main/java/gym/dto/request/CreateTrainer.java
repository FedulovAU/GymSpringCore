package gym.dto.request;

import gym.enums.TrainingType;

public record CreateTrainer(
        String firstName,
        String lastName,
        TrainingType specialization) {
}
