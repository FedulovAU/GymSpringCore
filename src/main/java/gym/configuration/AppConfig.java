package gym.configuration;

import gym.data.entity.TraineeEntity;
import gym.data.entity.TrainerEntity;
import gym.data.entity.TrainingEntity;
import gym.data.storage.Storage;
import gym.data.storage.aggregated.AggregatedValueStorage;
import gym.data.storage.MemStorageSnapshot;
import gym.data.storage.autoincrement.AutoIncrementStorage;
import gym.data.storage.keyed.KeyStorage;
import gym.repository.trainee.TraineeKeyAutoincrement;
import gym.repository.trainee.TraineeUsernameAggregateRule;
import gym.repository.trainer.TrainerKeyAutoincrement;
import gym.repository.trainer.TrainerUsernameAggregateRule;
import gym.repository.UsernameKey;
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
    public AggregatedValueStorage<TraineeEntity, Long, UsernameKey, Long> traineeStorage(
            TraineeUsernameAggregateRule usernameAggregateRule, TraineeKeyAutoincrement keyAutoincrement) {
        var keyStorage = new KeyStorage<TraineeEntity, Long>(TraineeEntity.class, TraineeEntity::getId);
        var autoIncrementStorage = new AutoIncrementStorage<>(keyStorage, keyAutoincrement);
        return new AggregatedValueStorage<>(autoIncrementStorage, usernameAggregateRule);
    }

    @Bean
    public AggregatedValueStorage<TrainerEntity, Long, UsernameKey, Long> trainerStorage(
            TrainerUsernameAggregateRule usernameAggregateRule, TrainerKeyAutoincrement keyAutoincrement) {
        var keyStorage = new KeyStorage<TrainerEntity, Long>(TrainerEntity.class, TrainerEntity::getId);
        var autoIncrementStorage = new AutoIncrementStorage<>(keyStorage, keyAutoincrement);
        return new AggregatedValueStorage<>(autoIncrementStorage, usernameAggregateRule);
    }


    @Bean
    public Storage<TrainingEntity, TrainingEntity.CompositeKey> trainingStorage() {
        return new KeyStorage<>(
                TrainingEntity.class,
                t -> new TrainingEntity.CompositeKey(t.getTrainerId(), t.getTraineeId()));
    }
}
