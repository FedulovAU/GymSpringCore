package gym.data.entity;

import gym.data.entity.TrainingTypeEntity.TrainingType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TrainingTypeEntityTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void of_givesEachTypeItsFixedId() {
        Map<TrainingType, Long> expectedIds = Map.of(
                TrainingType.BODY_BUILDING, 1L,
                TrainingType.CROSSFIT, 2L,
                TrainingType.FITNESS, 3L);

        for (TrainingType type : TrainingType.values()) {
            var entity = TrainingTypeEntity.of(type);
            assertEquals(expectedIds.get(type), entity.getId());
            assertEquals(type, entity.getTrainingType());
        }
    }

    @Test
    void json_writesTypeByName() {
        var json = mapper.readTree(mapper.writeValueAsString(TrainingTypeEntity.of(TrainingType.CROSSFIT)));

        assertEquals(mapper.readTree("{\"id\":2,\"trainingType\":\"CROSSFIT\"}"), json);
    }

    @Test
    void json_readsBackSameEntity() {
        var original = TrainingTypeEntity.of(TrainingType.FITNESS);

        var restored = mapper.readValue(mapper.writeValueAsString(original), TrainingTypeEntity.class);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
