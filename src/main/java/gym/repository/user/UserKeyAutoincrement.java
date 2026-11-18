package gym.repository.user;

import gym.data.entity.UserEntity;
import gym.data.storage.autoincrement.KeySetter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class UserKeyAutoincrement implements KeySetter<UserEntity, Long> {

    @Override
    public UserEntity setKey(UserEntity entity, Long key) {
        return entity.withId(key);
    }

    @Override
    public Long incrementAndGet(AtomicReference<Long> key) {
        return key.updateAndGet(currentKey -> currentKey == null ? 1L : currentKey + 1);
    }

    @Override
    public void setMaxKey(UserEntity userEntity, AtomicReference<Long> prev) {
        Long userId = userEntity.getId();
        prev.updateAndGet(currentKey -> currentKey == null || userId > currentKey ? userId : currentKey);
    }
}
