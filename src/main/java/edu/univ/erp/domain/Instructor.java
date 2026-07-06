package edu.univ.erp.domain;

/**
 * Instructor profile in the ERP db (erp_main.instructors). Like Student, its user_id matches
 * the account row over in the auth db.
 */
public class Instructor {

    private int userId;
    private String fullName;
    private String department;

    public Instructor() {
    }

    public Instructor(int userId, String fullName, String department) {
        this.userId = userId;
        this.fullName = fullName;
        this.department = department;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return fullName + " (" + department + ")";
    }
}
