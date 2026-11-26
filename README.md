# Gym Spring Core

A gym management application on Spring Core (no Spring Boot) and plain Hibernate: trainees,
trainers and trainings, stored in PostgreSQL.

## Requirements

- JDK 27
- Maven 3.9+
- Docker with Docker Compose

Run all commands from the project root folder.

## Launch

1. Start the database and wait until it is ready:

   ```bash
   docker compose up -d --wait
   ```

   The first start downloads the `postgres:19` image.

2. Run the application:

   ```bash
   mvn compile exec:java
   ```

   `compile` makes sure the code is up to date; `exec:java` then starts `gym.Program`
   (configured in `pom.xml`) with the project's classpath, inside Maven's JVM.

3. Stop the database when you are done:

   ```bash
   docker compose down
   ```

The database container keeps running between application starts, so step 1 is needed only once
per session.

## Database

### Connection

`compose.yaml` starts PostgreSQL in the `gym-db` container. Its database name, user, password
and port come from `.env`:

| Variable | Value |
|---|---|
| `DB_NAME` | `gym` |
| `DB_USER` | `gym_user` |
| `DB_PASSWORD` | `gym_pass` |
| `DB_PORT` | `5432` |

The application reads the same variables from its environment and falls back to these values
when they are not set. If you change `.env`, set the same variables in the environment the
application runs in.

### Schema and data

Application itself drops and recreates the schema on every start, then loads the seed data,
so each run starts from the same state. The scripts are in `src/main/resources/db/`:

| File | Contents |
|---|---|
| `drop.sql` | Drops all tables. |
| `schema.sql` | Creates the tables and the training types `FITNESS`, `BODY_BUILDING`, `CROSSFIT`. |
| `data.sql` | Seed users, trainees, trainers and trainings. |

## Configuration

`src/main/resources/appConfig.properties`. All properties are required: the app fails
to start if one is missing.

| Property | Default value | Meaning |
|---|---|---|
| `trainingDurationMinutesMin` | `15` | Shortest allowed training, in minutes. |
| `scheduleGapMinutesMin` | `15` | How many minutes from now a training can start at the earliest. |
| `minTraineeAge` | `5` | Youngest allowed trainee age, in years. |
| `passwordMinLength` | `10` | Shortest allowed password. |
| `db.driver`, `db.url`, `db.user`, `db.password` | see file | Database connection, built from the variables above. |
| `db.script.drop`, `db.script.schema`, `db.script.data` | `db/*.sql` | Classpath locations of the scripts. |

`RequestValidatorTest` reads the same file, so its limit tests follow these values.

## Logging

Application logs go to the console and to `logs/gym.log`, relative to the folder the app is
started from. Hibernate SQL statements go only to `logs/gym.log`. Logging is configured in
`src/main/resources/logback.xml`.

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
> so it isn't started with `java -jar`. Use `mvn compile exec:java` as described above.

## Tests

The tests are unit tests and don't need the database.

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
mvn test -Dtest=TraineeServiceTest#switchActive_activeUser_deactivates
```

Test reports are written to `target/surefire-reports/`.

## Debug

These commands start a JVM that waits until a debugger attaches to the given port on
`localhost`. Any JVM debugger that supports remote attach works.

Application, waits on port **8000** (start the database first):

```bash
mvnDebug compile exec:java
```

Tests, wait on port **5005**:

```bash
mvn test -Dmaven.surefire.debug
```
