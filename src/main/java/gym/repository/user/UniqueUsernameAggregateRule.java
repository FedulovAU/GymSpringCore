package gym.repository.user;

import gym.data.entity.UserEntity;
import gym.data.storage.aggregated.AggregatedValueRule;
import org.springframework.stereotype.Component;

@Component
public class UniqueUsernameAggregateRule
        implements AggregatedValueRule<UserEntity, UniqueUsernameAggregateRule.Key, Long> {

    @Override
    public Key aggregateBy(UserEntity entity) {
        return new Key(entity.getFirstName(), entity.getLastName());
    }

    @Override
    public Long getValue(UserEntity entity) {
        var suffix = entity.getUserNameSuffix();
        return suffix == null ? 0L : suffix;
    }

    @Override
    public UserEntity mergeOnAdd(UserEntity entity, Long existedValue) {
        String userName;
        long suffix;

        if(existedValue == null){
            userName = entity.getFirstName() + "_" + entity.getLastName();
            suffix = 0L;
        } else{
            suffix = existedValue + 1;
            userName = entity.getFirstName() + "_" + entity.getLastName() + "_" + suffix;
        }

        return entity.withUserName(userName).withUserNameSuffix(suffix);
    }

      public record Key(String firstName, String lastName) {}

}
