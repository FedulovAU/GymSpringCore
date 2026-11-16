package gym.data.common;

import lombok.With;

@With
public record UserData(
        String firstName,
        String lastName,
        String userName,
        String password,
        boolean isActive) {}
