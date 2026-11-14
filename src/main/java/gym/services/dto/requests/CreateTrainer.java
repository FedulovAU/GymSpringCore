package gym.services.dto.requests;

import gym.services.dto.enums.TrainingType;

/**
 * Represents a request to create a new trainer.
 * All fields are required and must be provided when creating a new trainer.
 */
public record CreateTrainer(
        String firstName,
        String lastName,
        TrainingType specialization) {
}
