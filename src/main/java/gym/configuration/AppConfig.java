package gym.configuration;

import gym.data.storage.SnapshotOptions;
import gym.data.storage.MemStorage;
import gym.data.storage.MemStorageSnapshot;
import gym.repositories.TraineeRepository;
import gym.repositories.TrainerRepository;
import gym.services.helpers.UniqueUsernameGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Role;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.env.Environment;

import java.nio.file.Path;
import java.util.stream.Stream;

@Configuration
@ComponentScan(basePackages = "gym")
@PropertySource(value = "classpath:appConfig.properties")
public class AppConfig {

    static final String INPUT_TYPE_PROPERTY = "storage.snapshot.input.type";
    static final String INPUT_LOCATION_PROPERTY = "storage.snapshot.input.location";

    /**
     * Static, because {@link StorageInitializer} (a bean post-processor) depends on it.
     * Marked as infrastructure: it is created early for the post-processor and doesn't need post-processing itself.
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static MemStorageSnapshot memStorageSnapshot(Environment environment) {
        String type = environment.getProperty(INPUT_TYPE_PROPERTY, "none").strip().toLowerCase();

        var options = switch (type) {
            case "none" -> new SnapshotOptions.None();
            case "resources" -> new SnapshotOptions.FromResources(requiredLocation(environment, type));
            case "directory" -> new SnapshotOptions.FromDirectory(Path.of(requiredLocation(environment, type)));
            default -> throw new IllegalStateException("Unknown " + INPUT_TYPE_PROPERTY + ": '" + type
                    + "'. Expected one of: resources, directory, none.");
        };
        return new MemStorageSnapshot(options);
    }

    /**
     * Seeded with the usernames already in storage, so loaded users' usernames are never generated again.
     */
    @Bean
    public UniqueUsernameGenerator uniqueUsernameGenerator(
            TraineeRepository traineeRepository,
            TrainerRepository trainerRepository) {
        var existingUserNames = Stream.concat(
                        traineeRepository.getAll().stream().map(trainee -> trainee.user().userName()),
                        trainerRepository.getAll().stream().map(trainer -> trainer.user().userName())
                ).toList();
        return new UniqueUsernameGenerator(existingUserNames);
    }

    private static String requiredLocation(Environment environment, String type) {
        String location = environment.getProperty(INPUT_LOCATION_PROPERTY);
        if (location == null || location.isBlank()) {
            throw new IllegalStateException(INPUT_LOCATION_PROPERTY + " must be set when "
                    + INPUT_TYPE_PROPERTY + " is '" + type + "'.");
        }
        return location.strip();
    }
}
