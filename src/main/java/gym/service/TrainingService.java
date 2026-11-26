package gym.service;

import gym.data.TraineeEntity;
import gym.data.TrainerEntity;
import gym.data.TrainingEntity;
import gym.dto.request.AddTraining;
import gym.dto.request.TraineeTrainingsFilter;
import gym.dto.request.TrainerTrainingsFilter;
import gym.dto.response.Training;
import gym.enums.TrainingType;
import gym.repository.TraineeRepository;
import gym.repository.TrainerRepository;
import gym.repository.TrainingRepository;
import gym.repository.TrainingTypeRepository;
import gym.service.exception.CannotScheduleTrainingException;
import gym.service.exception.ProfileNotFoundException;
import gym.service.mapper.GymMapper;
import gym.service.validation.RequestValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TrainingService {
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final RequestValidator requestValidator;
    private final GymMapper gymMapper;

    public TrainingService(
            TrainingRepository trainingRepository,
            TraineeRepository traineeRepository,
            TrainerRepository trainerRepository,
            RequestValidator requestValidator,
            GymMapper gymMapper) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.requestValidator = requestValidator;
        this.gymMapper = gymMapper;
    }

    @Transactional
    public Training addTraining(AddTraining request) {
        requestValidator.validateAddTrainingRequest(request);

        TraineeEntity trainee = getActiveTrainee(request.traineeUsername());
        TrainerEntity trainer = getActiveTrainer(request.trainerUsername());

        ensureNoOverlap(trainingRepository.findAllByTrainer(trainer), request, "Trainer");
        ensureNoOverlap(trainingRepository.findAllByTrainee(trainee), request, "Trainee");

        TrainingEntity training = trainingRepository.save(new TrainingEntity(
                trainee,
                trainer,
                request.trainingName(),
                trainer.getSpecialization(),
                request.trainingDate(),
                request.trainingDurationMinutes()));

        log.info("addTraining completed, trainee={}, trainer={}.", request.traineeUsername(), request.trainerUsername());
        return gymMapper.toTraining(training);
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String traineeUsername, TraineeTrainingsFilter filter) {

        return trainingRepository.findTraineeTrainings(
                        traineeUsername,
                        startOfDay(filter.fromDate()),
                        startOfDay(filter.toDate()),
                        filter.trainerName(),
                        filter.trainingType())
                .stream()
                .map(gymMapper::toTraining)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String trainerUsername, TrainerTrainingsFilter filter) {
        return trainingRepository.findTrainerTrainings(
                        trainerUsername,
                        startOfDay(filter.fromDate()),
                        startOfDay(filter.toDate()),
                        filter.traineeName())
                .stream()
                .map(gymMapper::toTraining)
                .toList();
    }

    private static void ensureNoOverlap(List<TrainingEntity> existing, AddTraining request, String who) {
        LocalDateTime newStart = request.trainingDate();
        LocalDateTime newEnd = newStart.plusMinutes(request.trainingDurationMinutes());

        for (TrainingEntity training : existing) {
            LocalDateTime existingStart = training.getTrainingDate();
            LocalDateTime existingEnd = existingStart.plusMinutes(training.getTrainingDurationMinutes());

            if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                throw new CannotScheduleTrainingException(who + " already has a training at that time: '"
                        + training.getTrainingName() + "' (" + existingStart + " - " + existingEnd + ").");
            }
        }
    }

    private TraineeEntity getActiveTrainee(String username) {
        TraineeEntity trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new ProfileNotFoundException("Trainee '" + username + "' not found."));
        if (!trainee.getUser().isActive()) {
            throw new CannotScheduleTrainingException("Trainee '" + username + "' is not active.");
        }
        return trainee;
    }

    private TrainerEntity getActiveTrainer(String username) {
        TrainerEntity trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new ProfileNotFoundException("Trainer '" + username + "' not found."));
        if (!trainer.getUser().isActive()) {
            throw new CannotScheduleTrainingException("Trainer '" + username + "' is not active.");
        }
        return trainer;
    }

    private static LocalDateTime startOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }
}
