package gym.data.storage;

import gym.data.storage.exceptions.ItemAlreadyExistsException;
import gym.data.storage.exceptions.ItemNotFoundException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;


@Component
public class MemStorage {
    public static class Baket {
        private final Class<?> type;
        private final ConcurrentHashMap<Long, EntityWithId<?>> items;
        private final AtomicLong nextId;

        public Baket(Class<?> type){
            this.type = type;
            items = new ConcurrentHashMap<>();
            nextId = new AtomicLong(0);
        }

        public EntityWithId<?> add(EntityWithId<?> item){
            if(item.id() != null){
                throw new IllegalArgumentException("Item already has an ID: " + item.id() +
                        ". Cannot add to storage, provide item with id == null.");
            }
            long id = nextId.incrementAndGet();
            EntityWithId<?> newItem = item.withId(id);
            // IDs come from an atomic counter, so a collision means a bug; never silently overwrite.
            if(items.putIfAbsent(id, newItem) != null){
                throw new ItemAlreadyExistsException(type, id);
            }
            return newItem;
        }

        public EntityWithId<?> update(long id, UnaryOperator<EntityWithId<?>> updater){
            EntityWithId<?> updated = items.computeIfPresent(id, (key, current) -> {
                EntityWithId<?> next = updater.apply(current);
                if(next == null || next.id() == null || next.id() != id){
                    throw new IllegalArgumentException("Updater must return an item with the same ID: " + id + ".");
                }
                return next;
            });

            if(updated == null){
                throw new ItemNotFoundException(type, id);
            }
            return updated;
        }

        public EntityWithId<?> delete(long id){
            EntityWithId<?> removed = items.remove(id);
            if(removed == null){
                throw new ItemNotFoundException(type, id);
            }
            return removed;
        }

        public List<EntityWithId<?>> getAll(){
            return List.copyOf(items.values());
        }

        public Optional<EntityWithId<?>> get(long id){
            return Optional.ofNullable(items.get(id));
        }

        void restore(List<? extends EntityWithId<?>> restored){
            for (EntityWithId<?> item : restored) {
                if (item.id() == null) {
                    throw new IllegalArgumentException("Restored item of type " + type.getSimpleName() + " has no ID.");
                }
                long id = item.id();
                if (items.putIfAbsent(id, item) != null) {
                    throw new ItemAlreadyExistsException(type, id);
                }
                nextId.accumulateAndGet(id, Math::max);
            }
        }
    }

    private final ConcurrentHashMap<Class<?>, Baket> bakets;

    public MemStorage(){
        bakets = new ConcurrentHashMap<>();
    }

    public <T extends EntityWithId<T>> T addItem(Class<T> type, T item){
        return type.cast(bakets
                .computeIfAbsent(type, Baket::new)
                .add(item));
    }

    public <T> Optional<T> getItem(Class<T> type, long id){
        Baket baket = bakets.get(type);
        if (baket == null) return Optional.empty();
        return baket.get(id).map(type::cast);
    }

    public <T extends EntityWithId<T>> List<T> getAllItems(Class<T> type){
        Baket baket = bakets.get(type);
        if (baket == null) return List.of();
        return baket.getAll().stream().map(type::cast).toList();
    }

    public <T extends EntityWithId<T>> T updateItem(Class<T> type, long id, UnaryOperator<T> updater){
        Baket baket = bakets.get(type);
        if (baket == null) {
            throw new ItemNotFoundException(type, id);
        }
        return type.cast(baket.update(id, current -> updater.apply(type.cast(current))));
    }

    public <T> T deleteItem(Class<T> type, long id){
        Baket baket = bakets.get(type);
        if (baket == null) {
            throw new ItemNotFoundException(type, id);
        }
        return type.cast(baket.delete(id));
    }

    <T extends EntityWithId<T>> void restoreItems(Class<T> type, List<T> items){
        bakets.computeIfAbsent(type, Baket::new).restore(items);
    }
}
