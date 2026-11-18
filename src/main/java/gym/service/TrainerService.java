package gym.service;

import gym.data.entity.TrainerEntity;
import gym.dto.request.CreateTrainer;
import gym.dto.request.UpdateTrainer;
import gym.repository.user.UserRepository;
import gym.service.validation.RequestValidator;
import gym.dto.response.Trainer;
import gym.repository.TrainerRepository;
import gym.service.helper.PasswordGenerator;
import gym.service.mapper.GymMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Slf4j
@Service
public class TrainerService {

    private TrainerRepository trainerRepository;
    private UserRepository userRepository;
    private PasswordGenerator passwordGenerator;
    private GymMapper gymMapper;
    private RequestValidator requestValidator;

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
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }


    public Trainer createTrainer(CreateTrainer request) {
        requestValidator.validateCreateTrainerRequest(request);

        var userEntity = userRepository.add(gymMapper.toUserEntity(request, passwordGenerator.generate()));
        TrainerEntity trainerEntity = trainerRepository.add(gymMapper.toTrainerEntity(request, userEntity));

        var result = gymMapper.toTrainer(trainerEntity, userEntity);
        log.info("createTrainer completed, id={}.", result.id());
        return result;
    }

    public Trainer updateTrainer(UpdateTrainer request) {
        requestValidator.validateUpdateTrainerRequest(request);

        var updatedTrainer = trainerRepository.update(
                request.id(),
                existing -> gymMapper.toUpdatedTrainer(request, existing));
        var updatedUser = userRepository.update(
                request.id(),
                existing -> gymMapper.toUpdatedUser(request, existing));

        var result = gymMapper.toTrainer(updatedTrainer, updatedUser);
        log.info("updateTrainer completed, id={}.", result.id());
        return result;
    }

    public Optional<Trainer> findTrainerById(long id) {
        return userRepository.getById(id).flatMap(userEntity -> trainerRepository.getById(id)
                .map(trainer -> gymMapper.toTrainer(trainer, userEntity)));
    }

    public List<Trainer> getAll() {
        return trainerRepository.getAll().stream()
                .flatMap(trainer -> userRepository.getById(trainer.getId())
                        .map(user -> gymMapper.toTrainer(trainer, user))
                        .stream())
                .toList();
    }

}
