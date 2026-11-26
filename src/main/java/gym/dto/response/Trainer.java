package gym.dto.response;

import gym.enums.TrainingType;

public record Trainer(
        Long id,
        String username,
        String firstName,
        String lastName,
        TrainingType specialization) {
}
