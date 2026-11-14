package gym.services.helpers;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Gives a lock per key (e.g. per trainer ID). The same key always gets the same lock.
 * Usefull for synchronizing operations on the same entity (e.g. updating a trainer)
 * while allowing concurrent operations on different entities.
 */
public class KeyedLocks {
    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    public ReentrantLock get(long key) {
        return locks.computeIfAbsent(key, k -> new ReentrantLock());
    }
}
