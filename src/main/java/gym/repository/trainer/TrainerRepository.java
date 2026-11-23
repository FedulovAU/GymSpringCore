package gym.repository.trainer;

import gym.data.entity.TrainerEntity;
import gym.data.storage.aggregated.AggregatedValueStorage;
import gym.repository.UsernameKey;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Repository
public class TrainerRepository {

    private final AggregatedValueStorage<TrainerEntity, Long, UsernameKey, Long> storage;

    public TrainerRepository(AggregatedValueStorage<TrainerEntity, Long, UsernameKey, Long> storage){
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

    public void deleteById(long id){
        storage.delete(id);
    }

    public Optional<TrainerEntity> getById(long id){
        return storage.get(id);
    }

    public Optional<Long> getLastUsernameSuffix(String firstName, String lastName) {
        return storage.getAggregatedValue(new UsernameKey(firstName, lastName));
    }
}
