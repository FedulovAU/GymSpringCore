package gym.services;

import gym.data.entities.TraineeEntity;
import gym.data.common.UserData;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TraineeService {

    private TrainingRepository trainingRepository;
    private TraineeRepository traineeRepository;
    private UniqueUsernameGenerator userNameGenerator;
    private PasswordGenerator passwordGenerator;
    private TraineeLocks traineeLocks;

    @Autowired
    public void setTrainingRepository(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setUserNameGenerator(UniqueUsernameGenerator userNameGenerator) {
        this.userNameGenerator = userNameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Autowired
    public void setTraineeLocks(TraineeLocks traineeLocks) {
        this.traineeLocks = traineeLocks;
    }

    public Trainee createTrainee(CreateTrainee request) {
        RequestValidator.validateCreateTraineeRequest(request);
        String userName = userNameGenerator.generateUniqueUserName(request.firstName(), request.lastName());

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

        var result = EntityMapper.toTrainee(traineeEntity);
        log.info("createTrainee completed, id={}.", result.id());
        return result;
    }


    public Trainee updateTrainee(UpdateTrainee request) {
        RequestValidator.validateUpdateTraineeRequest(request);

        TraineeEntity updatedTrainee = traineeRepository.update(request.id(), existing -> applyUpdate(existing, request));

        var result = EntityMapper.toTrainee(updatedTrainee);
        log.info("updateTrainee completed, id={}.", result.id());
        return result;
    }


    public Trainee deactivateTraineeById(long id) {
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

    public Trainee deleteTraineeById(long id) {
        var lock = traineeLocks.get(id);
        lock.lock();

        try {
            trainingRepository.getByTraineeId(id).forEach(training -> {
                trainingRepository.deleteById(training.id());
                log.info("deleted training id={} caused by deletion of trainee id={}", training.id(), id);
            });

            // Delete trainee the last, so if some training is impossible to delete, trainee remains, and we can retry and do clean up.
            // Otherwise, we may delete trainee, but not all trainings, leaving them orphaned.
            TraineeEntity deleted = traineeRepository.deleteById(id);
            var result = EntityMapper.toTrainee(deleted);
            log.info("deleteTrainee completed, id={}.", result.id());
            return result;
        } finally {
            lock.unlock();
        }
    }


    public Optional<Trainee> findTraineeById(long id) {
        return traineeRepository.getById(id).map(EntityMapper::toTrainee);
    }

    public List<Trainee> getAll() {
        return traineeRepository.getAll().stream().map(EntityMapper::toTrainee).toList();
    }

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
