package gym.services.synchronization;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


public abstract class BaseKeyedLocks<TKey> {
    private final ConcurrentHashMap<TKey, ReentrantLock> locks = new ConcurrentHashMap<>();

    public ReentrantLock get(TKey key) {
        return locks.computeIfAbsent(key, k -> new ReentrantLock());
    }
}
