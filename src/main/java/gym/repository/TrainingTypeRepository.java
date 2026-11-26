package gym.repository;

import gym.data.TrainingTypeEntity;
import gym.enums.TrainingType;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TrainingTypeRepository {

    private final SessionFactory sessionFactory;

    public TrainingTypeRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Optional<TrainingTypeEntity> findByType(TrainingType trainingType) {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select tt from TrainingTypeEntity tt where tt.trainingType = :trainingType",
                        TrainingTypeEntity.class)
                .setParameter("trainingType", trainingType)
                .uniqueResultOptional();
    }
}
