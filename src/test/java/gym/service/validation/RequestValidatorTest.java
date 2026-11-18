package gym.service.validation;

import gym.dto.enums.TrainingType;
import gym.dto.request.CreateTrainee;
import gym.dto.request.CreateTrainer;
import gym.dto.request.ScheduleTraining;
import gym.dto.request.UpdateTrainee;
import gym.dto.request.UpdateTrainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringJUnitConfig(RequestValidatorTest.Config.class)
class RequestValidatorTest {

    @Configuration
    @Import(RequestValidator.class)
    @PropertySource("classpath:appConfig.properties")
    static class Config {}

    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 5, 17);
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalDateTime NEXT_WEEK = LocalDate.now().plusWeeks(1).atTime(10, 0);
    private static final Duration ONE_HOUR = Duration.ofHours(1);

    @Autowired
    private RequestValidator validator;

    @Value("${minTraineeAge}")
    private int minTraineeAge;

    @Value("${scheduleGapMinutesMin}")
    private int scheduleGapMinutesMin;

    @Value("${trainingDurationMinutesMin}")
    private int trainingDurationMinutesMin;


    // ---------- CreateTrainee ----------

    @Test
    void validateCreateTraineeRequest_valid_passes() {
        assertDoesNotThrow(() -> validator.validateCreateTraineeRequest(new CreateTrainee("José", "Smith", BIRTH_DATE)));
    }

    @Test
    void validateCreateTraineeRequest_exactlyMinAge_passes() {
        var birthDate = LocalDate.now().minusYears(minTraineeAge);
        assertDoesNotThrow(() -> validator.validateCreateTraineeRequest(new CreateTrainee("John", "Smith", birthDate)));
    }

    @Test
    void validateCreateTraineeRequest_youngerThanMinAge_throwsIllegalArgument() {
        var birthDate = LocalDate.now().minusYears(minTraineeAge).plusDays(1);
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateCreateTraineeRequest(new CreateTrainee("John", "Smith", birthDate)));
    }

    static Stream<Arguments> invalidCreateTrainee() {
        return Stream.of(
                arguments("first name with digit", new CreateTrainee("John1", "Smith", BIRTH_DATE)),
                arguments("last name with underscore", new CreateTrainee("John", "Smith_2", BIRTH_DATE)),
                arguments("future birth date", new CreateTrainee("John", "Smith", TOMORROW)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateTrainee")
    void validateCreateTraineeRequest_invalid_throwsIllegalArgument(String rule, CreateTrainee request) {
        assertThrows(IllegalArgumentException.class, () -> validator.validateCreateTraineeRequest(request));
    }


    // ---------- UpdateTrainee ----------

    @Test
    void validateUpdateTraineeRequest_onlyIdProvided_passes() {
        assertDoesNotThrow(() -> validator.validateUpdateTraineeRequest(new UpdateTrainee(1L, null, null, null)));
    }

    @Test
    void validateUpdateTraineeRequest_youngerThanMinAge_throwsIllegalArgument() {
        var birthDate = LocalDate.now().minusYears(minTraineeAge).plusDays(1);
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateUpdateTraineeRequest(new UpdateTrainee(1L, null, null, birthDate)));
    }

    static Stream<Arguments> invalidUpdateTrainee() {
        return Stream.of(
                arguments("first name with digit", new UpdateTrainee(1L, "John1", null, null)),
                arguments("blank last name", new UpdateTrainee(1L, null, " ", null)),
                arguments("future birth date", new UpdateTrainee(1L, null, null, TOMORROW)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdateTrainee")
    void validateUpdateTraineeRequest_invalid_throwsIllegalArgument(String rule, UpdateTrainee request) {
        assertThrows(IllegalArgumentException.class, () -> validator.validateUpdateTraineeRequest(request));
    }


    // ---------- CreateTrainer ----------

    @Test
    void validateCreateTrainerRequest_valid_passes() {
        assertDoesNotThrow(() -> validator.validateCreateTrainerRequest(new CreateTrainer("Anna", "Lee", TrainingType.CROSSFIT)));
    }

    static Stream<Arguments> invalidCreateTrainer() {
        return Stream.of(
                arguments("first name with digit", new CreateTrainer("Anna1", "Lee", TrainingType.CROSSFIT)),
                arguments("last name with space", new CreateTrainer("Anna", "Le e", TrainingType.CROSSFIT)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateTrainer")
    void validateCreateTrainerRequest_invalid_throwsIllegalArgument(String rule, CreateTrainer request) {
        assertThrows(IllegalArgumentException.class, () -> validator.validateCreateTrainerRequest(request));
    }


    // ---------- UpdateTrainer ----------

    @Test
    void validateUpdateTrainerRequest_onlyIdProvided_passes() {
        assertDoesNotThrow(() -> validator.validateUpdateTrainerRequest(new UpdateTrainer(1L, null, null, null)));
    }

    static Stream<Arguments> invalidUpdateTrainer() {
        return Stream.of(
                arguments("first name with digit", new UpdateTrainer(1L, "Anna1", null, null)),
                arguments("blank last name", new UpdateTrainer(1L, null, " ", null)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdateTrainer")
    void validateUpdateTrainerRequest_invalid_throwsIllegalArgument(String rule, UpdateTrainer request) {
        assertThrows(IllegalArgumentException.class, () -> validator.validateUpdateTrainerRequest(request));
    }


    // ---------- ScheduleTraining ----------

    @Test
    void validateScheduleTrainingRequest_valid_passes() {
        assertDoesNotThrow(() -> validator.validateScheduleTrainingRequest(
                new ScheduleTraining(1L, 2L, "Morning", NEXT_WEEK, ONE_HOUR)));
    }

    @Test
    void validateScheduleTrainingRequest_withinScheduleGap_throwsIllegalArgument() {
        var date = LocalDateTime.now().plusMinutes(scheduleGapMinutesMin - 1);
        assertThrows(IllegalArgumentException.class, () -> validator.validateScheduleTrainingRequest(
                new ScheduleTraining(1L, 2L, "Morning", date, ONE_HOUR)));
    }

    @Test
    void validateScheduleTrainingRequest_exactlyMinDuration_passes() {
        var duration = Duration.ofMinutes(trainingDurationMinutesMin);
        assertDoesNotThrow(() -> validator.validateScheduleTrainingRequest(
                new ScheduleTraining(1L, 2L, "Morning", NEXT_WEEK, duration)));
    }

    @Test
    void validateScheduleTrainingRequest_shorterThanMinDuration_throwsIllegalArgument() {
        var duration = Duration.ofMinutes(trainingDurationMinutesMin - 1);
        assertThrows(IllegalArgumentException.class, () -> validator.validateScheduleTrainingRequest(
                new ScheduleTraining(1L, 2L, "Morning", NEXT_WEEK, duration)));
    }

    static Stream<Arguments> invalidScheduleTraining() {
        return Stream.of(
                arguments("date in the past", new ScheduleTraining(1L, 2L, "Morning", LocalDateTime.now().minusDays(1), ONE_HOUR)),
                arguments("null duration", new ScheduleTraining(1L, 2L, "Morning", NEXT_WEEK, null)),
                arguments("negative duration", new ScheduleTraining(1L, 2L, "Morning", NEXT_WEEK, ONE_HOUR.negated())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidScheduleTraining")
    void validateScheduleTrainingRequest_invalid_throwsIllegalArgument(String rule, ScheduleTraining request) {
        assertThrows(IllegalArgumentException.class, () -> validator.validateScheduleTrainingRequest(request));
    }
}
