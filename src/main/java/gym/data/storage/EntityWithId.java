package gym.data.storage;

/**
 * An interface that represents an entity with an ID.
 * Entities have to implements this interface to be stored in the MemStorage.
 * @param <T>
 */
public interface EntityWithId<T extends EntityWithId<T>> {
    Long id();
    T withId(Long id);
}
