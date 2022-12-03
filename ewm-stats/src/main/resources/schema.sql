DROP TABLE IF EXISTS hits;

CREATE TABLE IF NOT EXISTS hits
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app           VARCHAR NOT NULL,
    uri           VARCHAR NOT NULL,
    ip            VARCHAR NOT NULL,
    hit_timestamp TIMESTAMP
);