DROP TABLE IF EXISTS users, categories, compilations, events, compilation_events, requests;

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  VARCHAR NOT NULL,
    email VARCHAR NOT NULL UNIQUE,
    CONSTRAINT inv_user_email CHECK (email <> '' AND POSITION('@' IN email) > 0),
    CONSTRAINT inv_user_name CHECK (name <> '' AND POSITION(' ' IN name) = 0)
);

CREATE TABLE IF NOT EXISTS categories
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR NOT NULL UNIQUE,
    CONSTRAINT inv_category_name CHECK (name <> '')
);

CREATE TABLE IF NOT EXISTS compilations
(
    id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title  VARCHAR NOT NULL,
    pinned BOOLEAN DEFAULT FALSE,
    CONSTRAINT inv_compilation_name CHECK (title <> '')
);

CREATE TABLE IF NOT EXISTS events
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id        BIGINT REFERENCES categories ON DELETE CASCADE,
    user_id            BIGINT REFERENCES users ON DELETE CASCADE,
    title              VARCHAR(120)  NOT NULL,
    annotation         VARCHAR(2000) NOT NULL,
    description        VARCHAR(7000) NOT NULL,
    event_date         TIMESTAMP,
    created            TIMESTAMP,
    published          TIMESTAMP,
    state              VARCHAR       NOT NULL DEFAULT 'PENDING',
    paid               BOOLEAN                DEFAULT FALSE,
    participant_limit  INT                    default 0,
    request_moderation BOOLEAN                DEFAULT TRUE,
    confirmed_requests INT,
    latitude           double precision,
    longitude          double precision,

    CONSTRAINT inv_event_title CHECK (title <> ''),
    CONSTRAINT inv_event_descr CHECK (description <> ''),
    CONSTRAINT inv_event_annotation CHECK (annotation <> '')
);

CREATE TABLE IF NOT EXISTS compilation_events
(
    compilation_id BIGINT REFERENCES compilations ON DELETE CASCADE,
    event_id       BIGINT REFERENCES events ON DELETE CASCADE,
    PRIMARY KEY (compilation_id, event_id)
);

CREATE TABLE IF NOT EXISTS requests
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    requester_id BIGINT REFERENCES users ON DELETE CASCADE,
    event_id     BIGINT REFERENCES events ON DELETE CASCADE,
    status       VARCHAR NOT NULL DEFAULT 'PENDING',
    created      TIMESTAMP
);