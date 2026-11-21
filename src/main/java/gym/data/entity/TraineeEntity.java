package gym.data.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.With;

import java.time.LocalDate;

@Getter
@With
public class TraineeEntity {
    private final Long id;
    private final LocalDate birthDate;

    @JsonCreator
    public TraineeEntity(
            @JsonProperty("id") Long id,
            @JsonProperty("birthDate") LocalDate birthDate) {
        this.id = id;
        this.birthDate = birthDate;
    }
}
