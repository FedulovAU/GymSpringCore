package gym.dto.responses;

import gym.dto.enums.TrainingType;

public record Trainer(
        Long id,
        String firstName,
        String lastName,
        String userName,
        boolean isActive,
        TrainingType specialization
) {}
