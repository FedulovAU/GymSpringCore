package gym.configuration;

import gym.data.storage.MemStorage;
import gym.data.storage.MemStorageSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Fills the {@link MemStorage} bean with prepared data from the snapshot as soon as Spring creates it,
 * before it's injected into any repository.
 */
@Slf4j
@Component
public class StorageInitializer implements BeanPostProcessor {

    private final MemStorageSnapshot snapshot;

    public StorageInitializer(MemStorageSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof MemStorage storage) {
            snapshot.loadInto(storage);
            log.info("Storage bean '{}' initialized from snapshot.", beanName);
        }
        return bean;
    }
}
