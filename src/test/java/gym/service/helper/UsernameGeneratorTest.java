package gym.service.helper;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;


class UsernameGeneratorTest {

    private final UsernameGenerator generator = new UsernameGenerator();

    @Test
    void generate_noExistingSuffix_usesBaseNameAndSuffixZero() {
        assertEquals(new UsernameGenerator.Result("John_Smith", 0L),
                generator.generate("John", "Smith", Optional.empty()));
    }

    @Test
    void generate_baseNameTaken_appendsSuffixOne() {
        // Suffix 0 is the base name without a number.
        assertEquals(new UsernameGenerator.Result("John_Smith_1", 1L),
                generator.generate("John", "Smith", Optional.of(0L)));
    }

    @Test
    void generate_existingSuffix_appendsNextSuffix() {
        assertEquals(new UsernameGenerator.Result("John_Smith_5", 5L),
                generator.generate("John", "Smith", Optional.of(4L)));
    }
}
