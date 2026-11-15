package gym.services;

import gym.data.entities.TrainerEntity;
import gym.data.entities.common.UserData;
import gym.services.dto.requests.CreateTrainer;
import gym.services.dto.requests.UpdateTrainer;
import gym.services.synchronization.TrainerLocks;
import gym.services.validation.RequestValidator;
import gym.services.dto.responses.Trainer;
import gym.repositories.TrainerRepository;
import gym.services.helpers.UniqueUsernameGenerator;
import gym.services.helpers.PasswordGenerator;
import gym.services.mapper.EntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Trainer operations. Trainers can't be deleted, only deactivated.
 */
@Slf4j
@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final UniqueUsernameGenerator userNameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final TrainerLocks trainerLocks;

    public TrainerService(
            TrainerRepository trainerRepository,
            UniqueUsernameGenerator userNameGenerator,
            PasswordGenerator passwordGenerator,
            TrainerLocks trainerLocks) {
        this.trainerRepository = trainerRepository;
        this.userNameGenerator = userNameGenerator;
        this.passwordGenerator = passwordGenerator;
        this.trainerLocks = trainerLocks;
    }

    /**
     * Creates a new trainer.
     * @param request the request containing trainer details.
     * @return the created trainer, with unique username and assigned ID.
     */
    public Trainer createTrainer(CreateTrainer request) {
        log.info("createTrainer started, specialization={}.", request == null ? null : request.specialization());
        // Check for null or invalid fields in the request.
        RequestValidator.validateCreateTrainerRequest(request);

        // Create unique username. The generator is shared with trainees, so usernames are unique across all users.
        String userName = userNameGenerator.generateUniqueUserName(request.firstName(), request.lastName());

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
        log.info("updateTrainer started, id={}.", request == null ? null : request.id());
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
        log.info("deactivateTrainer started, id={}.", id);
        var lock = trainerLocks.get(id);
        lock.lock();
        try {
            TrainerEntity deactivated = trainerRepository.update(id, trainer -> trainer.withUser(trainer.user().withActive(false)));
            return EntityMapper.toTrainer(deactivated);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Finds a trainer by their ID.
     * @param id
     * @return an Optional containing the Trainer if found, or empty if not found.
     */
    public Optional<Trainer> findTrainerById(long id) {
        log.info("findTrainerById started, id={}.", id);
        return trainerRepository.getById(id).map(EntityMapper::toTrainer);
    }

    public List<Trainer> getAll() {
        log.info("getAll trainers started.");
        return trainerRepository.getAll().stream().map(EntityMapper::toTrainer).toList();
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
