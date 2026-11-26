package gym.service;

import gym.data.TraineeEntity;
import gym.data.UserEntity;
import gym.dto.request.CreateTrainee;
import gym.dto.request.UpdateTrainee;
import gym.dto.response.Credentials;
import gym.dto.response.Trainee;
import gym.repository.TraineeRepository;
import gym.service.exception.ProfileNotFoundException;
import gym.service.helper.UserFactory;
import gym.service.mapper.GymMapper;
import gym.service.validation.RequestValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class TraineeService {
    private final TraineeRepository traineeRepository;
    private final UserFactory userFactory;
    private final RequestValidator requestValidator;
    private final GymMapper gymMapper;

    public TraineeService(
            TraineeRepository traineeRepository,
            UserFactory userFactory,
            RequestValidator requestValidator,
            GymMapper gymMapper) {
        this.traineeRepository = traineeRepository;
        this.userFactory = userFactory;
        this.requestValidator = requestValidator;
        this.gymMapper = gymMapper;
    }

    @Transactional
    public Credentials createTrainee(CreateTrainee request) {
        requestValidator.validateCreateTraineeRequest(request);

        UserEntity user = userFactory.createUser(request.firstName(), request.lastName());
        traineeRepository.save(new TraineeEntity(user, request.dateOfBirth(), request.address()));

        log.info("createTrainee completed, username={}.", user.getUsername());
        return gymMapper.toCredentials(user);
    }

    @Transactional(readOnly = true)
    public boolean matchesCredentials(String username, String password) {
        return traineeRepository.findByUsername(username)
                .map(trainee -> trainee.getUser().getPassword().equals(password))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> findByUsername(String username) {
        return traineeRepository.findByUsername(username).map(gymMapper::toTrainee);
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        requestValidator.validatePassword(newPassword);

        getTrainee(username).getUser().setPassword(newPassword);
        log.info("changePassword completed, trainee username={}.", username);
    }

    @Transactional
    public Trainee updateTrainee(UpdateTrainee request) {
        requestValidator.validateUpdateTraineeRequest(request);

        TraineeEntity trainee = getTrainee(request.username());
        gymMapper.applyUpdate(request, trainee);

        log.info("updateTrainee completed, username={}.", request.username());
        return gymMapper.toTrainee(trainee);
    }

    @Transactional
    public void setActive(String username, boolean isActive) {
        getTrainee(username).getUser().setActive(isActive);
        log.info("setActive completed, trainee username={}, isActive={}.", username, isActive);
    }

    @Transactional
    public void deleteByUsername(String username) {
        traineeRepository.delete(getTrainee(username));
        log.info("deleteByUsername completed, trainee username={}.", username);
    }

    private TraineeEntity getTrainee(String username) {
        return traineeRepository.findByUsername(username)
                .orElseThrow(() -> new ProfileNotFoundException("Trainee '" + username + "' not found."));
    }
}
