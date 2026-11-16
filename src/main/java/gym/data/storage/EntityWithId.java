package gym.data.storage;

public interface EntityWithId<T extends EntityWithId<T>> {
    Long id();
    T withId(Long id);
}
