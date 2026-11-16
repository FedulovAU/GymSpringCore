package gym.services.dto.requests;

import gym.services.dto.enums.TrainingType;

public record UpdateTrainer(
        long id,
        String firstName,
        String lastName,
        TrainingType specialization) {
}
