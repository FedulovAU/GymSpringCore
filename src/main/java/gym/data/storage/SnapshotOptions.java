package gym.data.storage;

import java.nio.file.Path;

public sealed interface SnapshotOptions {

    record FromDirectory(Path path) implements SnapshotOptions {}

    record FromResources(String classPath) implements SnapshotOptions {}

    record FromEmptyStorage() implements SnapshotOptions {}
}
