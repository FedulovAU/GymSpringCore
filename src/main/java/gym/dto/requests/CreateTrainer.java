package gym.dto.requests;

import gym.dto.enums.TrainingType;

/**
 * Represents a request to create a new trainer.
 * All fields are required and must be provided when creating a new trainer.
 */
public record CreateTrainer(
        String firstName,
        String lastName,
        TrainingType specialization) {
}
