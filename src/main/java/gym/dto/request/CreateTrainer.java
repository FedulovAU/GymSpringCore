package gym.dto.request;

import gym.enums.TrainingType;

public record CreateTrainer(
        String firstName,
        String lastName,
        String password,
        TrainingType specialization) {
}
