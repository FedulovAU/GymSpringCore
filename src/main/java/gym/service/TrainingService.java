package gym.service;

import gym.data.entity.TraineeEntity;
import gym.data.entity.TrainerEntity;
import gym.data.entity.TrainingEntity;
import gym.dto.request.ScheduleTraining;
import gym.service.validation.RequestValidator;
import gym.dto.response.Training;
import gym.repository.trainee.TraineeRepository;
import gym.repository.trainer.TrainerRepository;
import gym.repository.TrainingRepository;
import gym.service.exception.CannotScheduleTrainingException;
import gym.service.mapper.GymMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
public class TrainingService {

    private TrainingRepository trainingRepository;
    private TrainerRepository trainerRepository;
    private TraineeRepository traineeRepository;
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
    public void setTrainingRepository(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }


    public Training scheduleTraining(ScheduleTraining request) {
        requestValidator.validateScheduleTrainingRequest(request);

        TrainerEntity trainer = findActiveTrainerById(request.trainerId());
        TraineeEntity trainee = findActiveTraineeById(request.traineeId());

        ensureNoOverlap(trainingRepository.getByTrainerId(trainer.getId()), request, "Trainer");
        ensureNoOverlap(trainingRepository.getByTraineeId(trainee.getId()), request, "Trainee");

        TrainingEntity training = trainingRepository.add(
                gymMapper.toTrainingEntity(request, trainee, trainer));

        var result = gymMapper.toTraining(training);
        log.info("scheduleTraining completed, traineeId={}, trainerId={}.", result.traineeId(), result.trainerId());
        return result;
    }


    public List<Training> getAll() {
        return trainingRepository.getAll().stream().map(gymMapper::toTraining).toList();
    }

    private TrainerEntity findActiveTrainerById(long id) {
        TrainerEntity trainer = trainerRepository.getById(id)
                .orElseThrow(() -> new CannotScheduleTrainingException("Trainer with ID " + id + " does not exist."));
        if (!trainer.getUser().isActive()) {
            throw new CannotScheduleTrainingException("Trainer with ID " + id + " is not active.");
        }
        return trainer;
    }

    private TraineeEntity findActiveTraineeById(long id) {
        TraineeEntity trainee = traineeRepository.getById(id)
                .orElseThrow(() -> new CannotScheduleTrainingException("Trainee with ID " + id + " does not exist."));
        if (!trainee.getUser().isActive()) {
            throw new CannotScheduleTrainingException("Trainee with ID " + id + " is not active.");
        }
        return trainee;
    }

    private static void ensureNoOverlap(List<TrainingEntity> existing, ScheduleTraining request, String who) {
        LocalDateTime newTrainingStart = request.trainingDate();
        LocalDateTime newTrainingEnd = newTrainingStart.plus(request.duration());

        for (TrainingEntity training : existing) {
            LocalDateTime existingStart = training.getTrainingDate();
            LocalDateTime existingEnd = existingStart.plus(training.getTrainingDuration());

            if (newTrainingStart.isBefore(existingEnd) && newTrainingEnd.isAfter(existingStart)) {
                throw new CannotScheduleTrainingException(who + " already has a training at that time: '"
                        + training.getTrainingName() + "' (" + existingStart + " - " + existingEnd + ").");
            }
        }
    }
}
