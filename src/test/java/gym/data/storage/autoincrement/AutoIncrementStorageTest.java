package gym.data.storage.autoincrement;

import gym.data.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutoIncrementStorageTest {

    record Item(Long id, String name) {}

    private static final KeySetter<Item, Long> KEY_SETTER = new KeySetter<>() {
        @Override
        public Item setKey(Item entity, Long newKey) {
            return new Item(newKey, entity.name());
        }

        @Override
        public Long incrementAndGet(AtomicReference<Long> key) {
            return key.updateAndGet(current -> current == null ? 1L : current + 1);
        }

        @Override
        public void setMaxKey(Item entity, AtomicReference<Long> prev) {
            prev.accumulateAndGet(entity.id(), (current, id) -> current == null || id > current ? id : current);
        }
    };

    @Mock
    private Storage<Item, Long> innerStorage;

    private AutoIncrementStorage<Item, Long> storage;

    @BeforeEach
    void setUp() {
        storage = new AutoIncrementStorage<>(innerStorage, KEY_SETTER);
    }


    @Test
    void add_assignsSequentialKeysAndPassesItemsToInnerStorage() {
        when(innerStorage.add(any())).then(returnsFirstArg());

        assertEquals(new Item(1L, "a"), storage.add(new Item(null, "a")));
        assertEquals(new Item(2L, "b"), storage.add(new Item(null, "b")));

        verify(innerStorage).add(new Item(1L, "a"));
        verify(innerStorage).add(new Item(2L, "b"));
        verifyNoMoreInteractions(innerStorage);
    }

    @Test
    void put_thenAdd_continuesAfterHighestPutKey() {
        when(innerStorage.add(any())).then(returnsFirstArg());

        storage.put(new Item(7L, "b"));
        storage.put(new Item(3L, "a"));
        Item added = storage.add(new Item(null, "new"));

        assertEquals(8L, added.id());
        verify(innerStorage).put(new Item(7L, "b"));
        verify(innerStorage).put(new Item(3L, "a"));
        verify(innerStorage).add(new Item(8L, "new"));
        verifyNoMoreInteractions(innerStorage);
    }

    @Test
    void getAndDelete_delegateToInnerStorage() {
        Item item = new Item(1L, "a");
        when(innerStorage.get(1L)).thenReturn(Optional.of(item));

        assertEquals(Optional.of(item), storage.get(1L));
        storage.delete(1L);

        verify(innerStorage).get(1L);
        verify(innerStorage).delete(1L);
        verifyNoMoreInteractions(innerStorage);
    }
}
