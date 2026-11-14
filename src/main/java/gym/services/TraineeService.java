package gym.services;

import gym.data.entities.TraineeEntity;
import gym.data.entities.common.UserData;
import gym.dto.requests.CreateTrainee;
import gym.dto.requests.UpdateTrainee;
import gym.dto.requests.validation.RequestValidator;
import gym.dto.responses.Trainee;
import gym.repositories.TraineeRepository;
import gym.services.helpers.DuplicateNameGenerator;
import gym.services.helpers.PasswordGenerator;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TraineeService {
    private final TraineeRepository traineeRepository;
    private final DuplicateNameGenerator duplicateNameGenerator;
    private final RequestValidator requestValidator;
    private final PasswordGenerator passwordGenerator;

    public TraineeService(
            TraineeRepository traineeRepository,
            DuplicateNameGenerator duplicateNameGenerator,
            RequestValidator requestValidator,
            PasswordGenerator passwordGenerator) {
        this.traineeRepository = traineeRepository;
        this.duplicateNameGenerator = duplicateNameGenerator;
        this.requestValidator = requestValidator;
        this.passwordGenerator = passwordGenerator;
    }

    /**
     * Creates a new trainee.
     * @param request the request containing trainee details.
     * @return the created trainee, with unique username and assigned ID.
     */
    public Trainee createTrainee(CreateTrainee request) {
        // Check for null or invalid fields in the request.
        requestValidator.validateCreateTraineeRequest(request);

        // Create unique username.
        String userName = duplicateNameGenerator.generateUniqueUserName(request.firstName(), request.lastName());

        // Store new trainee.
        TraineeEntity traineeEntity = traineeRepository.add(new TraineeEntity(
                null,
                new UserData(
                        request.firstName(),
                        request.lastName(),
                        userName,
                        passwordGenerator.generate(),
                        true),
                request.birthDate()
        ));

        // Map stored entity to the result.
        return mapTrainee(traineeEntity);
    }

    /**
     * Updates an existing trainee's information.
     * @param request the request containing updated trainee details.
     * The username is the login and stays the same, even if first name or last name is changed.
     * @return the updated trainee.
     */
    public Trainee updateTrainee(UpdateTrainee request) {
        requestValidator.validateUpdateTraineeRequest(request);

        // Read-modify-write runs atomically inside the storage.
        TraineeEntity updatedTrainee = traineeRepository.update(request.id(), existing -> applyUpdate(existing, request));

        return mapTrainee(updatedTrainee);
    }

    /**
     * Soft deletes a trainee by their ID: the trainee stays in storage, but is marked as inactive.
     * @param id the unique identifier of the trainee to be deactivated.
     * @return the deactivated trainee.
     */
    public Trainee deactivateTrainee(long id) {
        TraineeEntity deactivated = traineeRepository.update(id, trainee -> trainee.withUser(trainee.user().withActive(false)));
        return mapTrainee(deactivated);
    }

    /**
     * Applies the provided (non-null) fields of the request to the trainee. Username is never changed.
     */
    private static TraineeEntity applyUpdate(TraineeEntity trainee, UpdateTrainee request) {
        var userData = trainee.user();

        if (request.firstName() != null) {
            userData = userData.withFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            userData = userData.withLastName(request.lastName());
        }

        var updated = trainee.withUser(userData);
        if (request.birthDate() != null) {
            updated = updated.withBirthDate(request.birthDate());
        }
        return updated;
    }

    /**
     * Finds a trainee by their ID.
     * @param id
     * @return an Optional containing the Trainee if found, or empty if not found.
     */
    public Optional<Trainee> findTraineeById(long id) {
        return traineeRepository.getById(id).map(this::mapTrainee);
    }

    /**
     * Maps a TraineeEntity to a Trainee DTO. Avoids exposing sensitive information like password.
     * @param trainee the TraineeEntity to be mapped.
     * @return the corresponding Trainee DTO.
     */
    private Trainee mapTrainee(TraineeEntity trainee) {
        return new Trainee(
                trainee.id(),
                trainee.user().firstName(),
                trainee.user().lastName(),
                trainee.user().userName(),
                trainee.user().isActive(),
                trainee.birthDate());
    }
}
