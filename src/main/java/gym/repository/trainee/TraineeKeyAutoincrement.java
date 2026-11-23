package gym.repository.trainee;

import gym.data.entity.TraineeEntity;
import gym.data.storage.autoincrement.KeySetter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class TraineeKeyAutoincrement implements KeySetter<TraineeEntity, Long> {

    @Override
    public TraineeEntity setKey(TraineeEntity entity, Long key) {
        return entity.withId(key);
    }

    @Override
    public Long incrementAndGet(AtomicReference<Long> key) {
        return key.updateAndGet(currentKey -> currentKey == null ? 1L : currentKey + 1);
    }

    @Override
    public void setMaxKey(TraineeEntity entity, AtomicReference<Long> prev) {
        Long id = entity.getId();
        prev.updateAndGet(currentKey -> currentKey == null || id > currentKey ? id : currentKey);
    }
}
