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
    private final UserEntity user;
    private final LocalDate birthDate;

    @JsonCreator
    public TraineeEntity(
            @JsonProperty("id") Long id,
            @JsonProperty("user") UserEntity user,
            @JsonProperty("birthDate") LocalDate birthDate) {
        this.id = id;
        this.user = user;
        this.birthDate = birthDate;
    }
}
