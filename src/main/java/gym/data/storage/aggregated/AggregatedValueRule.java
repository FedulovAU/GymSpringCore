package gym.data.storage.aggregated;

public interface AggregatedValueRule<TEntity, TAggregateKey, TAggregateValue extends Comparable<TAggregateValue>> {
    TAggregateKey aggregateBy(TEntity entity);
    TAggregateValue getValue(TEntity entity);
    TEntity mergeOnAdd(TEntity entity, TAggregateValue existedValue);
}
