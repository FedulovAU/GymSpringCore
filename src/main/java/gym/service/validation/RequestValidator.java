package gym.service.validation;

import gym.dto.request.CreateTrainee;
import gym.dto.request.CreateTrainer;
import gym.dto.request.ScheduleTraining;
import gym.dto.request.UpdateTrainee;
import gym.dto.request.UpdateTrainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Component
public final class RequestValidator {
    private static final String userNamePattern = "\\p{L}+";

    @Value("${trainingDurationMinutesMin}")
    private int trainingDurationMinutesMin;

    @Value("${scheduleGapMinutesMin}")
    private int scheduleGapMinutesMin;

    @Value("${minTraineeAge}")
    private int minTraineeAge;

    public void validateUpdateTraineeRequest(UpdateTrainee request) {

        if (request.firstName() != null && !request.firstName().matches(userNamePattern)) {
            throw new IllegalArgumentException("First name contains invalid characters.");
        }

        if (request.lastName() != null && !request.lastName().matches(userNamePattern)) {
            throw new IllegalArgumentException("Last name contains invalid characters.");
        }

        if (request.birthDate() != null && request.birthDate().isAfter(java.time.LocalDate.now().minusYears(minTraineeAge))) {
            throw new IllegalArgumentException("Trainee must be at least " + minTraineeAge + " years old.");
        }
    }

    public void validateCreateTraineeRequest(CreateTrainee request) {
        validateFirstLastName(request.firstName(), request.lastName());

        if (request.birthDate() != null && request.birthDate().isAfter(java.time.LocalDate.now().minusYears(minTraineeAge))) {
            throw new IllegalArgumentException("Trainee must be at least " + minTraineeAge + " years old.");
        }
    }

    public void validateCreateTrainerRequest(CreateTrainer request) {
        validateFirstLastName(request.firstName(), request.lastName());
    }

    public void validateUpdateTrainerRequest(UpdateTrainer request) {
        validateFirstLastName(request.firstName(), request.lastName());
    }

    public void validateFirstLastName(String firstName, String lastName) {

        if (firstName != null && !firstName.matches(userNamePattern)) {
            throw new IllegalArgumentException("First name contains invalid characters.");
        }

        if (lastName != null && !lastName.matches(userNamePattern)) {
            throw new IllegalArgumentException("Last name contains invalid characters.");
        }
    }

    public void validateScheduleTrainingRequest(ScheduleTraining request) {
        if (request.trainingDate().isBefore(java.time.LocalDateTime.now().plusMinutes(scheduleGapMinutesMin))) {
            throw new IllegalArgumentException("Training date cannot be scheduled in the past or within " + scheduleGapMinutesMin + " minutes from now.");
        }

        if (request.duration() == null || request.duration().compareTo(Duration.ofMinutes(trainingDurationMinutesMin)) < 0) {
            throw new IllegalArgumentException("Training duration must be at least " + trainingDurationMinutesMin + " minutes.");
        }
    }
}
