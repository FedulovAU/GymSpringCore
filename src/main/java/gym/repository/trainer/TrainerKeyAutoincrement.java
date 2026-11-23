package gym.repository.trainer;

import gym.data.entity.TrainerEntity;
import gym.data.storage.autoincrement.KeySetter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class TrainerKeyAutoincrement implements KeySetter<TrainerEntity, Long> {

    @Override
    public TrainerEntity setKey(TrainerEntity entity, Long key) {
        return entity.withId(key);
    }

    @Override
    public Long incrementAndGet(AtomicReference<Long> key) {
        return key.updateAndGet(currentKey -> currentKey == null ? 1L : currentKey + 1);
    }

    @Override
    public void setMaxKey(TrainerEntity entity, AtomicReference<Long> prev) {
        Long id = entity.getId();
        prev.updateAndGet(currentKey -> currentKey == null || id > currentKey ? id : currentKey);
    }
}
