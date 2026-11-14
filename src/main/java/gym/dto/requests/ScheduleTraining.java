package gym.dto.requests;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Represents a request to schedule a new training.
 * All fields are required. The training type is taken from the trainer's specialization.
 * @param traineeId the unique identifier of the trainee.
 * @param trainerId the unique identifier of the trainer.
 * @param trainingName the name of the training.
 * @param trainingDate the start date and time of the training.
 * @param duration the duration of the training, must be positive.
 */
public record ScheduleTraining(
        long traineeId,
        long trainerId,
        String trainingName,
        LocalDateTime trainingDate,
        Duration duration) {
}
