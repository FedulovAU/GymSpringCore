INSERT INTO users (first_name, last_name, base_username, suffix, username, password, is_active) VALUES
    ('John', 'Smith',  'John_Smith',  0, 'John_Smith',   'u1egUEoOeN', TRUE),
    ('John', 'Smith',  'John_Smith',  1, 'John_Smith_1', '6ZvzFttA4A', TRUE),
    ('Mia',  'Wong',   'Mia_Wong',    0, 'Mia_Wong',     'IJqEpIXZHt', TRUE),
    ('Kate', 'Green',  'Kate_Green',  0, 'Kate_Green',   '0AoHAs1VFO', TRUE),
    ('Anna', 'Lee',    'Anna_Lee',    0, 'Anna_Lee',     'afJVUpZxuZ', TRUE),
    ('Mark', 'Brown',  'Mark_Brown',  0, 'Mark_Brown',   'HHKpfAZCmb', TRUE),
    ('Sara', 'Connor', 'Sara_Connor', 0, 'Sara_Connor',  'Jb0k5nOSLQ', TRUE);

INSERT INTO trainee (date_of_birth, address, user_id)
SELECT v.date_of_birth, v.address, u.id
FROM (VALUES
    ('John_Smith',   DATE '1990-05-17', '12 Oak St'),
    ('John_Smith_1', DATE '1988-11-03', NULL),
    ('Mia_Wong',     DATE '1995-03-02', '7 Pine Ave'),
    ('Kate_Green',   DATE '2001-07-21', NULL)
) AS v (username, date_of_birth, address)
JOIN users u ON u.username = v.username;

INSERT INTO trainer (specialization_id, user_id)
SELECT tt.id, u.id
FROM (VALUES
    ('Anna_Lee',    'CROSSFIT'),
    ('Mark_Brown',  'BODY_BUILDING'),
    ('Sara_Connor', 'FITNESS')
) AS v (username, specialization)
JOIN users u          ON u.username = v.username
JOIN training_type tt ON tt.training_type_name = v.specialization;

INSERT INTO training (trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration)
SELECT te.id, tr.id, v.training_name, tt.id, v.training_date, v.duration_minutes
FROM (VALUES
    ('John_Smith',   'Anna_Lee',    'Morning WOD', 'CROSSFIT',      TIMESTAMP '2026-12-01 09:00', 60),
    ('Mia_Wong',     'Anna_Lee',    'Midday WOD',  'CROSSFIT',      TIMESTAMP '2026-12-01 12:00', 60),
    ('John_Smith_1', 'Mark_Brown',  'Leg day',     'BODY_BUILDING', TIMESTAMP '2026-12-01 09:00', 90),
    ('John_Smith',   'Mark_Brown',  'Upper body',  'BODY_BUILDING', TIMESTAMP '2026-12-02 09:00', 90),
    ('Kate_Green',   'Sara_Connor', 'Cardio',      'FITNESS',       TIMESTAMP '2026-12-01 10:00', 60),
    ('Mia_Wong',     'Sara_Connor', 'Stretching',  'FITNESS',       TIMESTAMP '2026-12-03 09:00', 60)
) AS v (trainee_username, trainer_username, training_name, training_type, training_date, duration_minutes)
JOIN users ute        ON ute.username = v.trainee_username
JOIN trainee te       ON te.user_id = ute.id
JOIN users utr        ON utr.username = v.trainer_username
JOIN trainer tr       ON tr.user_id = utr.id
JOIN training_type tt ON tt.training_type_name = v.training_type;