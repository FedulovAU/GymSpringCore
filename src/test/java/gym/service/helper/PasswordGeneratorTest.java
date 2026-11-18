package gym.service.helper;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PasswordGeneratorTest {

    @Test
    void generate_manyPasswords_allUnique() {
        var generator = new PasswordGenerator();
        int count = 10_000;

        Set<String> passwords = new HashSet<>();
        for (int i = 0; i < count; i++) {
            passwords.add(generator.generate());
        }

        assertEquals(count, passwords.size());
    }
}
