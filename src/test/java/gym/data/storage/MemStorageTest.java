package gym.data.storage;

import gym.data.storage.exceptions.ItemAlreadyExistsException;
import gym.data.storage.exceptions.ItemNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MemStorageTest {

    record EntityA(Long id, String name) implements EntityWithId<EntityA> {
        @Override
        public EntityA withId(Long id) {
            return new EntityA(id, name);
        }
    }

    record EntityB(Long id, String name) implements EntityWithId<EntityB> {
        @Override
        public EntityB withId(Long id) {
            return new EntityB(id, name);
        }
    }

    private MemStorage storage;

    @BeforeEach
    void setUp() {
        storage = new MemStorage();
    }

    // ---------- addItem ----------

    @Test
    void addItem_noBaket_returnsItemWithIdAndStoresIt() {
        EntityA added = storage.addItem(EntityA.class, new EntityA(null, "a"));

        assertNotNull(added.id());
        assertEquals("a", added.name());
        assertEquals(Optional.of(added), storage.getItem(EntityA.class, added.id()));
    }

    @Test
    void addItem_baketExists_returnsItemWithIdAndStoresIt() {
        EntityA temp = storage.addItem(EntityA.class, new EntityA(null, "temp"));
        storage.deleteItem(EntityA.class, temp.id());

        EntityA added = storage.addItem(EntityA.class, new EntityA(null, "a"));

        assertNotNull(added.id());
        assertEquals("a", added.name());
        assertEquals(Optional.of(added), storage.getItem(EntityA.class, added.id()));
    }

    @Test
    void addItem_itemHasId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> storage.addItem(EntityA.class, new EntityA(1L, "a")));
    }

    @Test
    void addItem_multipleItems_assignsUniqueIds() {
        int count = 10;
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < count; i++) {
            ids.add(storage.addItem(EntityA.class, new EntityA(null, "a" + i)).id());
        }

        assertEquals(count, ids.size());
    }

    // ---------- updateItem ----------

    @Test
    void updateItem_itemExists_returnsAndStoresNewVersion() {
        EntityA added = storage.addItem(EntityA.class, new EntityA(null, "a"));

        EntityA updated = storage.updateItem(EntityA.class, added.id(),
                current -> new EntityA(current.id(), "updated"));

        assertEquals(new EntityA(added.id(), "updated"), updated);
        assertEquals(Optional.of(updated), storage.getItem(EntityA.class, added.id()));
    }

    @Test
    void updateItem_noBaket_throwsItemNotFoundException() {
        ItemNotFoundException ex = assertThrows(ItemNotFoundException.class,
                () -> storage.updateItem(EntityA.class, 1L, current -> current));

        assertEquals(EntityA.class, ex.getType());
        assertEquals(1L, ex.getId());
    }

    @Test
    void updateItem_baketExistsButNoItem_throwsItemNotFoundException() {
        EntityA added = storage.addItem(EntityA.class, new EntityA(null, "a"));
        long missingId = added.id() + 1;

        ItemNotFoundException ex = assertThrows(ItemNotFoundException.class,
                () -> storage.updateItem(EntityA.class, missingId, current -> current));

        assertEquals(EntityA.class, ex.getType());
        assertEquals(missingId, ex.getId());
    }

    @Test
    void updateItem_updaterChangesId_throwsIllegalArgumentExceptionAndKeepsItem() {
        EntityA added = storage.addItem(EntityA.class, new EntityA(null, "a"));

        assertThrows(IllegalArgumentException.class,
                () -> storage.updateItem(EntityA.class, added.id(),
                        current -> new EntityA(current.id() + 100, "updated")));

        assertEquals(Optional.of(added), storage.getItem(EntityA.class, added.id()));
    }

    @Test
    void updateItem_updaterReturnsNull_throwsIllegalArgumentExceptionAndKeepsItem() {
        EntityA added = storage.addItem(EntityA.class, new EntityA(null, "a"));

        assertThrows(IllegalArgumentException.class,
                () -> storage.updateItem(EntityA.class, added.id(), current -> null));

        assertEquals(Optional.of(added), storage.getItem(EntityA.class, added.id()));
    }

    // ---------- deleteItem ----------

    @Test
    void deleteItem_itemExists_removesIt() {
        EntityA added = storage.addItem(EntityA.class, new EntityA(null, "a"));

        storage.deleteItem(EntityA.class, added.id());

        assertEquals(Optional.empty(), storage.getItem(EntityA.class, added.id()));
    }

    @Test
    void deleteItem_noBaket_throwsItemNotFoundException() {
        ItemNotFoundException ex = assertThrows(ItemNotFoundException.class,
                () -> storage.deleteItem(EntityA.class, 1L));

        assertEquals(EntityA.class, ex.getType());
        assertEquals(1L, ex.getId());
    }

    @Test
    void deleteItem_baketExistsButNoItem_throwsItemNotFoundException() {
        EntityA added = storage.addItem(EntityA.class, new EntityA(null, "a"));
        long missingId = added.id() + 1;

        ItemNotFoundException ex = assertThrows(ItemNotFoundException.class,
                () -> storage.deleteItem(EntityA.class, missingId));

        assertEquals(EntityA.class, ex.getType());
        assertEquals(missingId, ex.getId());
    }

    // ---------- getItem ----------

    @Test
    void getItem_itemExists_returnsIt() {
        EntityA added = storage.addItem(EntityA.class, new EntityA(null, "a"));

        assertEquals(Optional.of(added), storage.getItem(EntityA.class, added.id()));
    }

    @Test
    void getItem_noBaket_returnsEmpty() {
        assertEquals(Optional.empty(), storage.getItem(EntityA.class, 1L));
    }

    @Test
    void getItem_baketExistsButNoItem_returnsEmpty() {
        EntityA added = storage.addItem(EntityA.class, new EntityA(null, "a"));

        assertEquals(Optional.empty(), storage.getItem(EntityA.class, added.id() + 1));
    }

    // ---------- getAllItems ----------

    @Test
    void getAllItems_itemsOfTwoTypes_returnsOnlyRequestedType() {
        Set<EntityA> expected = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            expected.add(storage.addItem(EntityA.class, new EntityA(null, "a" + i)));
            storage.addItem(EntityB.class, new EntityB(null, "b" + i));
        }

        List<EntityA> result = storage.getAllItems(EntityA.class);

        assertEquals(3, result.size());
        assertEquals(expected, new HashSet<>(result));
    }

    @Test
    void getAllItems_noBaket_returnsEmptyList() {
        assertTrue(storage.getAllItems(EntityA.class).isEmpty());
    }

    @Test
    void getAllItems_baketExistsButNoItems_returnsEmptyList() {
        EntityA temp = storage.addItem(EntityA.class, new EntityA(null, "temp"));
        storage.deleteItem(EntityA.class, temp.id());

        assertTrue(storage.getAllItems(EntityA.class).isEmpty());
    }

    // ---------- restoreItems ----------

    @Test
    void restoreItems_itemsWithIds_keepsIdsAndStoresThem() {
        storage.restoreItems(EntityA.class, List.of(new EntityA(3L, "a"), new EntityA(7L, "b")));

        assertEquals(Optional.of(new EntityA(3L, "a")), storage.getItem(EntityA.class, 3L));
        assertEquals(Optional.of(new EntityA(7L, "b")), storage.getItem(EntityA.class, 7L));
    }

    @Test
    void restoreItems_thenAddItem_assignsIdAboveHighestRestored() {
        storage.restoreItems(EntityA.class, List.of(new EntityA(7L, "b"), new EntityA(3L, "a")));

        EntityA added = storage.addItem(EntityA.class, new EntityA(null, "new"));

        assertEquals(8L, added.id());
    }

    @Test
    void restoreItems_idAlreadyTaken_throwsItemAlreadyExistsException() {
        EntityA added = storage.addItem(EntityA.class, new EntityA(null, "a"));

        assertThrows(ItemAlreadyExistsException.class,
                () -> storage.restoreItems(EntityA.class, List.of(new EntityA(added.id(), "other"))));
    }

    @Test
    void restoreItems_itemWithoutId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> storage.restoreItems(EntityA.class, List.of(new EntityA(null, "a"))));
    }
}
