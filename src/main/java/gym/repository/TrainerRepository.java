package gym.repository;

import gym.data.entity.TrainerEntity;
import gym.data.storage.Storage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Repository
public class TrainerRepository {

    private final Storage<TrainerEntity, Long> storage;

    public TrainerRepository(Storage<TrainerEntity, Long> storage){
        this.storage = storage;
    }

    public TrainerEntity add(TrainerEntity trainer){
        return storage.add(trainer);
    }

    public List<TrainerEntity> getAll(){
        return storage.getAll();
    }

    public TrainerEntity update(long id, UnaryOperator<TrainerEntity> updater){
        return storage.update(id, updater);
    }

    public Optional<TrainerEntity> getById(long id){
        return storage.get(id);
    }
}
