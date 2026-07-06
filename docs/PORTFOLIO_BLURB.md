# Portfolio / LinkedIn descriptions

Every claim below is backed by code in this repo — safe to say in an interview.

## LinkedIn "Projects" version (recommended)

**University ERP — Java + Swing desktop application**

Built a desktop ERP that manages courses, sections, enrollments and grades for three roles
(student, instructor, admin), applying core OOP across modelled entities (Student, Instructor,
Course, Section, Enrollment, Grade). Designed a clean 3-layer architecture (Swing UI → service →
JDBC data) that centralizes all business logic and authorization in the service layer, so no screen
touches the database directly. Implemented role-based access control and a system-wide maintenance
mode, plus a "UNIX shadow"-style split into two separate MySQL databases — an auth DB holding only
bcrypt password hashes and an ERP DB for academic data — linked by a shared user ID.

**Highlights**
- Role-based access enforced in one place; every write checks "is this allowed?" and "is
  maintenance off?" before proceeding
- bcrypt-hashed credentials in a separate auth database (no plaintext, no passwords in the ERP DB)
- Weighted final-grade computation, transcript export (CSV/PDF) and grade-sheet export (CSV)
- HikariCP connection pooling, FlatLaf UI, JUnit test suite, built with Maven

**Tech:** Java, Swing, MySQL, JDBC, HikariCP, jBCrypt, FlatLaf, MigLayout, OpenCSV, OpenPDF, JUnit, Maven

---

## Resume bullet version (tighter — 3 lines)

- Built a **Java/Swing** desktop ERP for courses, sections, enrollments and grades with **role-based
  access** for 3 user types, applying core OOP across modelled domain entities.
- Designed a **3-layer architecture (UI → service → JDBC data)** centralizing all logic and
  authorization in the service layer; added a system-wide maintenance mode.
- Separated auth and academic data into **two MySQL databases** ("shadow"-style) linked by a shared
  user ID, storing only **bcrypt** password hashes; added weighted grade computation and CSV/PDF exports.

---

## One-liner (for a headline / summary)

Java + Swing university ERP with a 3-layer architecture, role-based access control, and a two-database
(auth/ERP) design storing only bcrypt password hashes.
