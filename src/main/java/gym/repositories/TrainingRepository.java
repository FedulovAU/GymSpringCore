package gym.repositories;

import gym.data.entities.TrainingEntity;
import gym.data.storage.MemStorage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingRepository {

    private final MemStorage storage;

    public TrainingRepository(MemStorage storage){
        this.storage = storage;
    }

    public TrainingEntity add(TrainingEntity training){
        return storage.addItem(TrainingEntity.class, training);
    }

    public List<TrainingEntity> getAll(){
        return storage.getAllItems(TrainingEntity.class);
    }

    public List<TrainingEntity> getByTrainerId(long trainerId){
        return getAll().stream()
                .filter(training -> training.trainerId() == trainerId)
                .toList();
    }

    public List<TrainingEntity> getByTraineeId(long traineeId){
        return getAll().stream()
                .filter(training -> training.traineeId() == traineeId)
                .toList();
    }

    public Optional<TrainingEntity> getById(long id){
        return storage.getItem(TrainingEntity.class, id);
    }

    /**
     * Deletes a training.
     * @param id the unique identifier of the training to delete.
     * @throws gym.data.storage.exceptions.ItemNotFoundException if the training doesn't exist.
     */
    public TrainingEntity deleteById(long id){
        return storage.deleteItem(TrainingEntity.class, id);
    }
}
