package edu.univ.erp.domain;

/**
 * A course in the catalog (erp_main.courses), e.g. code "CS101", title "Intro to Programming".
 * A course can be offered as many sections across terms.
 */
public class Course {

    private int id;
    private String code;
    private String title;
    private int credits;

    public Course() {
    }

    public Course(int id, String code, String title, int credits) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.credits = credits;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    @Override
    public String toString() {
        return code + " " + title;
    }
}
