package gym.services.synchronization;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Gives a lock per key (e.g. per trainer ID). The same key always gets the same lock.
 * Usefull for synchronizing operations on the same entity (e.g. updating a trainer)
 * while allowing concurrent operations on different entities.
 */
public abstract class BaseKeyedLocks<TKey> {
    private final ConcurrentHashMap<TKey, ReentrantLock> locks = new ConcurrentHashMap<>();

    public ReentrantLock get(TKey key) {
        return locks.computeIfAbsent(key, k -> new ReentrantLock());
    }
}
