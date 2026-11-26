package gym.service.security;

import gym.service.exception.AuthenticationException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticationContextTest {

    private static final String USERNAME = "John_Smith";

    private final AuthenticationContext context = new AuthenticationContext();

    @Test
    void requireSignedIn_nobodySignedIn_throws() {
        assertThrows(AuthenticationException.class, () -> context.requireSignedIn());
    }

    @Test
    void requireSignedIn_afterSignIn_returnsUser() {
        var user = new AuthenticatedUser(USERNAME, UserRole.TRAINEE);
        context.signIn(user);

        assertEquals(user, context.requireSignedIn());
    }

    @Test
    void getUserByRole_matchingRole_returnsUsername() {
        context.signIn(new AuthenticatedUser(USERNAME, UserRole.TRAINEE));

        assertEquals(USERNAME, context.getUserByRole(UserRole.TRAINEE));
    }

    @Test
    void getUserByRole_otherRole_throws() {
        context.signIn(new AuthenticatedUser(USERNAME, UserRole.TRAINEE));

        assertThrows(AuthenticationException.class, () -> context.getUserByRole(UserRole.TRAINER));
    }

    @Test
    void signOut_afterSignIn_clearsUser() {
        context.signIn(new AuthenticatedUser(USERNAME, UserRole.TRAINER));

        context.signOut();

        assertThrows(AuthenticationException.class, () -> context.requireSignedIn());
    }

    @Test
    void signIn_onOneThread_isNotVisibleOnAnotherThread() {
        context.signIn(new AuthenticatedUser(USERNAME, UserRole.TRAINEE));

        boolean signedInOnOtherThread = CompletableFuture.supplyAsync(this::isSignedIn).join();

        assertFalse(signedInOnOtherThread);
        assertEquals(USERNAME, context.getUserByRole(UserRole.TRAINEE));
    }

    private boolean isSignedIn() {
        try {
            context.requireSignedIn();
            return true;
        } catch (AuthenticationException e) {
            return false;
        }
    }
}
