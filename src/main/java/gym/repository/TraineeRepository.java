package gym.repository;

import gym.data.entity.TraineeEntity;
import gym.data.storage.Storage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Repository
public class TraineeRepository {

    private final Storage<TraineeEntity, Long> storage;

    public TraineeRepository(Storage<TraineeEntity, Long> storage){
        this.storage = storage;
    }

    public TraineeEntity add(TraineeEntity trainee){
        return storage.add(trainee);
    }

    public List<TraineeEntity> getAll(){
        return storage.getAll();
    }

    public TraineeEntity update(long id, UnaryOperator<TraineeEntity> updater){
        return storage.update(id, updater);
    }

    public void deleteById(long id){
        storage.delete(id);
    }

    public Optional<TraineeEntity> getById(long id){
        return storage.get(id);
    }
}
