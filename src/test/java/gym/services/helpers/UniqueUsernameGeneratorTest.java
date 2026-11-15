package gym.services.helpers;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UniqueUsernameGeneratorTest {

    @Test
    void generate_noExistingUsers_firstHasNoSuffixThenNumbered() {
        var generator = new UniqueUsernameGenerator();

        assertEquals("John_Smith", generator.generateUniqueUserName("John", "Smith"));
        assertEquals("John_Smith_1", generator.generateUniqueUserName("John", "Smith"));
    }

    @Test
    void generate_existingBaseUserName_continuesWithSuffix() {
        var generator = new UniqueUsernameGenerator(List.of("John_Smith"));

        assertEquals("John_Smith_1", generator.generateUniqueUserName("John", "Smith"));
    }

    @Test
    void generate_gapInExistingSuffixes_continuesAfterHighest() {
        // John_Smith_1 was deleted: its number must not be handed out again either.
        var generator = new UniqueUsernameGenerator(List.of("John_Smith_2", "John_Smith"));

        assertEquals("John_Smith_3", generator.generateUniqueUserName("John", "Smith"));
    }

    @Test
    void generate_otherNameExisting_doesNotAffectNewName() {
        var generator = new UniqueUsernameGenerator(List.of("Anna_Lee_4"));

        assertEquals("John_Smith", generator.generateUniqueUserName("John", "Smith"));
    }
}
