package gym.data.storage;

import gym.data.storage.exceptions.ItemAlreadyExistsException;
import gym.data.storage.exceptions.ItemNotFoundException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;

/**
 * Stores items in memory grouped by their class type.
 * Each class type has its own "baket" of items, which is a map of IDs to objects.
 * Thread safe, allows concurrent access and modification of items.
 */
@Component
public class MemStorage {

    /**
     * Stores items of type EntityWithId<?> in a map, provide CRUD operations for them.
     */
    public static class Baket {
        private final Class<?> type;
        private final ConcurrentHashMap<Long, EntityWithId<?>> items;
        private final AtomicLong nextId;

        public Baket(Class<?> type){
            this.type = type;
            items = new ConcurrentHashMap<>();
            nextId = new AtomicLong(0);
        }

        /**
         * Adds an item to the baket and assigns it a unique ID.
         * @param item, the item to add, item.id must be null, indicating that the item is new.
         * @return the item that was added, with its ID set to the newly assigned unique ID.
         * @throws IllegalArgumentException if the item already has an ID.
         * @throws ItemAlreadyExistsException if the generated ID is already taken (indicates a bug).
         */
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

        /**
         * Atomically updates an existing item in the baket.
         * The read-modify-write runs under the map's per-key lock, so concurrent updates
         * of the same item can't overwrite each other.
         * @param id, ID of the item to update.
         * @param updater, receives the current item and returns its new version.
         *                 Must be short, must not access this storage and must keep the item's ID.
         * @return the new version of the item.
         * @throws ItemNotFoundException if no item with this ID exists.
         */
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

        /**
         * Deletes an item from the baket.
         * @param id, ID of the item to delete.
         * @throws ItemNotFoundException if no item with this ID exists.
         * @return the deleted item.
         */
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

        /**
         * Retrieves an item from the baket by its ID.
         * @param id
         * @return either an Optional containing the item with the specified ID, or an empty Optional if no such item exists.
         */
        public Optional<EntityWithId<?>> get(long id){
            return Optional.ofNullable(items.get(id));
        }
    }

    private final ConcurrentHashMap<Class<?>, Baket> bakets;

    public MemStorage(){
        bakets = new ConcurrentHashMap<>();
    }

    /**
     * Stores an item in the appropriate baket based on its class type.
     * @param type, item class type, used to determine which baket to store the item in.
     * @param item, item.id must be null, indicating that the item is new.
     * @param <T>, item class type, must implement EntityWithId<T> interface.
     * @return the item that was added, with its ID set to the newly assigned unique ID.
     * @throws IllegalArgumentException if the item already has an ID.
     * @throws ItemAlreadyExistsException if the generated ID is already taken (indicates a bug).
     */
    public <T extends EntityWithId<T>> T addItem(Class<T> type, T item){
        return type.cast(bakets
                .computeIfAbsent(type, Baket::new)
                .add(item));
    }

    /**
     * Retrieves an item from the appropriate baket based on its class type and ID.
     * @param type, item class type, used to determine which baket to retrieve the item from.
     * @param id, ID of the item to retrieve.
     * @param <T>, item class type, must implement EntityWithId<T> interface.
     * @return an Optional containing the item if found, or an empty Optional if not found.
     */
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


    /**
     * Atomically updates an item in the storage bucket corresponding to its class type.
     *
     * @param type the class type used to locate the item's storage bucket
     * @param id the ID of the item to update
     * @param updater receives the current item and returns its new version, must keep the item's ID.
     *                Runs under a lock: keep it short and don't access this storage from it.
     * @return the new version of the item
     * @throws ItemNotFoundException if the item doesn't exist
     */
    public <T extends EntityWithId<T>> T updateItem(Class<T> type, long id, UnaryOperator<T> updater){
        Baket baket = bakets.get(type);
        if (baket == null) {
            throw new ItemNotFoundException(type, id);
        }
        return type.cast(baket.update(id, current -> updater.apply(type.cast(current))));
    }

    /**
     * Deletes an item from the storage bucket corresponding to its class type.
     *
     * @param type the class type used to locate the item's storage bucket
     * @param id the ID of the item to delete
     * @throws ItemNotFoundException if the item doesn't exist
     * @return the deleted item
     */
    public <T> T deleteItem(Class<T> type, long id){
        Baket baket = bakets.get(type);
        if (baket == null) {
            throw new ItemNotFoundException(type, id);
        }
        return type.cast(baket.delete(id));
    }
}
