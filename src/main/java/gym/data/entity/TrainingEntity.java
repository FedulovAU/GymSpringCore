package gym.data.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gym.data.enums.TrainingType;
import lombok.Getter;
import lombok.With;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@With
public class TrainingEntity {
    private final Long traineeId;
    private final Long trainerId;
    private final String trainingName;
    private final TrainingType trainingType;
    private final LocalDateTime trainingDate;
    private final Duration trainingDuration;

    @JsonCreator
    public TrainingEntity(
            @JsonProperty("traineeId") Long traineeId,
            @JsonProperty("trainerId") Long trainerId,
            @JsonProperty("trainingName") String trainingName,
            @JsonProperty("trainingType") TrainingType trainingType,
            @JsonProperty("trainingDate") LocalDateTime trainingDate,
            @JsonProperty("trainingDuration") Duration trainingDuration) {
        this.traineeId = traineeId;
        this.trainerId = trainerId;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    public record CompositeKey(long trainerId, long traineeId) {}
}