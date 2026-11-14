package gym.services;

import gym.data.entities.TrainerEntity;
import gym.data.entities.common.UserData;
import gym.services.dto.requests.CreateTrainer;
import gym.services.dto.requests.UpdateTrainer;
import gym.services.validation.RequestValidator;
import gym.services.dto.responses.Trainer;
import gym.repositories.TrainerRepository;
import gym.services.helpers.DuplicateNameGenerator;
import gym.services.helpers.PasswordGenerator;
import gym.services.mapper.EntityMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Trainer operations. Trainers can't be deleted, only deactivated.
 */
@Service
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final DuplicateNameGenerator duplicateNameGenerator;
    private final PasswordGenerator passwordGenerator;

    public TrainerService(
            TrainerRepository trainerRepository,
            DuplicateNameGenerator duplicateNameGenerator,
            PasswordGenerator passwordGenerator) {
        this.trainerRepository = trainerRepository;
        this.duplicateNameGenerator = duplicateNameGenerator;
        this.passwordGenerator = passwordGenerator;
    }

    /**
     * Creates a new trainer.
     * @param request the request containing trainer details.
     * @return the created trainer, with unique username and assigned ID.
     */
    public Trainer createTrainer(CreateTrainer request) {
        // Check for null or invalid fields in the request.
        RequestValidator.validateCreateTrainerRequest(request);

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
                EntityMapper.toTrainingTypeEntity(request.specialization())
        ));

        // Map stored entity to the result.
        return EntityMapper.toTrainer(trainerEntity);
    }

    /**
     * Updates an existing trainer's information.
     * @param request the request containing updated trainer details.
     * The username is the login and stays the same, even if first name or last name is changed.
     * @return the updated trainer.
     */
    public Trainer updateTrainer(UpdateTrainer request) {
        RequestValidator.validateUpdateTrainerRequest(request);

        // Read-modify-write runs atomically inside the storage.
        TrainerEntity updatedTrainer = trainerRepository.update(request.id(), existing -> applyUpdate(existing, request));

        return EntityMapper.toTrainer(updatedTrainer);
    }

    /**
     * Deactivates a trainer by their ID: the trainer stays in storage, but is marked as inactive.
     * @param id the unique identifier of the trainer to be deactivated.
     * @return the deactivated trainer.
     */
    public Trainer deactivateTrainer(long id) {
        TrainerEntity deactivated = trainerRepository.update(id, trainer -> trainer.withUser(trainer.user().withActive(false)));
        return EntityMapper.toTrainer(deactivated);
    }

    /**
     * Finds a trainer by their ID.
     * @param id
     * @return an Optional containing the Trainer if found, or empty if not found.
     */
    public Optional<Trainer> findTrainerById(long id) {
        return trainerRepository.getById(id).map(EntityMapper::toTrainer);
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
            updated = updated.withSpecialization(EntityMapper.toTrainingTypeEntity(request.specialization()));
        }
        return updated;
    }
}
