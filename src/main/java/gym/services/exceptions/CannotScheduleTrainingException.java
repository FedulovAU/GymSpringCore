package gym.services.exceptions;

public class CannotScheduleTrainingException extends RuntimeException {
    public CannotScheduleTrainingException(String message) {
        super(message);
    }
}
