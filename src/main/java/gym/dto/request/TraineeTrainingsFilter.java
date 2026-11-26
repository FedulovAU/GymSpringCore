package gym.dto.request;

import gym.enums.TrainingType;

import java.time.LocalDate;

public record TraineeTrainingsFilter(
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        TrainingType trainingType) {
}
