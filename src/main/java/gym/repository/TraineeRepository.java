package gym.repository;

import gym.data.TraineeEntity;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TraineeRepository {

    private final SessionFactory sessionFactory;

    public TraineeRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public TraineeEntity save(TraineeEntity trainee) {
        sessionFactory.getCurrentSession().persist(trainee);
        return trainee;
    }

    public Optional<TraineeEntity> findByUsername(String username) {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select t from TraineeEntity t join fetch t.user u where u.username = :username",
                        TraineeEntity.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public void delete(TraineeEntity trainee) {
        sessionFactory.getCurrentSession().remove(trainee);
    }
}
