package gym.service;

import gym.data.entity.TraineeEntity;
import gym.dto.request.CreateTrainee;
import gym.dto.request.UpdateTrainee;
import gym.repository.trainer.TrainerRepository;
import gym.service.helper.UsernameGenerator;
import gym.service.validation.RequestValidator;
import gym.dto.response.Trainee;
import gym.repository.trainee.TraineeRepository;
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
public class TraineeService {
    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;
    private RequestValidator requestValidator;
    private PasswordGenerator passwordGenerator;
    private UsernameGenerator usernameGenerator;
    private GymMapper gymMapper;

    @Autowired
    public void setGymMapper(GymMapper gymMapper) {
        this.gymMapper = gymMapper;
    }

    @Autowired
    public void setRequestValidator(RequestValidator requestValidator) {
        this.requestValidator = requestValidator;
    }

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    public Trainee createTrainee(CreateTrainee request) {
        requestValidator.validateCreateTraineeRequest(request);

        var lastSuffix = getLastUsernameSuffix(request.firstName(), request.lastName());
        var usernameResult = usernameGenerator.generate(request.firstName(), request.lastName(), lastSuffix);

        TraineeEntity traineeEntity = traineeRepository.add(
                gymMapper.toTraineeEntity(request, usernameResult.username(), usernameResult.suffix(), passwordGenerator.generate()));

        var result = gymMapper.toTrainee(traineeEntity);
        log.info("createTrainee completed, id={}.", result.id());
        return result;
    }

    public Trainee updateTrainee(UpdateTrainee request) {
        requestValidator.validateUpdateTraineeRequest(request);

        var updatedTrainee = traineeRepository.update(
                request.id(),
                existing -> gymMapper.toUpdatedTrainee(request, existing));

        var result = gymMapper.toTrainee(updatedTrainee);
        log.info("updateTrainee completed, id={}.", result.id());
        return result;
    }

    public void deleteTraineeById(long id) {
        traineeRepository.deleteById(id);
        log.info("deleteTrainee completed, id={}.", id);
    }

    public Optional<Trainee> findTraineeById(long id) {
        return traineeRepository.getById(id).map(gymMapper::toTrainee);
    }

    public List<Trainee> getAll() {
        return traineeRepository.getAll().stream().map(gymMapper::toTrainee).toList();
    }

    private Optional<Long> getLastUsernameSuffix(String firstName, String lastName) {
        return Stream.of(
                        traineeRepository.getLastUsernameSuffix(firstName, lastName),
                        trainerRepository.getLastUsernameSuffix(firstName, lastName))
                .flatMap(Optional::stream)
                .max(Long::compare);
    }
}
