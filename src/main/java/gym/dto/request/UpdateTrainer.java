package gym.dto.request;

import gym.dto.enums.TrainingType;

public record UpdateTrainer(
        long id,
        String firstName,
        String lastName,
        TrainingType specialization) {
}
