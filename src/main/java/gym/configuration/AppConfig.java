package gym.configuration;

import gym.data.entity.TraineeEntity;
import gym.data.entity.TrainerEntity;
import gym.data.entity.TrainingEntity;
import gym.data.entity.UserEntity;
import gym.data.storage.Storage;
import gym.data.storage.aggregated.AggregatedValueStorage;
import gym.data.storage.MemStorageSnapshot;
import gym.data.storage.autoincrement.AutoIncrementStorage;
import gym.data.storage.keyed.KeyStorage;
import gym.repository.user.UniqueUsernameAggregateRule;
import gym.repository.user.UserKeyAutoincrement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Role;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.env.Environment;

@Configuration
@ComponentScan(basePackages = "gym")
@PropertySource(value = "classpath:appConfig.properties")
public class AppConfig {

    static final String INPUT_LOCATION_PROPERTY = "storage.snapshot.location";

    /**
     * Static, because {@link StorageInitializer} (a bean post-processor) depends on it.
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static MemStorageSnapshot memStorageSnapshot(Environment environment) {
        String location = environment.getProperty(INPUT_LOCATION_PROPERTY);
        return new MemStorageSnapshot(location);
    }

    @Bean
    public Storage<TraineeEntity, Long> traineeStorage() {
        return new KeyStorage<>(TraineeEntity.class, TraineeEntity::getId);
    }

    @Bean
    public Storage<TrainerEntity, Long> trainerStorage() {
        return new KeyStorage<>(TrainerEntity.class, TrainerEntity::getId);
    }

    @Bean
    public Storage<UserEntity, Long> userStorage(UniqueUsernameAggregateRule uniqueUsernameAggregateRule, UserKeyAutoincrement keyAutoincrement) {
        var keyStorage = new KeyStorage<UserEntity, Long>(UserEntity.class, UserEntity::getId);
        var autoIncrementStorage = new AutoIncrementStorage<UserEntity, Long>(keyStorage, keyAutoincrement);
        return new AggregatedValueStorage<>(autoIncrementStorage, uniqueUsernameAggregateRule);
    }


    @Bean
    public Storage<TrainingEntity, TrainingEntity.CompositeKey> trainingStorage() {
        return new KeyStorage<>(
                TrainingEntity.class,
                t -> new TrainingEntity.CompositeKey(t.getTrainerId(), t.getTraineeId()));
    }
}
