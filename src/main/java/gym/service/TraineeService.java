package gym.service;

import gym.data.entity.TraineeEntity;
import gym.dto.request.CreateTrainee;
import gym.dto.request.UpdateTrainee;
import gym.repository.user.UserRepository;
import gym.service.validation.RequestValidator;
import gym.dto.response.Trainee;
import gym.repository.TraineeRepository;
import gym.service.helper.PasswordGenerator;
import gym.service.mapper.GymMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TraineeService {
    private TraineeRepository traineeRepository;
    private UserRepository userRepository;
    private RequestValidator requestValidator;
    private PasswordGenerator passwordGenerator;
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
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public Trainee createTrainee(CreateTrainee request) {
        requestValidator.validateCreateTraineeRequest(request);

        var userEntity = userRepository.add(gymMapper.toUserEntity(request, passwordGenerator.generate()));
        TraineeEntity traineeEntity = traineeRepository.add(gymMapper.toTraineeEntity(request, userEntity));

        var result = gymMapper.toTrainee(traineeEntity, userEntity);
        log.info("createTrainee completed, id={}.", result.id());
        return result;
    }


    public Trainee updateTrainee(UpdateTrainee request) {
        requestValidator.validateUpdateTraineeRequest(request);

        var updatedTrainee = traineeRepository.update(
                request.id(),
                existing -> gymMapper.toUpdatedTrainee(request, existing));

        var updatedUser = userRepository.update(
                request.id(),
                existing -> gymMapper.toUpdatedUser(request, existing));


        var result = gymMapper.toTrainee(updatedTrainee, updatedUser);
        log.info("updateTrainee completed, id={}.", result.id());
        return result;
    }

    public void deleteTraineeById(long id) {
        traineeRepository.deleteById(id);
        userRepository.deleteById(id);
        log.info("deleteTrainee completed, id={}.", id);
    }


    public Optional<Trainee> findTraineeById(long id) {
        return userRepository.getById(id).flatMap(userEntity -> traineeRepository.getById(id)
                .map(trainee -> gymMapper.toTrainee(trainee, userEntity)));
    }

    public List<Trainee> getAll() {
        return traineeRepository.getAll().stream()
                .flatMap(trainee -> userRepository.getById(trainee.getId())
                        .map(user -> gymMapper.toTrainee(trainee, user))
                        .stream())
                .toList();
    }

}
