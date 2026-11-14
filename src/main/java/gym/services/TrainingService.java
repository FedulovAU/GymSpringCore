package gym.services;

import gym.data.entities.TraineeEntity;
import gym.data.entities.TrainerEntity;
import gym.data.entities.TrainingEntity;
import gym.data.entities.enums.TrainingTypeEntity;
import gym.dto.enums.TrainingType;
import gym.dto.requests.ScheduleTraining;
import gym.dto.requests.validation.RequestValidator;
import gym.dto.responses.Training;
import gym.repositories.TraineeRepository;
import gym.repositories.TrainerRepository;
import gym.repositories.TrainingRepository;
import gym.services.exceptions.CannotScheduleTrainingException;
import gym.services.helpers.KeyedLocks;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Schedules, removes and lists training sessions.
 * For scheduling checks for schedule overlaps for both trainer and trainee.
 * Thread safe: locks per trainer and trainee to avoid race.
 */
@Service
public class TrainingService {
    private final TrainingRepository trainingRepository;
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final RequestValidator requestValidator;

    private final KeyedLocks trainerLocks = new KeyedLocks();
    private final KeyedLocks traineeLocks = new KeyedLocks();

    public TrainingService(
            TrainingRepository trainingRepository,
            TrainerRepository trainerRepository,
            TraineeRepository traineeRepository,
            RequestValidator requestValidator) {
        this.trainingRepository = trainingRepository;
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.requestValidator = requestValidator;
    }

    /**
     * Schedules a new training. The training type is the trainer's specialization.
     * @param request the request containing training details.
     * @return the scheduled training, with assigned ID.
     * @throws CannotScheduleTrainingException if the trainee or trainer doesn't exist or is inactive,
     * or the training overlaps with an already scheduled training of the same trainer or trainee.
     */
    public Training scheduleTraining(ScheduleTraining request) {
        requestValidator.validateScheduleTrainingRequest(request);

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

                return mapTraining(training);
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
     */
    public void removeTraining(long id) {
        trainingRepository.delete(id);
    }

    /**
     * @return all trainings.
     */
    public List<Training> getAll() {
        return trainingRepository.getAll().stream().map(this::mapTraining).toList();
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

    private Training mapTraining(TrainingEntity training) {
        return new Training(
                training.id(),
                training.traineeId(),
                training.trainerId(),
                training.trainingName(),
                fromTrainingTypeEntity(training.trainingType()),
                training.trainingDate(),
                training.trainingDuration());
    }

    /**
     * Maps an entity training type to the DTO training type.
     * @param trainingTypeEntity the entity training type, may be null.
     * @return the corresponding DTO training type, or null if entity is null.
     */
    private static TrainingType fromTrainingTypeEntity(TrainingTypeEntity trainingTypeEntity) {
        if (trainingTypeEntity == null) {
            return null;
        }
        return switch (trainingTypeEntity) {
            case Bodybuilding -> TrainingType.Bodybuilding;
            case Crossfit -> TrainingType.Crossfit;
            case Fitness -> TrainingType.Fitness;
        };
    }
}
