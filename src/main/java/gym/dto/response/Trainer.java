package gym.dto.response;

import gym.dto.enums.TrainingType;

public record Trainer(
        Long id,
        String firstName,
        String lastName,
        String userName,
        TrainingType specialization
) {}
