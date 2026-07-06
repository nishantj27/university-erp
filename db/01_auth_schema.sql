-- Auth database schema (the "shadow" db). Holds only login-related data:
-- usernames, roles, and bcrypt password HASHES. No real passwords, and nothing about
-- courses/grades lives here.
--
-- Load with:  mysql --user=erp_app --password=ErpApp@2026! < db/01_auth_schema.sql

USE erp_auth;

DROP TABLE IF EXISTS users_auth;

CREATE TABLE users_auth (
    user_id         INT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    role            VARCHAR(20)  NOT NULL,               -- ADMIN / INSTRUCTOR / STUDENT
    password_hash   VARCHAR(100) NOT NULL,               -- bcrypt hash, ~60 chars
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',   -- ACTIVE / LOCKED
    failed_attempts INT          NOT NULL DEFAULT 0,      -- for the lockout-after-N-tries feature
    last_login      DATETIME     NULL
);
