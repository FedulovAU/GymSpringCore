package gym.data.storage.aggregated;

import gym.data.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        @Override
        public Item mergeOnAdd(Item entity, Long existedValue) {
            long suffix = existedValue == null ? 0L : existedValue + 1;
            return new Item(entity.id(), entity.name(), suffix);
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
    void add_sameAggregateKeyGetsNextValue_otherAggregateKeyStartsOver() {
        when(innerStorage.add(any())).then(returnsFirstArg());

        assertEquals(0L, storage.add(new Item(null, "a", null)).suffix());
        assertEquals(1L, storage.add(new Item(null, "a", null)).suffix());
        assertEquals(0L, storage.add(new Item(null, "b", null)).suffix());

        verify(innerStorage).add(new Item(null, "a", 0L));
        verify(innerStorage).add(new Item(null, "a", 1L));
        verify(innerStorage).add(new Item(null, "b", 0L));
        verifyNoMoreInteractions(innerStorage);
    }

    @Test
    void put_thenAdd_continuesAfterHighestPutValue() {
        when(innerStorage.add(any())).then(returnsFirstArg());

        storage.put(new Item(1L, "a", 5L));
        storage.put(new Item(2L, "a", 2L));
        Item added = storage.add(new Item(null, "a", null));

        assertEquals(6L, added.suffix());
        verify(innerStorage).put(new Item(1L, "a", 5L));
        verify(innerStorage).put(new Item(2L, "a", 2L));
        verify(innerStorage).add(new Item(null, "a", 6L));
        verifyNoMoreInteractions(innerStorage);
    }

    @Test
    void add_innerStorageThrows_valueNotAdvanced() {
        when(innerStorage.add(any()))
                .thenThrow(new IllegalStateException("rejected"))
                .then(returnsFirstArg());

        assertThrows(IllegalStateException.class, () -> storage.add(new Item(null, "a", null)));
        Item added = storage.add(new Item(null, "a", null));

        assertEquals(0L, added.suffix());
        verify(innerStorage, times(2)).add(new Item(null, "a", 0L));
        verifyNoMoreInteractions(innerStorage);
    }
}
