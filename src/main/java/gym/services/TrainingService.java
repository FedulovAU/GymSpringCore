package gym.services;

import gym.data.entities.TraineeEntity;
import gym.data.entities.TrainerEntity;
import gym.data.entities.TrainingEntity;
import gym.services.dto.requests.ScheduleTraining;
import gym.services.synchronization.TraineeLocks;
import gym.services.synchronization.TrainerLocks;
import gym.services.validation.RequestValidator;
import gym.services.dto.responses.Training;
import gym.repositories.TraineeRepository;
import gym.repositories.TrainerRepository;
import gym.repositories.TrainingRepository;
import gym.services.exceptions.CannotScheduleTrainingException;
import gym.services.mapper.EntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
@Service
public class TrainingService {

    private TrainingRepository trainingRepository;
    private TrainerRepository trainerRepository;
    private TraineeRepository traineeRepository;

    private TrainerLocks trainerLocks;
    private TraineeLocks traineeLocks;

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

    @Autowired
    public void setTrainerLocks(TrainerLocks trainerLocks) {
        this.trainerLocks = trainerLocks;
    }

    @Autowired
    public void setTraineeLocks(TraineeLocks traineeLocks) {
        this.traineeLocks = traineeLocks;
    }


    public Training scheduleTraining(ScheduleTraining request) {
        RequestValidator.validateScheduleTrainingRequest(request);

        ReentrantLock trainerLock = trainerLocks.get(request.trainerId());
        ReentrantLock traineeLock = traineeLocks.get(request.traineeId());

        trainerLock.lock();
        try {
            traineeLock.lock();
            try {
                TrainerEntity trainer = findActiveTrainerById(request.trainerId());
                TraineeEntity trainee = findActiveTraineeById(request.traineeId());

                ensureNoOverlap(trainingRepository.getByTrainerId(trainer.id()), request, "Trainer");
                ensureNoOverlap(trainingRepository.getByTraineeId(trainee.id()), request, "Trainee");

                TrainingEntity training = trainingRepository.add(new TrainingEntity(
                        null,
                        trainee.id(),
                        trainer.id(),
                        request.trainingName(),
                        trainer.specialization(),
                        request.trainingDate(),
                        request.duration()));

                var result = EntityMapper.toTraining(training);
                log.info("scheduleTraining completed, id={}, traineeId={}, trainerId={}", result.id(), result.traineeId(), result.trainerId());
                return result;
            } finally {
                traineeLock.unlock();
            }
        } finally {
            trainerLock.unlock();
        }
    }


    public Training removeTrainingById(long id) {
        var result = EntityMapper.toTraining(trainingRepository.deleteById(id));
        log.info("removeTraining completed, id={}, traineeId={}, trainerId={}", result.id(), result.traineeId(), result.trainerId());
        return result;
    }


    public List<Training> getAll() {
        return trainingRepository.getAll().stream().map(EntityMapper::toTraining).toList();
    }

    private TrainerEntity findActiveTrainerById(long id) {
        TrainerEntity trainer = trainerRepository.getById(id)
                .orElseThrow(() -> new CannotScheduleTrainingException("Trainer with ID " + id + " does not exist."));
        if (!trainer.user().isActive()) {
            throw new CannotScheduleTrainingException("Trainer with ID " + id + " is not active.");
        }
        return trainer;
    }

    private TraineeEntity findActiveTraineeById(long id) {
        TraineeEntity trainee = traineeRepository.getById(id)
                .orElseThrow(() -> new CannotScheduleTrainingException("Trainee with ID " + id + " does not exist."));
        if (!trainee.user().isActive()) {
            throw new CannotScheduleTrainingException("Trainee with ID " + id + " is not active.");
        }
        return trainee;
    }


    private static void ensureNoOverlap(List<TrainingEntity> existing, ScheduleTraining request, String who) {
        LocalDateTime newTrainingStart = request.trainingDate();
        LocalDateTime newTrainingEnd = newTrainingStart.plus(request.duration());

        for (TrainingEntity training : existing) {
            LocalDateTime existingStart = training.trainingDate();
            LocalDateTime existingEnd = existingStart.plus(training.trainingDuration());

            if (newTrainingStart.isBefore(existingEnd) && newTrainingEnd.isAfter(existingStart)) {
                throw new CannotScheduleTrainingException(who + " already has a training at that time, training ID "
                        + training.id() + " (" + existingStart + " - " + existingEnd + ").");
            }
        }
    }
}
