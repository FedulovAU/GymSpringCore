package gym.data.storage;

import java.util.*;

/**
 * Stores items in memory grouped by their class type.
 * Each class type has its own "baket" of items, which is a map of IDs to objects.
 */
public class MemStorage {

    /**
     * Stores items of type EntityWithId<?> in a map, provide CRUD operations for them.
     */
    public static class Baket {
        private final Map<Long, EntityWithId<?>> items;
        private long nextId;

        public Baket(){
            items = new HashMap<>();
            nextId = 0;
        }

        /**
         * Adds an item to the baket and assigns it a unique ID.
         * @param item, the item to add, item.id must be null, indicating that the item is new.
         *              Providing item with id != null will result in an exception.
         * @return the item that was added, with its ID set to the newly assigned unique ID.
         */
        public EntityWithId<?> add(EntityWithId<?> item){
            if(item.id() != null){
                throw new IllegalArgumentException("Item already has an ID: " + item.id() +
                        ". Cannot add to storage, provide item with id == null.");
            }
            long id = nextId++;
            EntityWithId<?> newItem = item.withId(id);
            items.put(id, newItem);
            return newItem;
        }

        /**
         * Updates an existing item in the baket.
         * @param item, must have a non-null ID that corresponds to an existing item in the baket.
         */
        public void update(EntityWithId<?> item){
            if(item.id() == null){
                throw new IllegalArgumentException("Item does not have an ID: " + item.id() +
                        ". Cannot update storage, provide item with id != null.");
            }

            if(!items.containsKey(item.id())){
                throw new IllegalArgumentException("Item with ID " + item.id() + " does not exist in storage.");
            }
            items.put(item.id(), item);
        }

        public void delete(long id){
            if(!items.containsKey(id)){
                throw new IllegalArgumentException("Item with ID " + id + " does not exist in storage.");
            }
            items.remove(id);
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

    private final Map<Class<?>, Baket> bakets;

    public MemStorage(){
        bakets = new HashMap<>();
    }

    /**
     * Stores an item in the appropriate baket based on its class type.
     * @param type, item class type, used to determine which baket to store the item in.
     * @param item, item.id must be null, indicating that the item is new. Providing item with id != null will result in an exception.
     * @param <T>, item class type, must implement EntityWithId<T> interface.
     */
    public <T extends EntityWithId<T>> void addItem(Class<T> type, T item){
        bakets
                .computeIfAbsent(type, t -> new Baket())
                .add(item);
    }

    /**
     * Retrieves an item from the appropriate baket based on its class type and ID.
     * @param type, item class type, used to determine which baket to retrieve the item from.
     * @param id, ID of the item to retrieve.
     * @param <T>, item class type, must implement EntityWithId<T> interface.
     * @return an Optional containing the item if found, or an empty Optional if not found.
     */
    public <T> Optional<T> getItem(Class<T> type, Long id){
        Baket baket = bakets.get(type);
        if (baket == null) return Optional.empty();
        return baket.get(id).map(type::cast);
    }


    /**
     * Updates an item in the storage bucket corresponding to its class type.
     *
     * @param type the class type used to locate the item's storage bucket
     * @param item the item to update
     * @throws IllegalArgumentException if no storage bucket exists for {@code type}
     */
    public void updateItem(Class<?> type, EntityWithId<?> item){
        Baket baket = bakets.get(type);
        if (baket == null) {
            throw new IllegalArgumentException("No items of type " + type.getName() + " found in storage.");
        }
        baket.update(item);
    }

    /**
     * Deletes an item from the storage bucket corresponding to its class type.
     *
     * @param type the class type used to locate the item's storage bucket
     * @param id the ID of the item to delete
     * @throws IllegalArgumentException if no storage bucket exists for {@code type}
     */
    public void deleteItem(Class<?> type, long id){
        Baket baket = bakets.get(type);
        if (baket == null) {
            throw new IllegalArgumentException("No items of type " + type.getName() + " found in storage.");
        }
        baket.delete(id);
    }
}
