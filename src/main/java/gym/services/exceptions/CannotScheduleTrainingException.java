package gym.services.exceptions;

/**
 * Thrown when a training can't be scheduled, e.g. the trainee or trainer doesn't exist or is inactive,
 * or the training overlaps with an already scheduled training of the same trainer or trainee.
 */
public class CannotScheduleTrainingException extends RuntimeException {
    public CannotScheduleTrainingException(String message) {
        super(message);
    }
}
