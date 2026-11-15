package gym.services;

import gym.data.entities.TraineeEntity;
import gym.data.entities.common.UserData;
import gym.repositories.TrainingRepository;
import gym.services.dto.requests.CreateTrainee;
import gym.services.dto.requests.UpdateTrainee;
import gym.services.synchronization.TraineeLocks;
import gym.services.validation.RequestValidator;
import gym.services.dto.responses.Trainee;
import gym.repositories.TraineeRepository;
import gym.services.helpers.UniqueUsernameGenerator;
import gym.services.helpers.PasswordGenerator;
import gym.services.mapper.EntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TraineeService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final UniqueUsernameGenerator userNameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final TraineeLocks traineeLocks;

    public TraineeService(
            TrainingRepository trainingRepository,
            TraineeRepository traineeRepository,
            UniqueUsernameGenerator userNameGenerator,
            PasswordGenerator passwordGenerator,
            TraineeLocks traineeLocks) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.userNameGenerator = userNameGenerator;
        this.passwordGenerator = passwordGenerator;
        this.traineeLocks = traineeLocks;
    }

    /**
     * Creates a new trainee.
     * @param request the request containing trainee details.
     * @return the created trainee, with unique username and assigned ID.
     */
    public Trainee createTrainee(CreateTrainee request) {
        // Check for null or invalid fields in the request.
        RequestValidator.validateCreateTraineeRequest(request);

        // Create unique username.
        String userName = userNameGenerator.generateUniqueUserName(request.firstName(), request.lastName());

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
        var result = EntityMapper.toTrainee(traineeEntity);
        log.info("createTrainee completed, id={}.", result.id());
        return result;
    }

    /**
     * Updates an existing trainee's information.
     * @param request the request containing updated trainee details.
     * The username is the login and stays the same, even if first name or last name is changed.
     * @return the updated trainee.
     */
    public Trainee updateTrainee(UpdateTrainee request) {
        RequestValidator.validateUpdateTraineeRequest(request);

        // Read-modify-write runs atomically inside the storage.
        TraineeEntity updatedTrainee = traineeRepository.update(request.id(), existing -> applyUpdate(existing, request));

        var result = EntityMapper.toTrainee(updatedTrainee);
        log.info("updateTrainee completed, id={}.", result.id());
        return result;
    }

    /**
     * Soft deletes a trainee by their ID: the trainee stays in storage, but is marked as inactive.
     * @param id the unique identifier of the trainee to be deactivated.
     * @return the deactivated trainee.
     */
    public Trainee deactivateTrainee(long id) {
        var lock = traineeLocks.get(id);
        lock.lock();
        try{
            TraineeEntity deactivated = traineeRepository.update(id, trainee -> trainee.withUser(trainee.user().withActive(false)));
            var result = EntityMapper.toTrainee(deactivated);
            log.info("deactivateTrainee completed, id={}.", result.id());
            return result;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Deletes a trainee by their ID, removes all training sessions for that trainee.
     * @param id
     * @return
     */
    public Trainee deleteTrainee(long id) {
        var lock = traineeLocks.get(id);
        lock.lock();
        try {
            trainingRepository.getByTraineeId(id).forEach(training -> {
                trainingRepository.deleteById(training.id());
                log.info("deleted training id={} caused by deletion of trainee id={}", training.id(), id);
            });
            // Delete trainee the last, so if some training is impossible to delete, trainee remains, and we can retry and do clean up.
            // Otherwise, we may delete trainee, but not all trainings, leaving them orphaned.
            TraineeEntity deleted = traineeRepository.delete(id);
            var result = EntityMapper.toTrainee(deleted);
            log.info("deleteTrainee completed, id={}.", result.id());
            return result;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Finds a trainee by their ID.
     * @param id
     * @return an Optional containing the Trainee if found, or empty if not found.
     */
    public Optional<Trainee> findTraineeById(long id) {
        return traineeRepository.getById(id).map(EntityMapper::toTrainee);
    }

    public List<Trainee> getAll() {
        return traineeRepository.getAll().stream().map(EntityMapper::toTrainee).toList();
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
}
