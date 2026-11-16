package gym.services;

import gym.data.entities.TrainerEntity;
import gym.data.common.UserData;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Slf4j
@Service
public class TrainerService {

    private TrainerRepository trainerRepository;
    private UniqueUsernameGenerator userNameGenerator;
    private PasswordGenerator passwordGenerator;
    private TrainerLocks trainerLocks;

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
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
    public void setTrainerLocks(TrainerLocks trainerLocks) {
        this.trainerLocks = trainerLocks;
    }


    public Trainer createTrainer(CreateTrainer request) {
        RequestValidator.validateCreateTrainerRequest(request);
        String userName = userNameGenerator.generateUniqueUserName(request.firstName(), request.lastName());

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

        var result = EntityMapper.toTrainer(trainerEntity);
        log.info("createTrainer completed, id={}.", result.id());
        return result;
    }

    public Trainer updateTrainer(UpdateTrainer request) {
        RequestValidator.validateUpdateTrainerRequest(request);
        TrainerEntity updatedTrainer = trainerRepository.update(request.id(), existing -> applyUpdate(existing, request));

        var result = EntityMapper.toTrainer(updatedTrainer);
        log.info("updateTrainer completed, id={}.", result.id());
        return result;
    }

    public Trainer deactivateTrainerById(long id) {
        var lock = trainerLocks.get(id);
        lock.lock();

        try {
            TrainerEntity deactivated = trainerRepository.update(id, trainer -> trainer.withUser(trainer.user().withActive(false)));
            var result = EntityMapper.toTrainer(deactivated);
            log.info("deactivateTrainer completed, id={}.", result.id());
            return result;
        } finally {
            lock.unlock();
        }
    }

    public Optional<Trainer> findTrainerById(long id) {
        return trainerRepository.getById(id).map(EntityMapper::toTrainer);
    }

    public List<Trainer> getAll() {
        return trainerRepository.getAll().stream().map(EntityMapper::toTrainer).toList();
    }


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
