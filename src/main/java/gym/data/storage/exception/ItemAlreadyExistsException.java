package gym.data.storage.exception;

import lombok.Getter;

@Getter
public class ItemAlreadyExistsException extends RuntimeException {
    private final String type;
    private final String key;

    public ItemAlreadyExistsException(String type, String key) {
        super("Item of type " + type + " with ID " + key + " already exists in storage. " +
                "Provide an item with id == null to add a new one.");
        this.type = type;
        this.key = key;
    }
}
