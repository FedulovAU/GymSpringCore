package gym.repository.trainer;

import gym.data.entity.TrainerEntity;
import gym.data.storage.aggregated.AggregatedValueRule;
import gym.repository.UsernameKey;
import org.springframework.stereotype.Component;

@Component
public class TrainerUsernameAggregateRule implements AggregatedValueRule<TrainerEntity, UsernameKey, Long> {

    @Override
    public UsernameKey aggregateBy(TrainerEntity entity) {
        return new UsernameKey(entity.getUser().getFirstName(), entity.getUser().getLastName());
    }

    @Override
    public Long getValue(TrainerEntity entity) {
        var suffix = entity.getUser().getUserNameSuffix();
        return suffix == null ? 0L : suffix;
    }
}
