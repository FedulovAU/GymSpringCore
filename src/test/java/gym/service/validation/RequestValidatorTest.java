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
    private static final String PASSWORD = "secret12345";
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalDateTime NEXT_WEEK = LocalDate.now().plusWeeks(1).atTime(10, 0);
    private static final int ONE_HOUR = 60;

    private final RequestValidator validator;
    private final int minTraineeAge;
    private final int scheduleGapMinutesMin;
    private final int trainingDurationMinutesMin;
    private final int passwordMinLength;

    RequestValidatorTest(
            RequestValidator validator,
            @Value("${minTraineeAge}") int minTraineeAge,
            @Value("${scheduleGapMinutesMin}") int scheduleGapMinutesMin,
            @Value("${trainingDurationMinutesMin}") int trainingDurationMinutesMin,
            @Value("${passwordMinLength}") int passwordMinLength) {
        this.validator = validator;
        this.minTraineeAge = minTraineeAge;
        this.scheduleGapMinutesMin = scheduleGapMinutesMin;
        this.trainingDurationMinutesMin = trainingDurationMinutesMin;
        this.passwordMinLength = passwordMinLength;
    }

    @Test
    void validateCreateTraineeRequest_valid_passes() {
        var request = new CreateTrainee("José", "Smith", PASSWORD, BIRTH_DATE, "1 Main St");
        assertDoesNotThrow(() -> validator.validateCreateTraineeRequest(request));
    }

    @Test
    void validateCreateTraineeRequest_optionalFieldsMissing_passes() {
        var request = new CreateTrainee("John", "Smith", PASSWORD, null, null);
        assertDoesNotThrow(() -> validator.validateCreateTraineeRequest(request));
    }

    @Test
    void validateCreateTraineeRequest_exactlyMinAge_passes() {
        var birthDate = LocalDate.now().minusYears(minTraineeAge);
        var request = new CreateTrainee("John", "Smith", PASSWORD, birthDate, null);
        assertDoesNotThrow(() -> validator.validateCreateTraineeRequest(request));
    }

    static Stream<Arguments> invalidCreateTrainee() {
        var tooYoungBirthDate = LocalDate.now().minusYears(5).plusDays(1);
        return Stream.of(
                arguments("missing first name", new CreateTrainee(null, "Smith", PASSWORD, BIRTH_DATE, null)),
                arguments("first name with digit", new CreateTrainee("John1", "Smith", PASSWORD, BIRTH_DATE, null)),
                arguments("last name with underscore", new CreateTrainee("John", "Smith_2", PASSWORD, BIRTH_DATE, null)),
                arguments("younger than min age", new CreateTrainee("John", "Smith", PASSWORD, tooYoungBirthDate, null)),
                arguments("future birth date", new CreateTrainee("John", "Smith", PASSWORD, TOMORROW, null)),
                arguments("missing password", new CreateTrainee("John", "Smith", null, BIRTH_DATE, null)),
                arguments("blank password", new CreateTrainee("John", "Smith", " ", BIRTH_DATE, null)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateTrainee")
    void validateCreateTraineeRequest_invalid_throwsIllegalArgument(String rule, CreateTrainee request) {
        assertThrows(IllegalArgumentException.class, () -> validator.validateCreateTraineeRequest(request));
    }

    @Test
    void validateUpdateTraineeRequest_onlyUsernameProvided_passes() {
        var request = new UpdateTrainee(null, null, null, null);
        assertDoesNotThrow(() -> validator.validateUpdateTraineeRequest(request));
    }

    static Stream<Arguments> invalidUpdateTrainee() {
        return Stream.of(
                arguments("first name with digit", new UpdateTrainee("John1", null, null, null)),
                arguments("blank last name", new UpdateTrainee(null, " ", null, null)),
                arguments("future birth date", new UpdateTrainee(null, null, TOMORROW, null)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdateTrainee")
    void validateUpdateTraineeRequest_invalid_throwsIllegalArgument(String rule, UpdateTrainee request) {
        assertThrows(IllegalArgumentException.class, () -> validator.validateUpdateTraineeRequest(request));
    }

    @Test
    void validateCreateTrainerRequest_valid_passes() {
        var request = new CreateTrainer("Anna", "Lee", PASSWORD, TrainingType.CROSSFIT);
        assertDoesNotThrow(() -> validator.validateCreateTrainerRequest(request));
    }

    static Stream<Arguments> invalidCreateTrainer() {
        return Stream.of(
                arguments("first name with digit", new CreateTrainer("Anna1", "Lee", PASSWORD, TrainingType.CROSSFIT)),
                arguments("missing last name", new CreateTrainer("Anna", null, PASSWORD, TrainingType.CROSSFIT)),
                arguments("missing specialization", new CreateTrainer("Anna", "Lee", PASSWORD, null)),
                arguments("blank password", new CreateTrainer("Anna", "Lee", " ", TrainingType.CROSSFIT)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateTrainer")
    void validateCreateTrainerRequest_invalid_throwsIllegalArgument(String rule, CreateTrainer request) {
        assertThrows(IllegalArgumentException.class, () -> validator.validateCreateTrainerRequest(request));
    }

    @Test
    void validateUpdateTrainerRequest_onlyUsernameProvided_passes() {
        var request = new UpdateTrainer(null, null, null);
        assertDoesNotThrow(() -> validator.validateUpdateTrainerRequest(request));
    }

    @Test
    void validateUpdateTrainerRequest_invalidName_throwsIllegalArgument() {
        var request = new UpdateTrainer("Anna1", null, null);
        assertThrows(IllegalArgumentException.class, () -> validator.validateUpdateTrainerRequest(request));
    }

    @Test
    void validatePassword_exactlyMinLength_passes() {
        var password = "a".repeat(passwordMinLength);
        assertDoesNotThrow(() -> validator.validatePassword(password));
    }

    @Test
    void validatePassword_shorterThanMinLength_throwsIllegalArgument() {
        var password = "a".repeat(passwordMinLength - 1);
        assertThrows(IllegalArgumentException.class, () -> validator.validatePassword(password));
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
        var request = training("Morning", NEXT_WEEK, trainingDurationMinutesMin);
        assertDoesNotThrow(() -> validator.validateAddTrainingRequest(request));
    }

    static Stream<Arguments> invalidAddTraining() {
        var yesterday = LocalDateTime.now().minusDays(1);
        var inOneMinute = LocalDateTime.now().plusMinutes(1);
        return Stream.of(
                arguments("blank name", training(" ", NEXT_WEEK, ONE_HOUR)),
                arguments("date in the past", training("Morning", yesterday, ONE_HOUR)),
                arguments("within schedule gap", training("Morning", inOneMinute, ONE_HOUR)),
                arguments("shorter than min duration", training("Morning", NEXT_WEEK, 1)),
                arguments("zero duration", training("Morning", NEXT_WEEK, 0)),
                arguments("negative duration", training("Morning", NEXT_WEEK, -ONE_HOUR)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidAddTraining")
    void validateAddTrainingRequest_invalid_throwsIllegalArgument(String rule, AddTraining request) {
        assertThrows(IllegalArgumentException.class, () -> validator.validateAddTrainingRequest(request));
    }

    private static AddTraining training(String name, LocalDateTime date, int durationMinutes) {
        return new AddTraining("John_Smith", "Anna_Lee", name, date, durationMinutes);
    }
}
