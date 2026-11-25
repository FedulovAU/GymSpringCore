package gym.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "training_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TrainingTypeEntity {
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_type_name", nullable = false, unique = true, length = 50)
    private TrainingType trainingType;

    public enum TrainingType {
        BODY_BUILDING,
        CROSSFIT,
        FITNESS
    }
}
