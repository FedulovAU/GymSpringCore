package gym.service.security;

public record AuthenticatedUser(String username, UserRole role) { }
