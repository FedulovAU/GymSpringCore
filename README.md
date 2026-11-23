# Gym Spring Core

A gym management application on Spring Core (no Spring Boot): trainees, trainers and trainings,
kept in an in-memory storage that is filled from JSON files on startup.

## Requirements

- JDK 27
- Maven 3.9+

Run all commands from the project root folder.

## Build

Compile, run the tests and package the jar:

```bash
mvn package
```

The jar is written to `target/GymSpringCore-1.0-SNAPSHOT.jar`.

Build without running the tests:

```bash
mvn package -DskipTests
```

> The jar has no main class in its manifest and doesn't include dependencies,
> so it isn't started with `java -jar`. Use `exec:java` as described below.

## Run

```bash
mvn compile exec:java
```

`compile` makes sure the code is up to date; `exec:java` then starts `gym.Program`
(configured in `pom.xml`) with the project's classpath, inside Maven's JVM.

`gym.Program` runs a sample use case through the services: it creates a trainee, a trainer
and a training between them, updates a trainer and a trainee, deletes a trainee, and logs
all trainers, trainees and trainings.

### Configuration and data

#### Properties

`src/main/resources/appConfig.properties`. All properties are required: the app fails
to start if one is missing.

| Property | Default value | Meaning |
|---|---|---|
| `storage.snapshot.location` | `snapshot` | Classpath folder with the startup data. |
| `trainingDurationMinutesMin` | `15` | Shortest allowed training, in minutes. |
| `scheduleGapMinutesMin` | `15` | How many minutes from now a training can start at the earliest. |
| `minTraineeAge` | `5` | Youngest allowed trainee age, in years. |

`RequestValidatorTest` reads the same file, so its limit tests follow these values.

#### Startup data

`src/main/resources/snapshot/` holds one JSON file per entity type, named after the
entity class:

| File | Contents |
|---|---|
| `gym.data.entity.TraineeEntity.json` | Trainees: `id`, `birthDate` and a nested `user`. |
| `gym.data.entity.TrainerEntity.json` | Trainers: `id`, `specialization` and a nested `user`. |
| `gym.data.entity.TrainingEntity.json` | Trainings, referencing a trainee and a trainer by ID. |

- The nested `user` holds the name, username, username suffix, password and active flag.
- Trainees and trainers have separate IDs, so a trainee and a trainer can have the same ID.
  New ones get the next ID after the highest loaded one of their type.
- New usernames continue from the highest loaded suffix for the same first and last name,
  across both trainees and trainers.
- A missing file means no items of that type.

#### Logging

Logs go to the console and to `logs/gym.log`, relative to the folder the app is started from.
Logging is configured in `src/main/resources/logback.xml`.

## Debug

### In IntelliJ IDEA

Open `gym.Program` and click the gutter icon next to `main`, then **Debug 'Program.main()'**.
For tests, use the same gutter icon next to a test class or test method.

### From the command line, with any debugger

The JVM waits until a debugger attaches to the given port. Attach with IntelliJ IDEA
(**Run → Edit Configurations → + → Remote JVM Debug**, host `localhost`, the port below),
VS Code, Eclipse or `jdb`.

Application, waits on port **8000**:

```bash
mvnDebug compile exec:java
```

Tests, wait on port **5005**:

```bash
mvn test -Dmaven.surefire.debug
```

## Tests

Run all tests:

```bash
mvn test
```

Run one test class:

```bash
mvn test -Dtest=TraineeServiceTest
```

Run one test method:

```bash
mvn test -Dtest=TraineeServiceTest#createTrainee_validRequest_returnsCreatedTrainee
```

Test reports are written to `target/surefire-reports/`.
