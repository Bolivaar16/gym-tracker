CREATE TABLE exercise (
    id                   BIGSERIAL PRIMARY KEY,
    name                 TEXT NOT NULL UNIQUE,
    muscle_groups        TEXT[] NOT NULL,
    default_rest_seconds INT NOT NULL DEFAULT 120,
    notes                TEXT,
    archived             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE workout (
    id          BIGSERIAL PRIMARY KEY,
    started_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at TIMESTAMPTZ,
    notes       TEXT,
    template_id BIGINT
);

CREATE TABLE template (
    id         BIGSERIAL PRIMARY KEY,
    name       TEXT NOT NULL UNIQUE,
    notes      TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE workout
    ADD CONSTRAINT fk_workout_template
    FOREIGN KEY (template_id) REFERENCES template (id) ON DELETE SET NULL;

CREATE TABLE workout_exercise (
    id          BIGSERIAL PRIMARY KEY,
    workout_id  BIGINT NOT NULL REFERENCES workout (id) ON DELETE CASCADE,
    exercise_id BIGINT NOT NULL REFERENCES exercise (id) ON DELETE RESTRICT,
    position    INT NOT NULL,
    UNIQUE (workout_id, exercise_id)
);

CREATE TABLE workout_set (
    id                  BIGSERIAL PRIMARY KEY,
    workout_exercise_id BIGINT NOT NULL REFERENCES workout_exercise (id) ON DELETE CASCADE,
    set_number          INT NOT NULL,
    reps                INT NOT NULL CHECK (reps > 0),
    weight_kg           NUMERIC(6, 2) NOT NULL CHECK (weight_kg >= 0),
    rpe                 NUMERIC(3, 1) CHECK (rpe BETWEEN 1 AND 10),
    completed_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (workout_exercise_id, set_number)
);

CREATE TABLE template_exercise (
    id               BIGSERIAL PRIMARY KEY,
    template_id      BIGINT NOT NULL REFERENCES template (id) ON DELETE CASCADE,
    exercise_id      BIGINT NOT NULL REFERENCES exercise (id) ON DELETE RESTRICT,
    position         INT NOT NULL,
    target_sets      INT NOT NULL DEFAULT 3,
    target_reps      INT NOT NULL DEFAULT 10,
    target_weight_kg NUMERIC(6, 2),
    UNIQUE (template_id, exercise_id)
);

CREATE INDEX idx_workout_started_at ON workout (started_at DESC);
CREATE INDEX idx_we_workout         ON workout_exercise (workout_id);
CREATE INDEX idx_we_exercise        ON workout_exercise (exercise_id);
CREATE INDEX idx_set_we             ON workout_set (workout_exercise_id);
