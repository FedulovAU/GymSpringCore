package gym.data.storage.keyed;

import gym.data.storage.exception.ItemAlreadyExistsException;
import gym.data.storage.exception.ItemNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class KeyStorageTest {

    record Item(Long id, String name) {}

    private KeyStorage<Item, Long> storage;

    @BeforeEach
    void setUp() {
        storage = new KeyStorage<>(Item.class, Item::id);
    }


    @Test
    void add_newKey_storesItemUnderItsKey() {
        Item item = new Item(1L, "a");

        assertEquals(item, storage.add(item));
        assertEquals(Optional.of(item), storage.get(1L));
        assertEquals(List.of(item), storage.getAll());
    }

    @Test
    void add_keyAlreadyTaken_throwsAndKeepsOriginal() {
        Item original = storage.add(new Item(1L, "a"));

        assertThrows(ItemAlreadyExistsException.class, () -> storage.add(new Item(1L, "other")));
        assertEquals(Optional.of(original), storage.get(1L));
    }

    @Test
    void update_existingAndMissingKey_replacesOrThrows() {
        storage.add(new Item(1L, "a"));

        Item updated = storage.update(1L, current -> new Item(current.id(), "updated"));

        assertEquals(new Item(1L, "updated"), updated);
        assertEquals(Optional.of(updated), storage.get(1L));
        assertThrows(ItemNotFoundException.class, () -> storage.update(2L, current -> current));
    }
}
