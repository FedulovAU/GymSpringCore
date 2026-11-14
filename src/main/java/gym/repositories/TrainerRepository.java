package gym.repositories;

import gym.data.entities.TrainerEntity;
import gym.data.storage.MemStorage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Repository
public class TrainerRepository {

    private final MemStorage storage;

    public TrainerRepository(MemStorage storage){
        this.storage = storage;
    }

    public TrainerEntity add(TrainerEntity trainer){
        return storage.addItem(TrainerEntity.class, trainer);
    }

    public List<TrainerEntity> getAll(){
        return storage.getAllItems(TrainerEntity.class);
    }

    /**
     * Atomically updates a trainer.
     * @param id the unique identifier of the trainer to update.
     * @param updater receives the current trainer and returns its new version, must keep the ID.
     * @return the updated trainer.
     * @throws gym.data.storage.exceptions.ItemNotFoundException if the trainer doesn't exist.
     */
    public TrainerEntity update(long id, UnaryOperator<TrainerEntity> updater){
        return storage.updateItem(TrainerEntity.class, id, updater);
    }

    public Optional<TrainerEntity> getById(long id){
        return storage.getItem(TrainerEntity.class, id);
    }
}
