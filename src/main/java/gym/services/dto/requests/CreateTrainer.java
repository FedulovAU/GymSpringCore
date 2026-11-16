package gym.services.dto.requests;

import gym.services.dto.enums.TrainingType;

public record CreateTrainer(
        String firstName,
        String lastName,
        TrainingType specialization) {
}
