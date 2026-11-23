package gym.data.storage.aggregated;

import gym.data.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AggregatedValueStorageTest {

    record Item(Long id, String name, Long suffix) {}

    private static final AggregatedValueRule<Item, String, Long> RULE = new AggregatedValueRule<>() {
        @Override
        public String aggregateBy(Item entity) {
            return entity.name();
        }

        @Override
        public Long getValue(Item entity) {
            return entity.suffix();
        }
    };

    @Mock
    private Storage<Item, Long> innerStorage;

    private AggregatedValueStorage<Item, Long, String, Long> storage;

    @BeforeEach
    void setUp() {
        storage = new AggregatedValueStorage<>(innerStorage, RULE);
    }


    @Test
    void add_storesItemAndRecordsItsValueForItsAggregateKeyOnly() {
        when(innerStorage.add(any())).then(returnsFirstArg());

        var item = new Item(null, "a", 0L);
        assertSame(item, storage.add(item));
        storage.add(new Item(null, "a", 1L));

        assertEquals(Optional.of(1L), storage.getAggregatedValue("a"));
        assertEquals(Optional.empty(), storage.getAggregatedValue("b"));
        verify(innerStorage).add(item);
        verify(innerStorage).add(new Item(null, "a", 1L));
        verifyNoMoreInteractions(innerStorage);
    }

    @Test
    void put_keepsHighestValue() {
        storage.put(new Item(1L, "a", 5L));
        storage.put(new Item(2L, "a", 2L));

        assertEquals(Optional.of(5L), storage.getAggregatedValue("a"));
        verify(innerStorage).put(new Item(1L, "a", 5L));
        verify(innerStorage).put(new Item(2L, "a", 2L));
        verifyNoMoreInteractions(innerStorage);
    }

    @Test
    void add_innerStorageThrows_valueNotRecorded() {
        when(innerStorage.add(any())).thenThrow(new IllegalStateException("rejected"));

        assertThrows(IllegalStateException.class, () -> storage.add(new Item(null, "a", 0L)));

        assertEquals(Optional.empty(), storage.getAggregatedValue("a"));
        verify(innerStorage).add(new Item(null, "a", 0L));
        verifyNoMoreInteractions(innerStorage);
    }
}
