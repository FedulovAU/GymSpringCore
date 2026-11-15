package gym.services.dto.responses;

import gym.services.dto.enums.TrainingType;

public record Trainer(
        Long id,
        String firstName,
        String lastName,
        String userName,
        boolean isActive,
        TrainingType specialization
) {}
