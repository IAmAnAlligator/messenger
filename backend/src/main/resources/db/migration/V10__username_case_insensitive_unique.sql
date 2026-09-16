-- V10__username_case_insensitive_unique.sql

ALTER TABLE users
    DROP CONSTRAINT users_username_key;

CREATE UNIQUE INDEX uk_users_username_lower
    ON users (LOWER(username));