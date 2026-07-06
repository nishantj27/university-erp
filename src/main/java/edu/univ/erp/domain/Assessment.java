package edu.univ.erp.domain;

/**
 * An assessment component the instructor defines for a section (erp_main.assessments),
 * e.g. "Quiz" worth 20, "Midterm" worth 30, "End-sem" worth 50. The weights are what the
 * final-grade calculation uses, so for a section they are expected to add up to 100.
 */
public class Assessment {

    private int id;
    private int sectionId;
    private String name;
    private double weight;     // contribution to the final, as a percentage (e.g. 20, 30, 50)
    private double maxScore;   // what a full score on this component is out of (e.g. 100)

    public Assessment() {
    }

    public Assessment(int id, int sectionId, String name, double weight, double maxScore) {
        this.id = id;
        this.sectionId = sectionId;
        this.name = name;
        this.weight = weight;
        this.maxScore = maxScore;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return name + " (" + weight + "%)";
    }
}
