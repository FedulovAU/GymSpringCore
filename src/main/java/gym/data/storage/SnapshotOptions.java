package gym.data.storage;

import java.nio.file.Path;

/**
 * Where {@link MemStorageSnapshot} loads snapshot files from.
 */
public sealed interface SnapshotOptions {

    /**
     * Snapshot files in a directory on disk.
     * @param path the directory containing the snapshot files.
     */
    record FromDirectory(Path path) implements SnapshotOptions {}

    /**
     * Snapshot files on the classpath, e.g. in src/main/resources. Works from a jar too.
     * @param classPath the classpath folder containing the snapshot files, e.g. "snapshot".
     */
    record FromResources(String classPath) implements SnapshotOptions {}

    /**
     * No snapshot: storage starts empty.
     */
    record None() implements SnapshotOptions {}
}
