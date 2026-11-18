package gym.data.storage.autoincrement;

import java.util.concurrent.atomic.AtomicReference;

public interface KeySetter<TEntity, TKey> {
    TEntity setKey(TEntity entity, TKey newKey);
    TKey incrementAndGet(AtomicReference<TKey> key);
    void setMaxKey(TEntity entity, AtomicReference<TKey> prev);
}
