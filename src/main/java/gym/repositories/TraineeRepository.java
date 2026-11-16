package gym.repositories;

import gym.data.entities.TraineeEntity;
import gym.data.storage.MemStorage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Repository
public class TraineeRepository {

    private final MemStorage storage;

    public TraineeRepository(MemStorage storage){
        this.storage = storage;
    }

    public TraineeEntity add(TraineeEntity trainee){
        return storage.addItem(TraineeEntity.class, trainee);
    }

    public List<TraineeEntity> getAll(){
        return storage.getAllItems(TraineeEntity.class);
    }

    public TraineeEntity update(long id, UnaryOperator<TraineeEntity> updater){
        return storage.updateItem(TraineeEntity.class, id, updater);
    }

    public TraineeEntity deleteById(long id){
        return storage.deleteItem(TraineeEntity.class, id);
    }

    public Optional<TraineeEntity> getById(long id){
        return storage.getItem(TraineeEntity.class, id);
    }
}
