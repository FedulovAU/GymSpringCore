package gym.data.entities.enums;

public enum TrainingTypeEntity {
    Bodybuilding(1),
    Crossfit(2),
    Fitness(3);

    private final int id;
    public int getId() {
        return id;
    }
    TrainingTypeEntity(int id){
        this.id = id;
    }
}
