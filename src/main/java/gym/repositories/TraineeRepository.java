package gym.repositories;

import gym.data.entities.TraineeEntity;
import gym.data.storage.MemStorage;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TraineeRepository {

    private final MemStorage storage;

    public TraineeRepository(MemStorage storage){
        this.storage = storage;
    }

    public TraineeEntity addTrainee(TraineeEntity trainee){
        return storage.addItem(TraineeEntity.class, trainee);
    }

    public List<TraineeEntity> getTrainees(){
        return storage.getAllItems(TraineeEntity.class);
    }

    public void updateTrainee(TraineeEntity trainee){
        storage.updateItem(TraineeEntity.class, trainee);
    }

    public void deleteTrainee(long id){
        storage.deleteItem(TraineeEntity.class, id);
    }
}
