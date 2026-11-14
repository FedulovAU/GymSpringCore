package gym.services;

import gym.data.entities.TrainerEntity;
import gym.data.entities.common.UserData;
import gym.data.entities.enums.TrainingTypeEntity;
import gym.dto.enums.TrainingType;
import gym.dto.requests.CreateTrainer;
import gym.dto.requests.UpdateTrainer;
import gym.dto.requests.validation.RequestValidator;
import gym.dto.responses.Trainer;
import gym.repositories.TrainerRepository;
import gym.services.helpers.DuplicateNameGenerator;
import gym.services.helpers.PasswordGenerator;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Trainer operations. Trainers can't be deleted, only deactivated.
 */
@Service
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final DuplicateNameGenerator duplicateNameGenerator;
    private final RequestValidator requestValidator;
    private final PasswordGenerator passwordGenerator;

    public TrainerService(
            TrainerRepository trainerRepository,
            DuplicateNameGenerator duplicateNameGenerator,
            RequestValidator requestValidator,
            PasswordGenerator passwordGenerator) {
        this.trainerRepository = trainerRepository;
        this.duplicateNameGenerator = duplicateNameGenerator;
        this.requestValidator = requestValidator;
        this.passwordGenerator = passwordGenerator;
    }

    /**
     * Creates a new trainer.
     * @param request the request containing trainer details.
     * @return the created trainer, with unique username and assigned ID.
     */
    public Trainer createTrainer(CreateTrainer request) {
        // Check for null or invalid fields in the request.
        requestValidator.validateCreateTrainerRequest(request);

        // Create unique username. The generator is shared with trainees, so usernames are unique across all users.
        String userName = duplicateNameGenerator.generateUniqueUserName(request.firstName(), request.lastName());

        // Store new trainer.
        TrainerEntity trainerEntity = trainerRepository.add(new TrainerEntity(
                null,
                new UserData(
                        request.firstName(),
                        request.lastName(),
                        userName,
                        passwordGenerator.generate(),
                        true),
                toTrainingTypeEntity(request.specialization())
        ));

        // Map stored entity to the result.
        return mapTrainer(trainerEntity);
    }

    /**
     * Updates an existing trainer's information.
     * @param request the request containing updated trainer details.
     * The username is the login and stays the same, even if first name or last name is changed.
     * @return the updated trainer.
     */
    public Trainer updateTrainer(UpdateTrainer request) {
        requestValidator.validateUpdateTrainerRequest(request);

        // Read-modify-write runs atomically inside the storage.
        TrainerEntity updatedTrainer = trainerRepository.update(request.id(), existing -> applyUpdate(existing, request));

        return mapTrainer(updatedTrainer);
    }

    /**
     * Deactivates a trainer by their ID: the trainer stays in storage, but is marked as inactive.
     * @param id the unique identifier of the trainer to be deactivated.
     * @return the deactivated trainer.
     */
    public Trainer deactivateTrainer(long id) {
        TrainerEntity deactivated = trainerRepository.update(id, trainer -> trainer.withUser(trainer.user().withActive(false)));
        return mapTrainer(deactivated);
    }

    /**
     * Finds a trainer by their ID.
     * @param id
     * @return an Optional containing the Trainer if found, or empty if not found.
     */
    public Optional<Trainer> findTrainerById(long id) {
        return trainerRepository.getById(id).map(this::mapTrainer);
    }

    /**
     * Applies the provided (non-null) fields of the request to the trainer. Username is never changed.
     */
    private TrainerEntity applyUpdate(TrainerEntity trainer, UpdateTrainer request) {
        var userData = trainer.user();

        if (request.firstName() != null) {
            userData = userData.withFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            userData = userData.withLastName(request.lastName());
        }

        var updated = trainer.withUser(userData);
        if (request.specialization() != null) {
            updated = updated.withSpecialization(toTrainingTypeEntity(request.specialization()));
        }
        return updated;
    }

    /**
     * Maps a TrainerEntity to a Trainer DTO. Avoids exposing sensitive information like password.
     * @param trainer the TrainerEntity to be mapped.
     * @return the corresponding Trainer DTO.
     */
    private Trainer mapTrainer(TrainerEntity trainer) {
        return new Trainer(
                trainer.id(),
                trainer.user().firstName(),
                trainer.user().lastName(),
                trainer.user().userName(),
                trainer.user().isActive(),
                fromTrainingTypeEntity(trainer.specialization()));
    }

    /**
     * Maps this DTO training type to the entity training type.
     */
    public TrainingTypeEntity toTrainingTypeEntity(TrainingType trainingType) {
        return switch (trainingType) {
            case Bodybuilding -> TrainingTypeEntity.Bodybuilding;
            case Crossfit -> TrainingTypeEntity.Crossfit;
            case Fitness -> TrainingTypeEntity.Fitness;
        };
    }

    /**
     * Maps an entity training type to the DTO training type.
     * @param trainingTypeEntity the entity training type, may be null.
     * @return the corresponding DTO training type, or null if entity is null.
     */
    private static TrainingType fromTrainingTypeEntity(TrainingTypeEntity trainingTypeEntity) {
        if (trainingTypeEntity == null) {
            return null;
        }
        return switch (trainingTypeEntity) {
            case Bodybuilding -> TrainingType.Bodybuilding;
            case Crossfit -> TrainingType.Crossfit;
            case Fitness -> TrainingType.Fitness;
        };
    }
}
