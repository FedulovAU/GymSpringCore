package gym.data.storage.aggregated;

import gym.data.storage.Storage;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;


public class AggregatedValueStorage<TEntity, TKey, TAggregateKey, TAggregateValue extends Comparable<TAggregateValue>>
        implements Storage<TEntity, TKey> {

    private final ConcurrentHashMap<TAggregateKey, TAggregateValue> aggregatedItems;
    private final Storage<TEntity, TKey> innerStorage;
    AggregatedValueRule<TEntity, TAggregateKey, TAggregateValue> aggregateRule;

    public AggregatedValueStorage(
            Storage<TEntity, TKey> storage,
            AggregatedValueRule<TEntity, TAggregateKey, TAggregateValue> aggregateRule) {
        this.innerStorage = storage;
        this.aggregateRule = aggregateRule;
        aggregatedItems = new ConcurrentHashMap<>();
    }

    @Override
    public Class<TEntity> getType() {
        return innerStorage.getType();
    }

    @Override
    public TEntity add(TEntity item){
        var addedItem = new AtomicReference<TEntity>();
        aggregatedItems.compute(
                aggregateRule.aggregateBy(item),
                (key, currentValue) -> {
                    var updatedItem = aggregateRule.mergeOnAdd(item, currentValue);
                    addedItem.set(innerStorage.add(updatedItem));
                    return aggregateRule.getValue(updatedItem);
                });

        return addedItem.get();
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
    public Optional<TEntity> get(TKey key){
        return innerStorage.get(key);
    }

    @Override
    public List<TEntity> getAll(){
        return innerStorage.getAll();
    }

    public void put(TEntity item){
        aggregatedItems.compute(
                aggregateRule.aggregateBy(item),
                (key, currentValue) -> {
                    var newValue = aggregateRule.getValue(item);
                    if(currentValue == null){
                        currentValue = newValue;
                    }
                    newValue = currentValue.compareTo(newValue) > 0
                            ? currentValue
                            : newValue;

                    innerStorage.put(item);
                    return newValue;
                });
    }

}
