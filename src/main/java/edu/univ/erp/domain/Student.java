package edu.univ.erp.domain;

/**
 * Student profile living in the ERP db (erp_main.students). The user_id is the same value
 * as the matching row in the auth db - that shared id is the only link between the two databases.
 */
public class Student {

    private int userId;
    private String rollNo;
    private String fullName;
    private String program;   // e.g. "B.Tech CSE"
    private int year;

    public Student() {
    }

    public Student(int userId, String rollNo, String fullName, String program, int year) {
        this.userId = userId;
        this.rollNo = rollNo;
        this.fullName = fullName;
        this.program = program;
        this.year = year;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return rollNo + " - " + fullName;
    }
}
