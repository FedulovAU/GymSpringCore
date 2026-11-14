package gym.repositories;

import gym.data.entities.TraineeEntity;
import gym.data.storage.MemStorage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeRepository {

    private final MemStorage storage;

    public TraineeRepository(MemStorage storage){
        this.storage = storage;
    }

    public TraineeEntity add(TraineeEntity trainee){
        return storage.addItem(TraineeEntity.class, trainee);
    }

    public List<TraineeEntity> getAll(){
        return storage.getAllItems(TraineeEntity.class);
    }

    public void updateTrainee(TraineeEntity trainee){
        storage.updateItem(TraineeEntity.class, trainee);
    }

    /**
     * Deactivates a trainee by their ID.
     * @param id the unique identifier of the trainee to be deactivated.
     * @return the updated TraineeEntity with isActive set to false.
     */
    public TraineeEntity deactivateById(long id){
        return storage.getItem(TraineeEntity.class, id)
                .map(trainee -> {
                    var updatedTrainee = trainee.withUser(trainee.user().withActive(false));
                    storage.updateItem(TraineeEntity.class, updatedTrainee);
                    return updatedTrainee;
                })
                .orElseThrow(() -> new IllegalArgumentException("Can't deactivate trainee, entity not found with ID: " + id));
    }

    public Optional<TraineeEntity> getById(long id){
        return storage.getItem(TraineeEntity.class, id);
    }
}
