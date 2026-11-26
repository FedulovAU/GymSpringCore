package gym.repository;

import gym.data.TrainerEntity;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerRepository {

    private final SessionFactory sessionFactory;

    public TrainerRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public TrainerEntity save(TrainerEntity trainer) {
        sessionFactory.getCurrentSession().persist(trainer);
        return trainer;
    }

    public Optional<TrainerEntity> findByUsername(String username) {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select t from TrainerEntity t join fetch t.user u join fetch t.specialization "
                                + "where u.username = :username",
                        TrainerEntity.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public List<TrainerEntity> findNotAssignedToTrainee(String traineeUsername) {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select tr from TrainerEntity tr join fetch tr.user u join fetch tr.specialization "
                                + "where not exists ("
                                + "select 1 from TrainingEntity t "
                                + "where t.trainer = tr and t.trainee.user.username = :traineeUsername) ",
                        TrainerEntity.class)
                .setParameter("traineeUsername", traineeUsername)
                .getResultList();
    }
}
