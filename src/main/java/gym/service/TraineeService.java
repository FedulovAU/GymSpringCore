package gym.service;

import gym.data.TraineeEntity;
import gym.data.UserEntity;
import gym.dto.request.CreateTrainee;
import gym.dto.request.UpdateTrainee;
import gym.dto.response.Trainee;
import gym.repository.TraineeRepository;
import gym.service.exception.AuthenticationException;
import gym.service.exception.ProfileNotFoundException;
import gym.service.helper.UserFactory;
import gym.service.mapper.GymMapper;
import gym.service.security.AuthenticatedUser;
import gym.service.security.AuthenticationContext;
import gym.service.security.UserRole;
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
    private final AuthenticationContext authenticationContext;

    public TraineeService(
            TraineeRepository traineeRepository,
            UserFactory userFactory,
            RequestValidator requestValidator,
            GymMapper gymMapper,
            AuthenticationContext authenticationContext) {
        this.traineeRepository = traineeRepository;
        this.userFactory = userFactory;
        this.requestValidator = requestValidator;
        this.gymMapper = gymMapper;
        this.authenticationContext = authenticationContext;
    }

    @Transactional
    public Trainee createTrainee(CreateTrainee request) {
        requestValidator.validateCreateTraineeRequest(request);

        UserEntity user = userFactory.createUser(request.firstName(), request.lastName(), request.password());
        var traineeEntity = traineeRepository.save(new TraineeEntity(user, request.dateOfBirth(), request.address()));

        log.info("createTrainee completed, traineeId={}.", traineeEntity.getId());
        return gymMapper.toTrainee(traineeEntity);
    }

    @Transactional(readOnly = true)
    public boolean matchesCredentials(String username, String password) {
        return traineeRepository.findByUsername(username)
                .map(trainee -> trainee.getUser().getPassword().equals(password))
                .orElse(false);
    }

    @Transactional
    public void signOut() {
        TraineeEntity trainee = getTrainee(authenticationContext.getUserByRole(UserRole.TRAINEE));
        authenticationContext.signOut();
        log.info("signOut completed, traineeId={}.", trainee.getId());
    }

    @Transactional(readOnly = true)
    public void signIn(String username, String password) {
        TraineeEntity trainee = traineeRepository.findByUsername(username)
                .filter(found -> found.getUser().getPassword().equals(password))
                .orElseThrow(() -> new AuthenticationException("Invalid trainee username or password."));

        authenticationContext.signIn(new AuthenticatedUser(username, UserRole.TRAINEE));
        log.info("signIn completed, traineeId={}.", trainee.getId());
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> findByUsername(String username) {
        authenticationContext.requireSignedIn();

        return traineeRepository.findByUsername(username).map(gymMapper::toTrainee);
    }

    @Transactional
    public void changePassword(String newPassword) {
        String username = authenticationContext.getUserByRole(UserRole.TRAINEE);
        requestValidator.validatePassword(newPassword);

        TraineeEntity trainee = getTrainee(username);
        trainee.getUser().setPassword(newPassword);
        log.info("changePassword completed, traineeId={}.", trainee.getId());
    }

    @Transactional
    public Trainee updateTrainee(UpdateTrainee request) {
        String username = authenticationContext.getUserByRole(UserRole.TRAINEE);
        requestValidator.validateUpdateTraineeRequest(request);

        TraineeEntity trainee = getTrainee(username);
        gymMapper.applyUpdate(request, trainee);

        log.info("updateTrainee completed, traineeId={}.", trainee.getId());
        return gymMapper.toTrainee(trainee);
    }

    @Transactional
    public boolean switchActive() {
        String username = authenticationContext.getUserByRole(UserRole.TRAINEE);

        TraineeEntity trainee = getTrainee(username);
        boolean isActive = !trainee.getUser().isActive();
        trainee.getUser().setActive(isActive);
        log.info("switchActive completed, traineeId={}, isActive={}.", trainee.getId(), isActive);
        return isActive;
    }

    @Transactional
    public void deleteProfile() {
        String username = authenticationContext.getUserByRole(UserRole.TRAINEE);

        TraineeEntity trainee = getTrainee(username);
        traineeRepository.delete(trainee);
        authenticationContext.signOut();
        log.info("deleteProfile completed, traineeId={}.", trainee.getId());
    }

    private TraineeEntity getTrainee(String username) {
        return traineeRepository.findByUsername(username)
                .orElseThrow(() -> new ProfileNotFoundException("Trainee not found."));
    }
}
