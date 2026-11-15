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

    /**
     * Atomically updates a trainee.
     * @param id the unique identifier of the trainee to update.
     * @param updater receives the current trainee and returns its new version, must keep the ID.
     * @return the updated trainee.
     * @throws gym.data.storage.exceptions.ItemNotFoundException if the trainee doesn't exist.
     */
    public TraineeEntity update(long id, UnaryOperator<TraineeEntity> updater){
        return storage.updateItem(TraineeEntity.class, id, updater);
    }

    public TraineeEntity delete(long id){
        return storage.deleteItem(TraineeEntity.class, id);
    }

    public Optional<TraineeEntity> getById(long id){
        return storage.getItem(TraineeEntity.class, id);
    }
}
