package gym.services;

import gym.data.entities.TraineeEntity;
import gym.dto.requests.CreateTrainee;
import gym.dto.responses.Trainee;
import gym.repositories.TraineeRepository;
import gym.services.helpers.DuplicateNameGenerator;
import org.springframework.stereotype.Service;

@Service
public class TraineeService {
    private final TraineeRepository traineeRepository;
    private final String userNamePattern = "\\p{L}+";

    private final DuplicateNameGenerator duplicateNameGenerator;

    public TraineeService(
            TraineeRepository traineeRepository,
            DuplicateNameGenerator duplicateNameGenerator) {
        this.traineeRepository = traineeRepository;
        this.duplicateNameGenerator = duplicateNameGenerator;
    }

    public Trainee CreateTrainee(CreateTrainee request) {
        // Check for null or invalid fields in the request.
        validateCreateTraineeRequest(request);

        // Create unique username.
        String userName = duplicateNameGenerator.generateUniqueUserName(request.firstName(), request.lastName());

        // Store new trainee.
        TraineeEntity traineeEntity = traineeRepository.addTrainee(new gym.data.entities.TraineeEntity(
                null,
                new gym.data.entities.User(
                        request.firstName(),
                        request.lastName(),
                        userName,
                        request.password(),
                        true),
                request.birthDate()
        ));

        // Map stored entity to the result.
        return new Trainee(
                traineeEntity.id(),
                traineeEntity.user().firstName(),
                traineeEntity.user().lastName(),
                traineeEntity.user().userName(),
                traineeEntity.user().isActive(),
                traineeEntity.birthDate());
    }

    private void validateCreateTraineeRequest(CreateTrainee request) {
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

        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank.");
        }
        if (request.birthDate() == null) {
            throw new IllegalArgumentException("Birth date cannot be null.");
        }

        if(request.birthDate().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Birth date cannot be in the future.");
        }
    }
}
