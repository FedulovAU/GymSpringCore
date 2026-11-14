package gym.dto.requests.validation;

import gym.dto.requests.CreateTrainee;
import gym.dto.requests.CreateTrainer;
import gym.dto.requests.UpdateTrainee;
import gym.dto.requests.UpdateTrainer;
import org.springframework.stereotype.Component;

@Component
public class RequestValidator {
    private final static String userNamePattern = "\\p{L}+";

    public void validateUpdateTraineeRequest(UpdateTrainee request) {
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


    public void validateCreateTraineeRequest(CreateTrainee request) {
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

    public void validateCreateTrainerRequest(CreateTrainer request) {
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

    public void validateUpdateTrainerRequest(UpdateTrainer request) {
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
}
