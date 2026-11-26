package gym.service.helper;

import gym.data.UserEntity;
import gym.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {

    private final UserRepository userRepository;

    public UserFactory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity createUser(String firstName, String lastName, String password) {
        String baseUsername = firstName + "_" + lastName;
        int suffix = userRepository.findMaxUsernameSuffix(baseUsername).map(max -> max + 1).orElse(0);
        String username = suffix == 0 ? baseUsername : baseUsername + "_" + suffix;

        return new UserEntity(
                firstName,
                lastName,
                password,
                true,
                username,
                baseUsername,
                suffix);
    }
}
