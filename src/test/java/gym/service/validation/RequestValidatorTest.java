package gym.service.validation;

import gym.enums.TrainingType;
import gym.dto.request.AddTraining;
import gym.dto.request.CreateTrainee;
import gym.dto.request.CreateTrainer;
import gym.dto.request.UpdateTrainee;
import gym.dto.request.UpdateTrainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringJUnitConfig(RequestValidatorTest.Config.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RequestValidatorTest {

    @Configuration
    @Import(RequestValidator.class)
    @PropertySource("classpath:appConfig.properties")
    static class Config {}

    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 5, 17);
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalDateTime NEXT_WEEK = LocalDate.now().plusWeeks(1).atTime(10, 0);
    private static final Duration ONE_HOUR = Duration.ofHours(1);

    private final RequestValidator validator;
    private final int minTraineeAge;
    private final int scheduleGapMinutesMin;
    private final int trainingDurationMinutesMin;

    RequestValidatorTest(
            RequestValidator validator,
            @Value("${minTraineeAge}") int minTraineeAge,
            @Value("${scheduleGapMinutesMin}") int scheduleGapMinutesMin,
            @Value("${trainingDurationMinutesMin}") int trainingDurationMinutesMin) {
        this.validator = validator;
        this.minTraineeAge = minTraineeAge;
        this.scheduleGapMinutesMin = scheduleGapMinutesMin;
        this.trainingDurationMinutesMin = trainingDurationMinutesMin;
    }

    @Test
    void validateCreateTraineeRequest_valid_passes() {
        var request = new CreateTrainee("José", "Smith", BIRTH_DATE, "1 Main St");
        assertDoesNotThrow(() -> validator.validateCreateTraineeRequest(request));
    }

    @Test
    void validateCreateTraineeRequest_optionalFieldsMissing_passes() {
        var request = new CreateTrainee("John", "Smith", null, null);
        assertDoesNotThrow(() -> validator.validateCreateTraineeRequest(request));
    }

    @Test
    void validateCreateTraineeRequest_exactlyMinAge_passes() {
        var birthDate = LocalDate.now().minusYears(minTraineeAge);
        var request = new CreateTrainee("John", "Smith", birthDate, null);
        assertDoesNotThrow(() -> validator.validateCreateTraineeRequest(request));
    }

    static Stream<Arguments> invalidCreateTrainee() {
        var tooYoungBirthDate = LocalDate.now().minusYears(5).plusDays(1);
        return Stream.of(
                arguments("missing first name", new CreateTrainee(null, "Smith", BIRTH_DATE, null)),
                arguments("first name with digit", new CreateTrainee("John1", "Smith", BIRTH_DATE, null)),
                arguments("last name with underscore", new CreateTrainee("John", "Smith_2", BIRTH_DATE, null)),
                arguments("younger than min age", new CreateTrainee("John", "Smith", tooYoungBirthDate, null)),
                arguments("future birth date", new CreateTrainee("John", "Smith", TOMORROW, null)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateTrainee")
    void validateCreateTraineeRequest_invalid_throwsIllegalArgument(String rule, CreateTrainee request) {
        assertThrows(IllegalArgumentException.class, () -> validator.validateCreateTraineeRequest(request));
    }

    @Test
    void validateUpdateTraineeRequest_onlyUsernameProvided_passes() {
        var request = new UpdateTrainee("John_Smith", null, null, null, null);
        assertDoesNotThrow(() -> validator.validateUpdateTraineeRequest(request));
    }

    static Stream<Arguments> invalidUpdateTrainee() {
        return Stream.of(
                arguments("first name with digit", new UpdateTrainee("John_Smith", "John1", null, null, null)),
                arguments("blank last name", new UpdateTrainee("John_Smith", null, " ", null, null)),
                arguments("future birth date", new UpdateTrainee("John_Smith", null, null, TOMORROW, null)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdateTrainee")
    void validateUpdateTraineeRequest_invalid_throwsIllegalArgument(String rule, UpdateTrainee request) {
        assertThrows(IllegalArgumentException.class, () -> validator.validateUpdateTraineeRequest(request));
    }

    @Test
    void validateCreateTrainerRequest_valid_passes() {
        var request = new CreateTrainer("Anna", "Lee", TrainingType.CROSSFIT);
        assertDoesNotThrow(() -> validator.validateCreateTrainerRequest(request));
    }

    static Stream<Arguments> invalidCreateTrainer() {
        return Stream.of(
                arguments("first name with digit", new CreateTrainer("Anna1", "Lee", TrainingType.CROSSFIT)),
                arguments("missing last name", new CreateTrainer("Anna", null, TrainingType.CROSSFIT)),
                arguments("missing specialization", new CreateTrainer("Anna", "Lee", null)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateTrainer")
    void validateCreateTrainerRequest_invalid_throwsIllegalArgument(String rule, CreateTrainer request) {
        assertThrows(IllegalArgumentException.class, () -> validator.validateCreateTrainerRequest(request));
    }

    @Test
    void validateUpdateTrainerRequest_onlyUsernameProvided_passes() {
        var request = new UpdateTrainer("Anna_Lee", null, null, null);
        assertDoesNotThrow(() -> validator.validateUpdateTrainerRequest(request));
    }

    @Test
    void validateUpdateTrainerRequest_invalidName_throwsIllegalArgument() {
        var request = new UpdateTrainer("Anna_Lee", "Anna1", null, null);
        assertThrows(IllegalArgumentException.class, () -> validator.validateUpdateTrainerRequest(request));
    }

    @Test
    void validatePassword_nonBlank_passes() {
        assertDoesNotThrow(() -> validator.validatePassword("secret"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void validatePassword_blank_throwsIllegalArgument(String password) {
        assertThrows(IllegalArgumentException.class, () -> validator.validatePassword(password));
    }

    @Test
    void validateAddTrainingRequest_valid_passes() {
        var request = training("Morning", NEXT_WEEK, ONE_HOUR);
        assertDoesNotThrow(() -> validator.validateAddTrainingRequest(request));
    }

    @Test
    void validateAddTrainingRequest_exactlyMinDuration_passes() {
        var duration = Duration.ofMinutes(trainingDurationMinutesMin);
        var request = training("Morning", NEXT_WEEK, duration);
        assertDoesNotThrow(() -> validator.validateAddTrainingRequest(request));
    }

    static Stream<Arguments> invalidAddTraining() {
        var yesterday = LocalDateTime.now().minusDays(1);
        var inOneMinute = LocalDateTime.now().plusMinutes(1);
        return Stream.of(
                arguments("blank name", training(" ", NEXT_WEEK, ONE_HOUR)),
                arguments("date in the past", training("Morning", yesterday, ONE_HOUR)),
                arguments("within schedule gap", training("Morning", inOneMinute, ONE_HOUR)),
                arguments("shorter than min duration", training("Morning", NEXT_WEEK, Duration.ofMinutes(1))),
                arguments("null duration", training("Morning", NEXT_WEEK, null)),
                arguments("negative duration", training("Morning", NEXT_WEEK, ONE_HOUR.negated())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidAddTraining")
    void validateAddTrainingRequest_invalid_throwsIllegalArgument(String rule, AddTraining request) {
        assertThrows(IllegalArgumentException.class, () -> validator.validateAddTrainingRequest(request));
    }

    private static AddTraining training(String name, LocalDateTime date, Duration duration) {
        return new AddTraining("John_Smith", "Anna_Lee", name, date, duration);
    }
}
