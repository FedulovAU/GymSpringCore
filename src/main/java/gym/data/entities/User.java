package gym.data.entities;

public record User(
        String firstName,
        String lastName,
        String userName,
        String password,
        boolean isActive) {}
