package edu.univ.erp.domain;

/**
 * Links a student to a section (erp_main.enrollments). There can only be one row per
 * (student, section) pair - that unique rule is what stops duplicate registrations.
 *
 * final_grade stays null until the instructor computes it from the assessment scores.
 */
public class Enrollment {

    private int id;
    private int studentId;
    private int sectionId;
    private String status;       // "REGISTERED" or "DROPPED"
    private Double finalGrade;   // null until computed

    // display-only fields from joins
    private String courseCode;
    private String courseTitle;
    private int credits;
    private String studentRollNo;
    private String studentName;

    public Enrollment() {
    }

    public Enrollment(int id, int studentId, int sectionId, String status, Double finalGrade) {
        this.id = id;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = status;
        this.finalGrade = finalGrade;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(Double finalGrade) {
        this.finalGrade = finalGrade;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public boolean isGraded() {
        return finalGrade != null;
    }
}
