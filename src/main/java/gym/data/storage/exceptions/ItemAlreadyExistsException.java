package gym.data.storage.exceptions;


public class ItemAlreadyExistsException extends RuntimeException {
    private final Class<?> type;
    private final long id;

    public ItemAlreadyExistsException(Class<?> type, long id) {
        super("Item of type " + type.getSimpleName() + " with ID " + id + " already exists in storage. " +
                "Provide an item with id == null to add a new one.");
        this.type = type;
        this.id = id;
    }

    public Class<?> getType() {
        return type;
    }

    public long getId() {
        return id;
    }
}
