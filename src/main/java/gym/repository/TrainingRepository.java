package gym.repository;

import gym.data.TraineeEntity;
import gym.data.TrainerEntity;
import gym.data.TrainingEntity;
import gym.enums.TrainingType;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TrainingRepository {

    private final SessionFactory sessionFactory;

    public TrainingRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public TrainingEntity save(TrainingEntity training) {
        sessionFactory.getCurrentSession().persist(training);
        return training;
    }

    public List<TrainingEntity> findTraineeTrainings(
            String traineeUsername,
            LocalDateTime from,
            LocalDateTime to,
            String trainerName,
            TrainingType trainingType) {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select t from TrainingEntity t "
                                + "join fetch t.trainee te join fetch te.user teu "
                                + "join fetch t.trainer tr join fetch tr.user tru "
                                + "join fetch t.trainingType tt "
                                + "where teu.username = :traineeUsername "
                                + "and (cast(:from as LocalDateTime) is null or t.trainingDate >= :from) "
                                + "and (cast(:to as LocalDateTime) is null or t.trainingDate <= :to) "
                                + "and (:trainerName is null or tru.firstName = :trainerName or tru.lastName = :trainerName) "
                                + "and (:trainingType is null or tt.trainingType = :trainingType) ",
                        TrainingEntity.class)
                .setParameter("traineeUsername", traineeUsername)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("trainerName", trainerName)
                .setParameter("trainingType", trainingType)
                .getResultList();
    }

    public List<TrainingEntity> findTrainerTrainings(
            String trainerUsername,
            LocalDateTime from,
            LocalDateTime to,
            String traineeName) {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select t from TrainingEntity t "
                                + "join fetch t.trainee te join fetch te.user teu "
                                + "join fetch t.trainer tr join fetch tr.user tru "
                                + "join fetch t.trainingType "
                                + "where tru.username = :trainerUsername "
                                + "and (cast(:from as LocalDateTime) is null or t.trainingDate >= :from) "
                                + "and (cast(:to as LocalDateTime) is null or t.trainingDate <= :to) "
                                + "and (:traineeName is null or teu.firstName = :traineeName or teu.lastName = :traineeName) ",
                        TrainingEntity.class)
                .setParameter("trainerUsername", trainerUsername)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("traineeName", traineeName)
                .getResultList();
    }

    public List<TrainingEntity> findAllByTrainee(TraineeEntity trainee) {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery("select t from TrainingEntity t where t.trainee = :trainee", TrainingEntity.class)
                .setParameter("trainee", trainee)
                .getResultList();
    }

    public List<TrainingEntity> findAllByTrainer(TrainerEntity trainer) {
        return sessionFactory.getCurrentSession()
                .createSelectionQuery("select t from TrainingEntity t where t.trainer = :trainer", TrainingEntity.class)
                .setParameter("trainer", trainer)
                .getResultList();
    }
}
