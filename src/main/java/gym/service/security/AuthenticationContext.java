package gym.service.security;

import gym.service.exception.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationContext {
    private final ThreadLocal<AuthenticatedUser> currentUser = new ThreadLocal<>();

    public void signIn(AuthenticatedUser user) {
        currentUser.set(user);
    }

    public void signOut() {
        currentUser.remove();
    }

    public AuthenticatedUser requireSignedIn() {
        AuthenticatedUser user = currentUser.get();
        if (user == null) {
            throw new AuthenticationException("Sign in is required.");
        }
        return user;
    }

    public String getUserByRole(UserRole role) {
        AuthenticatedUser user = requireSignedIn();
        if (user.role() != role) {
            throw new AuthenticationException("Signed in user is not a " + role.name().toLowerCase() + ".");
        }
        return user.username();
    }
}
