-- Sample data for the ERP db. The user_id values here line up with the accounts seeded in
-- 03_seed_auth.sql (2 = instructor, 3 & 4 = students).
--
-- Load with:  mysql --user=erp_app --password=ErpApp@2026! < db/04_seed_erp.sql

USE erp_main;

DELETE FROM grades;
DELETE FROM assessments;
DELETE FROM enrollments;
DELETE FROM sections;
DELETE FROM courses;
DELETE FROM students;
DELETE FROM instructors;
UPDATE settings SET svalue = 'false' WHERE skey = 'maintenance_on';

-- profiles
INSERT INTO instructors (user_id, full_name, department) VALUES
 (2, 'Dr. Alan Turing', 'Computer Science');

INSERT INTO students (user_id, roll_no, full_name, program, year) VALUES
 (3, '2023CS001', 'Ada Lovelace', 'B.Tech CSE', 2),
 (4, '2023CS002', 'Grace Hopper', 'B.Tech CSE', 2);

-- catalog
INSERT INTO courses (id, code, title, credits) VALUES
 (1, 'CS101', 'Introduction to Programming', 4),
 (2, 'CS201', 'Data Structures',            4),
 (3, 'MA101', 'Calculus I',                 3);

-- sections. inst1 (user_id 2) teaches CS101 and CS201; MA101 has no instructor yet.
-- CS101 capacity is deliberately small (2) so the "Section full" case is easy to demo.
INSERT INTO sections (id, course_id, instructor_id, day_time, room, capacity, semester, year, add_drop_deadline) VALUES
 (1, 1, 2,    'Mon/Wed 10:00-11:00', 'A-101', 2,  'Monsoon', 2026, '2026-08-15'),
 (2, 2, 2,    'Tue/Thu 11:00-12:30', 'A-102', 30, 'Monsoon', 2026, '2026-08-15'),
 (3, 3, NULL, 'Mon/Fri 09:00-10:00', 'B-201', 40, 'Monsoon', 2026, '2026-08-15');

-- assessment scheme (weights add up to 100), defined per section
INSERT INTO assessments (id, section_id, name, weight, max_score) VALUES
 (1, 1, 'Quiz',    20, 100),
 (2, 1, 'Midterm', 30, 100),
 (3, 1, 'End-sem', 50, 100),
 (4, 2, 'Quiz',    20, 100),
 (5, 2, 'Midterm', 30, 100),
 (6, 2, 'End-sem', 50, 100);

-- enrollments: both students take CS101 (which fills it), stu1 also takes CS201
INSERT INTO enrollments (id, student_id, section_id, status, final_grade) VALUES
 (1, 3, 1, 'REGISTERED', NULL),
 (2, 4, 1, 'REGISTERED', NULL),
 (3, 3, 2, 'REGISTERED', NULL);

-- a few scores already entered for stu1 in CS101 (final not computed yet - do that in the demo)
INSERT INTO grades (enrollment_id, assessment_id, score) VALUES
 (1, 1, 18),
 (1, 2, 25),
 (1, 3, 40);
