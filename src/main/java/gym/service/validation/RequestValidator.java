package gym.service.validation;

import gym.dto.request.AddTraining;
import gym.dto.request.CreateTrainee;
import gym.dto.request.CreateTrainer;
import gym.dto.request.UpdateTrainee;
import gym.dto.request.UpdateTrainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class RequestValidator {
    private static final String NAME_PATTERN = "\\p{L}+";

    @Value("${trainingDurationMinutesMin}")
    private int trainingDurationMinutesMin;

    @Value("${scheduleGapMinutesMin}")
    private int scheduleGapMinutesMin;

    @Value("${minTraineeAge}")
    private int minTraineeAge;

    public void validateCreateTraineeRequest(CreateTrainee request) {
        validateRequiredName(request.firstName(), "First name");
        validateRequiredName(request.lastName(), "Last name");
        validateDateOfBirth(request.dateOfBirth());
    }

    public void validateUpdateTraineeRequest(UpdateTrainee request) {
        validateOptionalName(request.firstName(), "First name");
        validateOptionalName(request.lastName(), "Last name");
        validateDateOfBirth(request.dateOfBirth());
    }

    public void validateCreateTrainerRequest(CreateTrainer request) {
        validateRequiredName(request.firstName(), "First name");
        validateRequiredName(request.lastName(), "Last name");
        if (request.specialization() == null) {
            throw new IllegalArgumentException("Specialization is required.");
        }
    }

    public void validateUpdateTrainerRequest(UpdateTrainer request) {
        validateOptionalName(request.firstName(), "First name");
        validateOptionalName(request.lastName(), "Last name");
    }

    public void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank.");
        }
    }

    public void validateAddTrainingRequest(AddTraining request) {
        if (request.trainingName() == null || request.trainingName().isBlank()) {
            throw new IllegalArgumentException("Training name must not be blank.");
        }

        if (request.trainingDate().isBefore(LocalDateTime.now().plusMinutes(scheduleGapMinutesMin))) {
            throw new IllegalArgumentException("Training date cannot be scheduled in the past or within "
                    + scheduleGapMinutesMin + " minutes from now.");
        }

        if (request.duration() == null || request.duration().compareTo(Duration.ofMinutes(trainingDurationMinutesMin)) < 0) {
            throw new IllegalArgumentException("Training duration must be at least " + trainingDurationMinutesMin + " minutes.");
        }
    }

    private void validateDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now().minusYears(minTraineeAge))) {
            throw new IllegalArgumentException("Trainee must be at least " + minTraineeAge + " years old.");
        }
    }

    private static void validateRequiredName(String name, String fieldName) {
        if (name == null || !name.matches(NAME_PATTERN)) {
            throw new IllegalArgumentException(fieldName + " is required and must contain only letters.");
        }
    }

    private static void validateOptionalName(String name, String fieldName) {
        if (name != null && !name.matches(NAME_PATTERN)) {
            throw new IllegalArgumentException(fieldName + " must contain only letters.");
        }
    }
}
