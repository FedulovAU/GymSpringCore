package gym.data.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.With;

@Getter
@With
public class TrainerEntity {
    private final Long id;
    private final UserEntity user;
    private final TrainingTypeEntity specialization;

    @JsonCreator
    public TrainerEntity(
            @JsonProperty("id") Long id,
            @JsonProperty("user") UserEntity user,
            @JsonProperty("specialization") TrainingTypeEntity specialization) {
        this.id = id;
        this.user = user;
        this.specialization = specialization;
    }
}
