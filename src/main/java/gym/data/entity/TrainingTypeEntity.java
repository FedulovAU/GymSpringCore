package gym.data.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TrainingTypeEntity {
    private final Long id;
    private final TrainingType trainingType;

    @JsonCreator
    public TrainingTypeEntity(
            @JsonProperty("id") Long id,
            @JsonProperty("trainingType") TrainingType trainingType) {
        this.id = id;
        this.trainingType = trainingType;
    }

    public static TrainingTypeEntity of(TrainingType trainingType) {
        long id = switch (trainingType) {
            case BODY_BUILDING -> 1L;
            case CROSSFIT -> 2L;
            case FITNESS -> 3L;
        };
        return new TrainingTypeEntity(id, trainingType);
    }

    public enum TrainingType {
        BODY_BUILDING,
        CROSSFIT,
        FITNESS
    }
}
