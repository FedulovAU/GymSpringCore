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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Schedules, removes and lists training sessions.
 * For scheduling checks for schedule overlaps for both trainer and trainee.
 * Thread safe: locks per trainer and trainee to avoid race.
 */
@Slf4j
@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;

    private final TrainerLocks trainerLocks;
    private final TraineeLocks traineeLocks;

    public TrainingService(
            TrainingRepository trainingRepository,
            TrainerRepository trainerRepository,
            TraineeRepository traineeRepository,
            TrainerLocks trainerLocks,
            TraineeLocks traineeLocks) {

        this.trainingRepository = trainingRepository;
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.traineeLocks = traineeLocks;
        this.trainerLocks = trainerLocks;
    }

    /**
     * Schedules a new training. The training type is the trainer's specialization.
     * @param request the request containing training details.
     * @return the scheduled training, with assigned ID.
     * @throws CannotScheduleTrainingException if the trainee or trainer doesn't exist or is inactive,
     * or the training overlaps with an already scheduled training of the same trainer or trainee.
     */
    public Training scheduleTraining(ScheduleTraining request) {
        log.info("scheduleTraining started, request={}.", request);
        RequestValidator.validateScheduleTrainingRequest(request);

        // Lock order is always trainer first, then trainee, so two calls can't deadlock.
        ReentrantLock trainerLock = trainerLocks.get(request.trainerId());
        ReentrantLock traineeLock = traineeLocks.get(request.traineeId());
        trainerLock.lock();
        try {
            traineeLock.lock();
            try {
                TrainerEntity trainer = findActiveTrainer(request.trainerId());
                TraineeEntity trainee = findActiveTrainee(request.traineeId());

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

                return EntityMapper.toTraining(training);
            } finally {
                traineeLock.unlock();
            }
        } finally {
            trainerLock.unlock();
        }
    }

    /**
     * Removes a training by its ID.
     * @param id the unique identifier of the training to remove.
     * @throws gym.data.storage.exceptions.ItemNotFoundException if the training doesn't exist.
     * @return the removed training.
     */
    public Training removeTraining(long id) {
        log.info("removeTraining started, id={}.", id);
        return EntityMapper.toTraining(trainingRepository.deleteById(id));
    }

    /**
     * @return all trainings.
     */
    public List<Training> getAll() {
        log.info("getAll trainings started.");
        return trainingRepository.getAll().stream().map(EntityMapper::toTraining).toList();
    }

    private TrainerEntity findActiveTrainer(long id) {
        TrainerEntity trainer = trainerRepository.getById(id)
                .orElseThrow(() -> new CannotScheduleTrainingException("Trainer with ID " + id + " does not exist."));
        if (!trainer.user().isActive()) {
            throw new CannotScheduleTrainingException("Trainer with ID " + id + " is not active.");
        }
        return trainer;
    }

    private TraineeEntity findActiveTrainee(long id) {
        TraineeEntity trainee = traineeRepository.getById(id)
                .orElseThrow(() -> new CannotScheduleTrainingException("Trainee with ID " + id + " does not exist."));
        if (!trainee.user().isActive()) {
            throw new CannotScheduleTrainingException("Trainee with ID " + id + " is not active.");
        }
        return trainee;
    }

    /**
     * Throws if the requested time slot overlaps with any of the existing trainings.
     */
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
