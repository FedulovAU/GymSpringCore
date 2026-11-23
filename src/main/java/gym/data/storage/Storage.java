package gym.data.storage;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

public interface Storage<TEntity, TKey> {
    Class<TEntity> getType();

    TEntity add(TEntity item);

    TEntity update(TKey key, UnaryOperator<TEntity> updater);

    Optional<TEntity> get(TKey key);

    List<TEntity> getAll();

    void delete(TKey key);

    void put(TEntity item);
}
