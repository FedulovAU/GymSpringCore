CREATE TABLE users (
    id             BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name     VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    base_username  VARCHAR(201) NOT NULL,
    username_suffix         INTEGER      NOT NULL DEFAULT 0,
    username       VARCHAR(255) NOT NULL,
    password       VARCHAR(255) NOT NULL,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_users_username     UNIQUE (username),
    CONSTRAINT uq_users_base_suffix  UNIQUE (base_username, username_suffix)
);

CREATE TABLE training_type (
    id                  BIGINT      PRIMARY KEY,
    training_type_name  VARCHAR(50) NOT NULL,
    CONSTRAINT uq_training_type_name UNIQUE (training_type_name)
);

INSERT INTO training_type (id, training_type_name) VALUES
    (1, 'BODY_BUILDING'),
    (2, 'CROSSFIT'),
    (3, 'FITNESS');

CREATE TABLE trainee (
    id             BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    date_of_birth  DATE,
    address        VARCHAR(255),
    user_id        BIGINT       NOT NULL,
    CONSTRAINT uq_trainee_user UNIQUE (user_id),
    CONSTRAINT fk_trainee_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE trainer (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    specialization_id  BIGINT NOT NULL,
    user_id            BIGINT NOT NULL,
    CONSTRAINT uq_trainer_user UNIQUE (user_id),
    CONSTRAINT fk_trainer_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_trainer_specialization FOREIGN KEY (specialization_id) REFERENCES training_type (id)
);

CREATE TABLE training (
    id                 BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    trainee_id         BIGINT       NOT NULL,
    trainer_id         BIGINT       NOT NULL,
    training_name      VARCHAR(255) NOT NULL,
    training_type_id   BIGINT       NOT NULL,
    training_date      TIMESTAMP    NOT NULL,
    training_duration  INTEGER      NOT NULL,
    CONSTRAINT fk_training_trainee FOREIGN KEY (trainee_id) REFERENCES trainee (id) ON DELETE CASCADE,
    CONSTRAINT fk_training_trainer FOREIGN KEY (trainer_id) REFERENCES trainer (id) ON DELETE CASCADE,
    CONSTRAINT fk_training_type FOREIGN KEY (training_type_id) REFERENCES training_type (id)
);
