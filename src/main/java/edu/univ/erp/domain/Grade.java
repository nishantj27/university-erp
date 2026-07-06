package edu.univ.erp.domain;

/**
 * One student's score on one assessment (erp_main.grades). A student has at most one grade row
 * per assessment. The final grade for the whole course is not stored here - it is computed from
 * these rows and kept on the enrollment.
 */
public class Grade {

    private int id;
    private int enrollmentId;
    private int assessmentId;
    private double score;

    // display-only fields from joins, handy when computing/showing the breakdown
    private String assessmentName;
    private double weight;
    private double maxScore;

    public Grade() {
    }

    public Grade(int id, int enrollmentId, int assessmentId, double score) {
        this.id = id;
        this.enrollmentId = enrollmentId;
        this.assessmentId = assessmentId;
        this.score = score;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public int getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(int assessmentId) {
        this.assessmentId = assessmentId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getAssessmentName() {
        return assessmentName;
    }

    public void setAssessmentName(String assessmentName) {
        this.assessmentName = assessmentName;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
    }
}
