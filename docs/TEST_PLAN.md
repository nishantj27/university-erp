# Test Plan

This is the list of things the app is supposed to do and how each one is checked. Some are checked
automatically (JUnit unit tests, or the backend smoke run), and some are checked by hand in the UI.

**Accounts used:** `admin1 / Admin@123`, `inst1 / Inst@123`, `stu1 / Stu@123`, `stu2 / Stu@123`.
**Seed data:** 3 courses, 3 sections (CS101 has capacity 2 so "Section full" is easy to reach),
CS101 already has both students enrolled, stu1 also has CS201.

## Login & roles

| # | Test | How | Expected |
|---|------|-----|----------|
| 1 | Wrong password rejected | UI / auto | Shows "Incorrect username or password." |
| 2 | Correct login opens the matching dashboard | UI | Student/instructor/admin dashboard by role |
| 3 | Blank fields handled | UI | Asks for both username and password |

## Student

| # | Test | How | Expected |
|---|------|-----|----------|
| 4 | Catalog shows code/title/credits/instructor/seats | UI | Table lists all sections |
| 5 | Register in a section with seats | UI / auto | Success; appears in My Registrations |
| 6 | Register in the same section again | auto | Blocked: "You are already registered in this section." |
| 7 | Register in a full section | auto | Blocked: "Section full." |
| 8 | Drop before deadline | UI / auto | Success; row disappears |
| 9 | Register/drop after the deadline | auto | Blocked with a deadline message |
| 10 | View grades + per-assessment breakdown | UI | Shows components and final |
| 11 | Download transcript (CSV or PDF) | UI | File saved and opens |

## Instructor

| # | Test | How | Expected |
|---|------|-----|----------|
| 12 | Sees only their own sections | auto | Only sections assigned to them |
| 13 | Define an assessment | UI | Added to the section's scheme |
| 14 | Enter a score | UI | Saved; re-entering updates, no duplicate |
| 15 | Score above the max rejected | auto | "Score must be between 0 and 100.0." |
| 16 | Compute finals using the weighting rule | auto | 18/25/40 on 20/30/50 -> 31.1 |
| 17 | Grade a section they don't teach | auto | Blocked: "Not your section." |
| 18 | Class stats | UI | Average / highest / lowest |
| 19 | Export grade CSV | UI | File saved |

## Admin

| # | Test | How | Expected |
|---|------|-----|----------|
| 20 | Create a student (login + profile) | UI / auto | New row in auth DB and ERP DB |
| 21 | Create a course and a section, assign instructor | UI | Appear in the lists |
| 22 | Delete a section with students enrolled | auto | Blocked with an explanation |
| 23 | Toggle maintenance ON | UI / auto | Banner shows for student/instructor |
| 24 | Toggle maintenance OFF | UI | Normal behaviour returns |

## Access rules & maintenance

| # | Test | How | Expected |
|---|------|-----|----------|
| 25 | Student can't do instructor/admin actions | auto | "You are not allowed to do this." |
| 26 | With maintenance ON, student/instructor writes blocked everywhere | auto | Blocked with maintenance message |
| 27 | Admin can still change data during maintenance | auto | Allowed |

## Security / two-database separation

| # | Test | How | Expected |
|---|------|-----|----------|
| 28 | Passwords stored only as bcrypt hashes | Inspect `erp_auth.users_auth` | `password_hash` starts with `$2a$`, no plaintext |
| 29 | ERP DB has no passwords | Inspect `erp_main` | No password column anywhere |
| 30 | Login uses auth DB, profile loaded from ERP DB via shared user_id | auto | Role from auth, name from ERP |
| 31 | Account locks after too many wrong tries | auto | `status` becomes LOCKED after 5 |

## Data integrity

| # | Test | How | Expected |
|---|------|-----|----------|
| 32 | Duplicate enrollment prevented | DB unique key + service | No second row for same (student, section) |
| 33 | Negative/zero capacity rejected | auto | "Capacity must be greater than zero." |

**Automated coverage:** items marked "auto" are exercised either by the JUnit suite
(`mvn test`, 19 tests) or the end-to-end backend smoke check. The rest are verified by clicking
through the UI (see the demo video).
