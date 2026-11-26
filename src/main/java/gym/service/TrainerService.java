package gym.service;

import gym.data.TraineeEntity;
import gym.data.TrainerEntity;
import gym.data.TrainingTypeEntity;
import gym.data.UserEntity;
import gym.dto.request.CreateTrainer;
import gym.dto.request.UpdateTrainer;
import gym.dto.response.Trainer;
import gym.repository.TraineeRepository;
import gym.repository.TrainerRepository;
import gym.repository.TrainingTypeRepository;
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

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserFactory userFactory;
    private final RequestValidator requestValidator;
    private final GymMapper gymMapper;
    private final AuthenticationContext authenticationContext;

    public TrainerService(
            TrainerRepository trainerRepository,
            TraineeRepository traineeRepository,
            TrainingTypeRepository trainingTypeRepository,
            UserFactory userFactory,
            RequestValidator requestValidator,
            GymMapper gymMapper,
            AuthenticationContext authenticationContext) {
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.userFactory = userFactory;
        this.requestValidator = requestValidator;
        this.gymMapper = gymMapper;
        this.authenticationContext = authenticationContext;
    }

    @Transactional
    public Trainer createTrainer(CreateTrainer request) {
        requestValidator.validateCreateTrainerRequest(request);

        TrainingTypeEntity specialization = trainingTypeRepository
                .findByType(request.specialization())
                .orElseThrow(() -> new ProfileNotFoundException("Training type '" + request.specialization() + "' not found."));

        UserEntity user = userFactory.createUser(request.firstName(), request.lastName(), request.password());
        var trainerEntity = trainerRepository.save(new TrainerEntity(user, specialization));

        log.info("createTrainer completed, trainerId={}.", trainerEntity.getId());
        return gymMapper.toTrainer(trainerEntity);
    }

    @Transactional(readOnly = true)
    public boolean matchesCredentials(String username, String password) {
        return trainerRepository.findByUsername(username)
                .map(trainer -> trainer.getUser().getPassword().equals(password))
                .orElse(false);
    }

    @Transactional
    public void signOut() {
        TrainerEntity trainer = getTrainer(authenticationContext.getUserByRole(UserRole.TRAINER));
        authenticationContext.signOut();
        log.info("signOut completed, trainerId={}.", trainer.getId());
    }

    @Transactional(readOnly = true)
    public void signIn(String username, String password) {
        TrainerEntity trainer = trainerRepository.findByUsername(username)
                .filter(found -> found.getUser().getPassword().equals(password))
                .orElseThrow(() -> new AuthenticationException("Invalid trainer username or password."));

        authenticationContext.signIn(new AuthenticatedUser(username, UserRole.TRAINER));
        log.info("signIn completed, trainerId={}.", trainer.getId());
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> findByUsername(String username) {
        authenticationContext.requireSignedIn();

        return trainerRepository.findByUsername(username).map(gymMapper::toTrainer);
    }

    @Transactional
    public void changePassword(String newPassword) {
        String username = authenticationContext.getUserByRole(UserRole.TRAINER);
        requestValidator.validatePassword(newPassword);

        TrainerEntity trainer = getTrainer(username);
        trainer.getUser().setPassword(newPassword);
        log.info("changePassword completed, trainerId={}.", trainer.getId());
    }

    @Transactional
    public Trainer updateTrainer(UpdateTrainer request) {
        String username = authenticationContext.getUserByRole(UserRole.TRAINER);
        requestValidator.validateUpdateTrainerRequest(request);

        TrainerEntity trainer = getTrainer(username);
        Optional<TrainingTypeEntity> specialization = request.specialization() == null
                ? Optional.empty()
                : trainingTypeRepository.findByType(request.specialization());

        gymMapper.applyUpdate(request, trainer, specialization);

        log.info("updateTrainer completed, trainerId={}.", trainer.getId());
        return gymMapper.toTrainer(trainer);
    }

    @Transactional
    public boolean switchActive() {
        String username = authenticationContext.getUserByRole(UserRole.TRAINER);

        TrainerEntity trainer = getTrainer(username);
        boolean isActive = !trainer.getUser().isActive();
        trainer.getUser().setActive(isActive);
        log.info("switchActive completed, trainerId={}, isActive={}.", trainer.getId(), isActive);
        return isActive;
    }

    @Transactional(readOnly = true)
    public List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername) {
        authenticationContext.requireSignedIn();

        if (traineeRepository.findByUsername(traineeUsername).isEmpty()) {
            throw new ProfileNotFoundException("Trainee not found.");
        }

        return trainerRepository.findNotAssignedToTrainee(traineeUsername).stream()
                .map(gymMapper::toTrainer)
                .toList();
    }

    private TrainerEntity getTrainer(String username) {
        return trainerRepository.findByUsername(username)
                .orElseThrow(() -> new ProfileNotFoundException("Trainer not found."));
    }
}
