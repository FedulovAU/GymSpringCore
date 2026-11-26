package gym.service;

import gym.data.TrainerEntity;
import gym.data.TrainingTypeEntity;
import gym.data.UserEntity;
import gym.enums.TrainingType;
import gym.dto.request.CreateTrainer;
import gym.dto.request.UpdateTrainer;
import gym.dto.response.Credentials;
import gym.dto.response.Trainer;
import gym.repository.TraineeRepository;
import gym.repository.TrainerRepository;
import gym.repository.TrainingTypeRepository;
import gym.service.exception.ProfileNotFoundException;
import gym.service.helper.UserFactory;
import gym.service.mapper.GymMapper;
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

    public TrainerService(
            TrainerRepository trainerRepository,
            TraineeRepository traineeRepository,
            TrainingTypeRepository trainingTypeRepository,
            UserFactory userFactory,
            RequestValidator requestValidator,
            GymMapper gymMapper) {
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.userFactory = userFactory;
        this.requestValidator = requestValidator;
        this.gymMapper = gymMapper;
    }

    @Transactional
    public Credentials createTrainer(CreateTrainer request) {
        requestValidator.validateCreateTrainerRequest(request);

        TrainingTypeEntity specialization = trainingTypeRepository
                .findByType(request.specialization())
                .orElseThrow(() -> new ProfileNotFoundException("Training type '" + request.specialization() + "' not found."));

        UserEntity user = userFactory.createUser(request.firstName(), request.lastName());
        trainerRepository.save(new TrainerEntity(user, specialization));

        log.info("createTrainer completed, username={}.", user.getUsername());
        return gymMapper.toCredentials(user);
    }

    @Transactional(readOnly = true)
    public boolean matchesCredentials(String username, String password) {
        return trainerRepository.findByUsername(username)
                .map(trainer -> trainer.getUser().getPassword().equals(password))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> findByUsername(String username) {
        return trainerRepository.findByUsername(username).map(gymMapper::toTrainer);
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        requestValidator.validatePassword(newPassword);

        getTrainer(username).getUser().setPassword(newPassword);
        log.info("changePassword completed, trainer username={}.", username);
    }

    @Transactional
    public Trainer updateTrainer(UpdateTrainer request) {
        requestValidator.validateUpdateTrainerRequest(request);

        TrainerEntity trainer = getTrainer(request.username());
        Optional<TrainingTypeEntity> specialization = request.specialization() == null
                ? Optional.empty()
                : trainingTypeRepository.findByType(request.specialization());

        gymMapper.applyUpdate(request, trainer, specialization);

        log.info("updateTrainer completed, username={}.", request.username());
        return gymMapper.toTrainer(trainer);
    }

    @Transactional
    public void setActive(String username, boolean isActive) {
        getTrainer(username).getUser().setActive(isActive);
        log.info("setActive completed, trainer username={}, isActive={}.", username, isActive);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername) {
        if (traineeRepository.findByUsername(traineeUsername).isEmpty()) {
            throw new ProfileNotFoundException("Trainee '" + traineeUsername + "' not found.");
        }

        return trainerRepository.findNotAssignedToTrainee(traineeUsername).stream()
                .map(gymMapper::toTrainer)
                .toList();
    }

    private TrainerEntity getTrainer(String username) {
        return trainerRepository.findByUsername(username)
                .orElseThrow(() -> new ProfileNotFoundException("Trainer '" + username + "' not found."));
    }
}
