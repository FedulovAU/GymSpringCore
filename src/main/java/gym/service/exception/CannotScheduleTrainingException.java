package gym.service.exception;

public class CannotScheduleTrainingException extends RuntimeException {
    public CannotScheduleTrainingException(String message) {
        super(message);
    }
}
