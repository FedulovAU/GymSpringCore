package gym.data.storage.keyed;

public interface KeyExtractor<TEntity, TKey> {
    TKey extractKey(TEntity entity);
}
