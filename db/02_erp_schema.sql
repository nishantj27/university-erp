-- ERP database schema. Everything that is not a login: student/instructor profiles, the course
-- catalog, sections, enrollments, assessments, grades and app settings.
--
-- Note there is NO foreign key from students/instructors back to the auth db - the two databases
-- are deliberately separate and only share the user_id value.
--
-- Load with:  mysql --user=erp_app --password=ErpApp@2026! < db/02_erp_schema.sql

USE erp_main;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS grades;
DROP TABLE IF EXISTS assessments;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS sections;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS instructors;
DROP TABLE IF EXISTS settings;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE students (
    user_id   INT PRIMARY KEY,           -- same id as the auth-db account
    roll_no   VARCHAR(20)  NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    program   VARCHAR(50),
    year      INT
);

CREATE TABLE instructors (
    user_id    INT PRIMARY KEY,          -- same id as the auth-db account
    full_name  VARCHAR(100) NOT NULL,
    department VARCHAR(50)
);

CREATE TABLE courses (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    code    VARCHAR(20)  NOT NULL UNIQUE,
    title   VARCHAR(150) NOT NULL,
    credits INT          NOT NULL
);

CREATE TABLE sections (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    course_id         INT NOT NULL,
    instructor_id     INT NULL,                 -- may be unassigned
    day_time          VARCHAR(50),
    room              VARCHAR(30),
    capacity          INT NOT NULL,
    semester          VARCHAR(20),
    year              INT,
    add_drop_deadline DATE,
    CONSTRAINT fk_section_course     FOREIGN KEY (course_id)     REFERENCES courses(id),
    CONSTRAINT fk_section_instructor FOREIGN KEY (instructor_id) REFERENCES instructors(user_id)
);

CREATE TABLE enrollments (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    student_id  INT NOT NULL,
    section_id  INT NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'REGISTERED',
    final_grade DOUBLE NULL,                    -- filled in when the instructor computes finals
    -- one row per (student, section): this is what blocks duplicate registration
    CONSTRAINT uq_enrollment UNIQUE (student_id, section_id),
    CONSTRAINT fk_enr_student FOREIGN KEY (student_id) REFERENCES students(user_id),
    CONSTRAINT fk_enr_section FOREIGN KEY (section_id) REFERENCES sections(id)
);

CREATE TABLE assessments (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    section_id INT NOT NULL,
    name       VARCHAR(50) NOT NULL,            -- Quiz / Midterm / End-sem ...
    weight     DOUBLE NOT NULL,                 -- percentage contribution to the final
    max_score  DOUBLE NOT NULL,
    CONSTRAINT fk_asmt_section FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE
);

CREATE TABLE grades (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL,
    assessment_id INT NOT NULL,
    score         DOUBLE NOT NULL,
    CONSTRAINT uq_grade UNIQUE (enrollment_id, assessment_id),
    CONSTRAINT fk_grade_enr  FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE,
    CONSTRAINT fk_grade_asmt FOREIGN KEY (assessment_id) REFERENCES assessments(id) ON DELETE CASCADE
);

CREATE TABLE settings (
    skey   VARCHAR(50) PRIMARY KEY,
    svalue VARCHAR(255)
);

-- maintenance mode starts OFF
INSERT INTO settings (skey, svalue) VALUES ('maintenance_on', 'false');
