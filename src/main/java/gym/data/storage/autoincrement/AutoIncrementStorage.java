package gym.data.storage.autoincrement;

import gym.data.storage.Storage;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;


public class AutoIncrementStorage<TEntity, TKey> implements Storage<TEntity, TKey> {

    private final Storage<TEntity, TKey> innerStorage;
    private final AtomicReference<TKey> lastKey;
    private final KeySetter<TEntity, TKey> keySetter;

    public AutoIncrementStorage(Storage<TEntity, TKey> storage, KeySetter<TEntity, TKey> keySetter){
        this.innerStorage = storage;
        this.keySetter = keySetter;
        lastKey = new AtomicReference<>(null);
    }

    @Override
    public Class<TEntity> getType() {
        return innerStorage.getType();
    }

    @Override
    public TEntity add(TEntity item){
        TEntity newItem = keySetter.setKey(item, keySetter.incrementAndGet(lastKey));
        return innerStorage.add(newItem);
    }

    @Override
    public TEntity update(TKey key, UnaryOperator<TEntity> updater){
       return innerStorage.update(key, updater);
    }

    @Override
    public void delete(TKey key){
        innerStorage.delete(key);
    }

    @Override
    public void put(TEntity item) {
        keySetter.setMaxKey(item, lastKey);
        innerStorage.put(item);
    }

    @Override
    public Optional<TEntity> get(TKey key){
        return innerStorage.get(key);
    }

    @Override
    public List<TEntity> getAll(){
        return innerStorage.getAll();
    }

}
