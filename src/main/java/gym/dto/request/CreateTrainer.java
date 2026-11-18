package gym.dto.request;

import gym.dto.enums.TrainingType;

public record CreateTrainer(
        String firstName,
        String lastName,
        TrainingType specialization) {
}
