package gym.repository.user;

import gym.data.entity.UserEntity;
import gym.data.storage.Storage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Repository
public class UserRepository {

    private final Storage<UserEntity, Long> storage;

    public UserRepository(Storage<UserEntity, Long> storage){
        this.storage = storage;
    }

    public UserEntity add(UserEntity user){
        return storage.add(user);
    }

    public List<UserEntity> getAll(){
        return storage.getAll();
    }

    public UserEntity update(long id, UnaryOperator<UserEntity> updater){
        return storage.update(id, updater);
    }

    public void deleteById(long id){
        storage.delete(id);
    }

    public Optional<UserEntity> getById(long id){
        return storage.get(id);
    }
}
