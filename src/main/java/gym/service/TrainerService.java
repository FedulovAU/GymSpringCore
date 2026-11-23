package gym.service;

import gym.data.entity.TrainerEntity;
import gym.dto.request.CreateTrainer;
import gym.dto.request.UpdateTrainer;
import gym.repository.trainee.TraineeRepository;
import gym.service.helper.UsernameGenerator;
import gym.service.validation.RequestValidator;
import gym.dto.response.Trainer;
import gym.repository.trainer.TrainerRepository;
import gym.service.helper.PasswordGenerator;
import gym.service.mapper.GymMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


@Slf4j
@Service
public class TrainerService {

    private TrainerRepository trainerRepository;
    private TraineeRepository traineeRepository;
    private PasswordGenerator passwordGenerator;
    private GymMapper gymMapper;
    private RequestValidator requestValidator;
    private UsernameGenerator usernameGenerator;

    @Autowired
    public void setGymMapper(GymMapper gymMapper) {
        this.gymMapper = gymMapper;
    }

    @Autowired
    public void setRequestValidator(RequestValidator requestValidator) {
        this.requestValidator = requestValidator;
    }

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    public Trainer createTrainer(CreateTrainer request) {
        requestValidator.validateCreateTrainerRequest(request);

        var lastSuffix = getLastUsernameSuffix(request.firstName(), request.lastName());
        var usernameResult = usernameGenerator.generate(request.firstName(), request.lastName(), lastSuffix);

        TrainerEntity trainerEntity = trainerRepository.add(
                gymMapper.toTrainerEntity(request, usernameResult.username(), usernameResult.suffix(), passwordGenerator.generate()));;

        var result = gymMapper.toTrainer(trainerEntity);
        log.info("createTrainer completed, id={}.", result.id());
        return result;
    }

    public Trainer updateTrainer(UpdateTrainer request) {
        requestValidator.validateUpdateTrainerRequest(request);

        var updatedTrainer = trainerRepository.update(
                request.id(),
                existing -> gymMapper.toUpdatedTrainer(request, existing));

        var result = gymMapper.toTrainer(updatedTrainer);
        log.info("updateTrainer completed, id={}.", result.id());
        return result;
    }

    public Optional<Trainer> findTrainerById(long id) {
        return trainerRepository.getById(id).map(gymMapper::toTrainer);
    }

    public List<Trainer> getAll() {
        return trainerRepository.getAll().stream().map(gymMapper::toTrainer).toList();
    }

    private Optional<Long> getLastUsernameSuffix(String firstName, String lastName) {
        return Stream.of(
                        traineeRepository.getLastUsernameSuffix(firstName, lastName),
                        trainerRepository.getLastUsernameSuffix(firstName, lastName))
                .flatMap(Optional::stream)
                .max(Long::compare);
    }
}
