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
        TraineeEntity traineeEntity = traineeRepository.add(new gym.data.entities.TraineeEntity(
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
     * @return the updated trainee, with unique username if first name or last name was changed.
     */
    public Trainee updateTrainee(UpdateTrainee request) {
        requestValidator.validateUpdateTraineeRequest(request);

        // Retrieve existing trainee.
        TraineeEntity existingTrainee = traineeRepository.getById(request.id())
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found with ID: " + request.id()));

        TraineeEntity updatedTrainee = existingTrainee;
        var updatedUserData = existingTrainee.user();

        // Update fields if provided.
        boolean wasUserNameUpdated = false;

        if (request.firstName() != null && !request.firstName().equals(updatedUserData.firstName())) {
            updatedUserData = updatedUserData.withFirstName(request.firstName());
            wasUserNameUpdated = true;
        }
        if (request.lastName() != null && !request.lastName().equals(updatedUserData.lastName())) {
            updatedUserData = updatedUserData.withLastName(request.lastName());
            wasUserNameUpdated = true;
        }

        // If first name or last name was updated, generate a new unique username.
        if(wasUserNameUpdated) {
            // Generate a new unique username if first name or last name has changed.
            String newUserName = duplicateNameGenerator.generateUniqueUserName(updatedUserData.firstName(), updatedUserData.lastName());
            updatedUserData = updatedUserData.withUserName(newUserName);
        }
        updatedTrainee = updatedTrainee.withUser(updatedUserData);

        // Update birth date if needed.
        if (request.birthDate() != null) {
            updatedTrainee = updatedTrainee.withBirthDate(request.birthDate());
        }

        // Save updated trainee.
        traineeRepository.updateTrainee(updatedTrainee);

        // Map updated entity to the result.
        return mapTrainee(updatedTrainee);
    }

    /**
     * Deactivates a trainee by their ID.
     * @param id the unique identifier of the trainee to be deactivated.
     */
    public Trainee deactivateTrainee(long id) {
        return mapTrainee(traineeRepository.deactivateById(id));
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
