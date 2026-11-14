package gym.dto.requests;

import gym.dto.enums.TrainingType;

/**
 * Represents a request to update an existing trainer's information.
 * @param id, mandatory, the unique identifier of the trainer to be updated.
 * @param firstName, optional. If null, the first name will not be updated.
 * @param lastName, optional. If null, the last name will not be updated.
 * @param specialization, optional. If null, the specialization will not be updated.
 */
public record UpdateTrainer(
        long id,
        String firstName,
        String lastName,
        TrainingType specialization) {
}
