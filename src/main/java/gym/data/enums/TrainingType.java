package gym.data.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TrainingType {
    BODY_BUILDING(1),
    CROSSFIT(2),
    FITNESS(3);

    private final int id;

    TrainingType(int id) {
        this.id = id;
    }

    @JsonValue
    public int getId() {
        return id;
    }

    @JsonCreator
    public static TrainingType fromId(int id) {
        for (TrainingType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown training type id: " + id + ".");
    }
}
