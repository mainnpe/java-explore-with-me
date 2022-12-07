DROP TABLE IF EXISTS users, categories, compilations, events, compilation_events, requests, cities, locations;

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  VARCHAR NOT NULL,
    email VARCHAR NOT NULL UNIQUE,
    CONSTRAINT inv_user_email CHECK (email <> '' AND POSITION('@' IN email) > 0),
    CONSTRAINT inv_user_name CHECK (name <> '')
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
    category_id        BIGINT REFERENCES categories,
    user_id            BIGINT REFERENCES users ON DELETE CASCADE,
    title              VARCHAR(120)  NOT NULL,
    annotation         VARCHAR(2000) NOT NULL,
    description        VARCHAR(7000) NOT NULL,
    event_date         TIMESTAMP,
    created            TIMESTAMP,
    published          TIMESTAMP,
    state              VARCHAR       NOT NULL DEFAULT 'PENDING',
    paid               BOOLEAN                DEFAULT FALSE,
    participant_limit  INT                    DEFAULT 0,
    request_moderation BOOLEAN                DEFAULT TRUE,
    confirmed_requests INT                    DEFAULT 0,
    latitude           double precision,
    longitude          double precision,

    CONSTRAINT inv_event_title CHECK (title <> ''),
    CONSTRAINT inv_event_descr CHECK (description <> ''),
    CONSTRAINT inv_event_annotation CHECK (annotation <> ''),
    CONSTRAINT events_confirmed_req_goe_than_0 CHECK (confirmed_requests >= 0)
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
    created      TIMESTAMP,
    CONSTRAINT uq_requester_event UNIQUE (requester_id, event_id)
);

CREATE TABLE IF NOT EXISTS cities
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR NOT NULL UNIQUE,
    CONSTRAINT inv_city_name CHECK (name <> '')
);

CREATE TABLE IF NOT EXISTS locations
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name      VARCHAR NOT NULL,
    city_id   BIGINT REFERENCES cities,
    latitude  double precision,
    longitude double precision,
    CONSTRAINT inv_location_name CHECK (name <> ''),
    CONSTRAINT uq_location_name UNIQUE (name, latitude, longitude)
);

ALTER TABLE events
    ADD COLUMN location_id BIGINT;
ALTER TABLE events
    ADD CONSTRAINT fk_location FOREIGN KEY (location_id) references locations;
ALTER TABLE events
    DROP COLUMN longitude;
ALTER TABLE events
    DROP COLUMN latitude;

CREATE OR REPLACE FUNCTION distance(lat1 float, lon1 float, lat2 float, lon2 float)
    RETURNS float
AS
'
    declare
        dist      float = 0;
        rad_lat1  float;
        rad_lat2  float;
        theta     float;
        rad_theta float;
    BEGIN
        IF lat1 = lat2 AND lon1 = lon2
        THEN
            RETURN dist;
        ELSE
            -- переводим градусы широты в радианы
            rad_lat1 = pi() * lat1 / 180;
            -- переводим градусы долготы в радианы
            rad_lat2 = pi() * lat2 / 180;
            -- находим разность долгот
            theta = lon1 - lon2;
            -- переводим градусы в радианы
            rad_theta = pi() * theta / 180;
            -- находим длину ортодромии
            dist = sin(rad_lat1) * sin(rad_lat2) + cos(rad_lat1) * cos(rad_lat2) * cos(rad_theta);

            IF dist > 1
            THEN
                dist = 1;
            END IF;

            dist = acos(dist);
            -- переводим радианы в градусы
            dist = dist * 180 / pi();
            -- переводим градусы в километры
            dist = dist * 60 * 1.8524;

            RETURN dist;
        END IF;
    END;
'
    LANGUAGE PLPGSQL;