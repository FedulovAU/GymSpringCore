package gym.services.validation;

import gym.services.dto.requests.CreateTrainee;
import gym.services.dto.requests.CreateTrainer;
import gym.services.dto.requests.ScheduleTraining;
import gym.services.dto.requests.UpdateTrainee;
import gym.services.dto.requests.UpdateTrainer;

/**
 * Validates service requests: checks that don't need storage access.
 */
public final class RequestValidator {
    private static final String userNamePattern = "\\p{L}+";

    private RequestValidator() {}

    public static void validateUpdateTraineeRequest(UpdateTrainee request) {
        if(request == null) {
            throw new IllegalArgumentException("UpdateTrainee request cannot be null.");
        }

        if (request.id() <= 0) {
            throw new IllegalArgumentException("Trainee ID must be a positive number.");
        }

        if (request.firstName() != null && !request.firstName().matches(userNamePattern)) {
            throw new IllegalArgumentException("First name contains invalid characters.");
        }

        if (request.lastName() != null && !request.lastName().matches(userNamePattern)) {
            throw new IllegalArgumentException("Last name contains invalid characters.");
        }

        if (request.birthDate() != null && request.birthDate().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Birth date cannot be in the future.");
        }
    }

    public static void validateCreateTraineeRequest(CreateTrainee request) {
        if(request == null) {
            throw new IllegalArgumentException("CreateTrainee request cannot be null.");
        }

        if (request.firstName() == null || request.firstName().isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or blank.");
        }
        if(!request.firstName().matches(userNamePattern)){
            throw new IllegalArgumentException("First name contains invalid characters.");
        }

        if (request.lastName() == null || request.lastName().isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or blank.");
        }
        if(!request.lastName().matches(userNamePattern)){
            throw new IllegalArgumentException("Last name contains invalid characters.");
        }

        if (request.birthDate() == null) {
            throw new IllegalArgumentException("Birth date cannot be null.");
        }

        if(request.birthDate().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Birth date cannot be in the future.");
        }
    }

    public static void validateCreateTrainerRequest(CreateTrainer request) {
        if(request == null) {
            throw new IllegalArgumentException("CreateTrainer request cannot be null.");
        }

        if(request.firstName() == null || request.firstName().isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or blank.");
        }
        if(!request.firstName().matches(userNamePattern)){
            throw new IllegalArgumentException("First name contains invalid characters.");
        }

        if (request.lastName() == null || request.lastName().isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or blank.");
        }
        if(!request.lastName().matches(userNamePattern)){
            throw new IllegalArgumentException("Last name contains invalid characters.");
        }

        if (request.specialization() == null) {
            throw new IllegalArgumentException("Specialization cannot be null.");
        }
    }

    public static void validateUpdateTrainerRequest(UpdateTrainer request) {
        if(request == null) {
            throw new IllegalArgumentException("UpdateTrainer request cannot be null.");
        }

        if (request.id() <= 0) {
            throw new IllegalArgumentException("Trainer ID must be a positive number.");
        }

        if (request.firstName() != null && !request.firstName().matches(userNamePattern)) {
            throw new IllegalArgumentException("First name contains invalid characters.");
        }

        if (request.lastName() != null && !request.lastName().matches(userNamePattern)) {
            throw new IllegalArgumentException("Last name contains invalid characters.");
        }
    }

    public static void validateScheduleTrainingRequest(ScheduleTraining request) {
        if(request == null) {
            throw new IllegalArgumentException("ScheduleTraining request cannot be null.");
        }

        if (request.traineeId() <= 0) {
            throw new IllegalArgumentException("Trainee ID must be a positive number.");
        }

        if (request.trainerId() <= 0) {
            throw new IllegalArgumentException("Trainer ID must be a positive number.");
        }

        if (request.trainingName() == null || request.trainingName().isBlank()) {
            throw new IllegalArgumentException("Training name cannot be null or blank.");
        }

        if (request.trainingDate() == null) {
            throw new IllegalArgumentException("Training date cannot be null.");
        }

        if (request.duration() == null || request.duration().isNegative() || request.duration().isZero()) {
            throw new IllegalArgumentException("Training duration must be positive.");
        }
    }
}
