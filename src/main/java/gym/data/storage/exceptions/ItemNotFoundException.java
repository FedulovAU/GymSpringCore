package gym.data.storage.exceptions;


public class ItemNotFoundException extends RuntimeException {
    private final Class<?> type;
    private final long id;

    public ItemNotFoundException(Class<?> type, long id) {
        super("Item of type " + type.getSimpleName() + " with ID " + id + " does not exist in storage.");
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
