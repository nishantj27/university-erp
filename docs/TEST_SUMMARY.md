# Test Summary

_Last run: JDK 25, MySQL 8.4, Maven 3.9._

## Automated

| Suite | Result |
|-------|--------|
| JUnit unit tests (`mvn test`) | **19 passed, 0 failed** |
| Backend end-to-end smoke check (login, register rules, cross-role access, maintenance, grading) | **14 passed, 0 failed** |

The unit tests cover: password hashing (5), access & maintenance rules (5), registration rules —
seats/duplicate/deadline/role/drop (6), and final-grade weighting incl. the missing-score case (3).

## Manual (UI)

Clicked through all three dashboards against the seed data:

- Login rejects wrong passwords and routes each role to the right dashboard. ✔
- Student: catalog, register, drop, timetable, grades breakdown, transcript CSV + PDF export. ✔
- Instructor: my-sections only, add assessment, enter score, compute finals, class stats, grade CSV. ✔
- Admin: add student/instructor, create course/section, assign instructor, delete section, maintenance toggle. ✔
- Maintenance banner appears for student/instructor when ON and their changes are blocked; admin unaffected. ✔

## Known limitations

- **Capacity check isn't fully transactional.** The "seats available" count and the insert aren't
  done in one locked transaction, so a burst of simultaneous registrations could in theory overfill
  by one. The duplicate-enrollment rule *is* hard-guaranteed by a DB unique key. Fine for a
  single-user desktop app; a production version would use `SELECT ... FOR UPDATE`.
- **User creation spans two databases** which can't share one transaction, so it uses a
  compensating delete (remove the auth row if the profile insert fails) rather than true 2-phase
  commit.
- **No self-service unlock.** After 5 failed logins an account locks; today an admin would clear the
  `status`/`failed_attempts` columns to unlock it (no dedicated screen yet).
- **Prerequisites are not enforced** — the brief lists this as optional and it's left out.
