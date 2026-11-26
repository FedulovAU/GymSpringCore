package gym.service.helper;

import gym.data.UserEntity;
import gym.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFactoryTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordGenerator passwordGenerator;

    private UserFactory factory;

    @BeforeEach
    void setUp() {
        factory = new UserFactory(
                userRepository,
                passwordGenerator);
        when(passwordGenerator.generate()).thenReturn("secret12345");
    }

    @Test
    void createUser_newName_usesBaseUsername() {
        when(userRepository.findMaxUsernameSuffix("John_Smith")).thenReturn(Optional.empty());

        UserEntity user = factory.createUser("John", "Smith");

        verify(userRepository).findMaxUsernameSuffix("John_Smith");
        var expectedUser = new UserEntity("John", "Smith", "secret12345", true, "John_Smith", "John_Smith", 0);
        assertThat(user).usingRecursiveComparison().isEqualTo(expectedUser);
        verify(passwordGenerator).generate();
    }

    @Test
    void createUser_nameTaken_usesNextSuffix() {
        when(userRepository.findMaxUsernameSuffix("John_Smith")).thenReturn(Optional.of(1));

        UserEntity user = factory.createUser("John", "Smith");

        var expectedUser = new UserEntity("John", "Smith", "secret12345", true, "John_Smith_2", "John_Smith", 2);
        assertThat(user).usingRecursiveComparison().isEqualTo(expectedUser);
        verify(passwordGenerator).generate();
        verify(userRepository).findMaxUsernameSuffix("John_Smith");
    }

    @Test
    void createUser_baseUsernameTaken_appendsSuffixOne() {
        when(userRepository.findMaxUsernameSuffix("John_Smith")).thenReturn(Optional.of(0));

        UserEntity user = factory.createUser("John", "Smith");

        var expectedUser = new UserEntity("John", "Smith", "secret12345", true, "John_Smith_1", "John_Smith", 1);
        assertThat(user).usingRecursiveComparison().isEqualTo(expectedUser);
        verify(passwordGenerator).generate();
        verify(userRepository).findMaxUsernameSuffix("John_Smith");
    }
}
