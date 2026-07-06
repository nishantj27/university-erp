package edu.univ.erp.domain;

import java.time.LocalDate;

/**
 * A specific offering of a course in a term (erp_main.sections): who teaches it, when/where it
 * meets, how many seats, and the add/drop deadline.
 *
 * The course code/title, instructor name and current enrolled count are not columns of the
 * sections table - they are filled in from joins so the UI has something readable to show.
 */
public class Section {

    private int id;
    private int courseId;
    private Integer instructorId;   // nullable: a section may not have an instructor assigned yet
    private String dayTime;         // e.g. "Mon/Wed 10:00-11:00"
    private String room;
    private int capacity;
    private String semester;        // e.g. "Monsoon"
    private int year;
    private LocalDate addDropDeadline;

    // display-only fields, filled from joins (not stored on the sections row itself)
    private String courseCode;
    private String courseTitle;
    private int credits;
    private String instructorName;
    private int enrolledCount;

    public Section() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public Integer getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Integer instructorId) {
        this.instructorId = instructorId;
    }

    public String getDayTime() {
        return dayTime;
    }

    public void setDayTime(String dayTime) {
        this.dayTime = dayTime;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public LocalDate getAddDropDeadline() {
        return addDropDeadline;
    }

    public void setAddDropDeadline(LocalDate addDropDeadline) {
        this.addDropDeadline = addDropDeadline;
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

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(int enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    /** Seats left, never negative. Used for the "Section full" check and the catalog display. */
    public int seatsAvailable() {
        int left = capacity - enrolledCount;
        return Math.max(left, 0);
    }

    public boolean isFull() {
        return enrolledCount >= capacity;
    }

    @Override
    public String toString() {
        return courseCode + " [" + semester + " " + year + "]";
    }
}
