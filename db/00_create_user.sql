-- One-time database + app-user setup.
-- Run as the MySQL admin:   sudo mysql < db/00_create_user.sql
--
-- Two SEPARATE databases, "UNIX shadow" style:
--   erp_auth -> usernames, roles, password HASHES only  (never real passwords)
--   erp_main -> everything else (students, courses, sections, enrollments, grades, settings)
-- The two are linked only by a shared user_id.

CREATE DATABASE IF NOT EXISTS erp_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS erp_main CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Dedicated application account (local development credentials).
CREATE USER IF NOT EXISTS 'erp_app'@'localhost' IDENTIFIED BY 'ErpApp@2026!';

GRANT ALL PRIVILEGES ON erp_auth.* TO 'erp_app'@'localhost';
GRANT ALL PRIVILEGES ON erp_main.* TO 'erp_app'@'localhost';
FLUSH PRIVILEGES;
