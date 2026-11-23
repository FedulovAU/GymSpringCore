package gym.repository;

import gym.data.entity.TrainingEntity;
import gym.data.storage.Storage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingRepository {

    private final Storage<TrainingEntity, TrainingEntity.CompositeKey> storage;

    public TrainingRepository(Storage<TrainingEntity, TrainingEntity.CompositeKey> storage){
        this.storage = storage;
    }

    public TrainingEntity add(TrainingEntity training){
        return storage.add(training);
    }

    public List<TrainingEntity> getAll(){
        return storage.getAll();
    }

    public List<TrainingEntity> getByTrainerId(long trainerId){
        return getAll().stream()
                .filter(training -> training.getTrainerId() == trainerId)
                .toList();
    }

    public List<TrainingEntity> getByTraineeId(long traineeId){
        return getAll().stream()
                .filter(training -> training.getTraineeId() == traineeId)
                .toList();
    }

    public Optional<TrainingEntity> getById(long trainerId, long traineeId){
        return storage.get(new TrainingEntity.CompositeKey(trainerId, traineeId));
    }
}
