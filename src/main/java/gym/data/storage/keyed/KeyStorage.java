package gym.data.storage.keyed;

import gym.data.storage.Storage;
import gym.data.storage.exception.ItemAlreadyExistsException;
import gym.data.storage.exception.ItemNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;


public class KeyStorage<TEntity, TKey> implements Storage<TEntity, TKey> {
    private final Class<TEntity> type;
    private final ConcurrentHashMap<TKey, TEntity> items;
    private final KeyExtractor<TEntity, TKey> keyExtractor;

    public KeyStorage(Class<TEntity> type, KeyExtractor<TEntity, TKey> keyExtractor){
        this.type = type;
        this.keyExtractor = keyExtractor;
        items = new ConcurrentHashMap<>();
    }

    @Override
    public Class<TEntity> getType() {
        return type;
    }

    @Override
    public TEntity add(TEntity item){
        var key = keyExtractor.extractKey(item);
        if(key == null){
            throw new IllegalArgumentException("Cannot add to storage, item key is null");
        }

        if(items.putIfAbsent(key, item) != null){
            throw new ItemAlreadyExistsException(type.getSimpleName(), key.toString());
        }
        return item;
    }

    @Override
    public TEntity update(TKey key, UnaryOperator<TEntity> updater){
        TEntity updated = items.computeIfPresent(key, (k, current) -> {
            TEntity next = updater.apply(current);
            if(next == null || keyExtractor.extractKey(next) == null || !keyExtractor.extractKey(next).equals(key)){
                throw new IllegalArgumentException("Updater must return an item with the same ID: " + key.toString() + ".");
            }
            return next;
        });

        if(updated == null){
            throw new ItemNotFoundException(type.getSimpleName(), key.toString());
        }
        return updated;
    }

    @Override
    public void delete(TKey key){
        items.remove(key);
    }

    @Override
    public void put(TEntity item) {
        add(item);
    }

    @Override
    public Optional<TEntity> get(TKey key){
        return Optional.ofNullable(items.get(key));
    }

    @Override
    public List<TEntity> getAll(){
        return List.copyOf(items.values());
    }

}
