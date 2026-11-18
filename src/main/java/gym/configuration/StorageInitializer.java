package gym.configuration;

import gym.data.storage.MemStorageSnapshot;
import gym.data.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StorageInitializer implements BeanPostProcessor {

    private final MemStorageSnapshot snapshot;

    public StorageInitializer(MemStorageSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof Storage<?, ?> storage) {
            snapshot.loadInto(storage);
            log.info("Storage bean '{}' initialized from snapshot.", beanName);
        }
        return bean;
    }
}
