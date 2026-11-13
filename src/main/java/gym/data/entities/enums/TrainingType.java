package gym.data.entities.enums;

public enum TrainingType {
    Bodybuilding(1),
    Crossfit(2),
    Fitness(3);

    private final int id;
    public int getId() {
        return id;
    }
    TrainingType(int id){
        this.id = id;
    }
}
