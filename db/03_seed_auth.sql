-- Sample accounts for the AUTH db. Passwords are stored only as bcrypt hashes.
-- Plain passwords (for testing/demo only):
--   admin1 / Admin@123      inst1 / Inst@123      stu1 / Stu@123      stu2 / Stu@123
--
-- Load with:  mysql --user=erp_app --password=ErpApp@2026! < db/03_seed_auth.sql

USE erp_auth;

DELETE FROM users_auth;
ALTER TABLE users_auth AUTO_INCREMENT = 1;

INSERT INTO users_auth (user_id, username, role, password_hash, status) VALUES
 (1, 'admin1', 'ADMIN',      '$2a$10$mJX0uYndHqiF1FfEU3JAX.Y.IYPGTk0aXdv81h/PDmuSOBzBsdg/O', 'ACTIVE'),
 (2, 'inst1',  'INSTRUCTOR', '$2a$10$noFVANnZz9/v9WYp0ghFueUxtIMVvMHyzq9jhKNhMxgKb3If2QOe2', 'ACTIVE'),
 (3, 'stu1',   'STUDENT',    '$2a$10$mfj8YmkCAEnQX0ZdIi.zOuphklIwMqelGlbb3I3UaBxt.xRDq3XtW', 'ACTIVE'),
 (4, 'stu2',   'STUDENT',    '$2a$10$HEC7nngj7ohpRGBEB1azMO5xi8poW0TwHDpPyfrBAen4rQCyi1e..', 'ACTIVE');
