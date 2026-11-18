package gym.data.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gym.data.enums.TrainingType;
import lombok.Getter;
import lombok.With;

@Getter
@With
public class TrainerEntity {
    private final Long id;
    private final TrainingType specialization;

    @JsonCreator
    public TrainerEntity(
            @JsonProperty("id") Long id,
            @JsonProperty("specialization") TrainingType specialization) {
        this.id = id;
        this.specialization = specialization;
    }
}
