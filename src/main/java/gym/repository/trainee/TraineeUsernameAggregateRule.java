package gym.repository.trainee;

import gym.data.entity.TraineeEntity;
import gym.data.storage.aggregated.AggregatedValueRule;
import gym.repository.UsernameKey;
import org.springframework.stereotype.Component;

@Component
public class TraineeUsernameAggregateRule implements AggregatedValueRule<TraineeEntity, UsernameKey, Long> {

    @Override
    public UsernameKey aggregateBy(TraineeEntity entity) {
        return new UsernameKey(entity.getUser().getFirstName(), entity.getUser().getLastName());
    }

    @Override
    public Long getValue(TraineeEntity entity) {
        var suffix = entity.getUser().getUserNameSuffix();
        return suffix == null ? 0L : suffix;
    }
}
