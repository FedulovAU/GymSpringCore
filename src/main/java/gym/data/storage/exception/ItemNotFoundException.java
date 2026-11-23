package gym.data.storage.exception;


import lombok.Getter;

@Getter
public class ItemNotFoundException extends RuntimeException {
    private final String type;
    private final String key;

    public ItemNotFoundException(String type, String key) {
        super("Item of type " + type + " with ID " + key + " does not exist in storage.");
        this.type = type;
        this.key = key;
    }

}
