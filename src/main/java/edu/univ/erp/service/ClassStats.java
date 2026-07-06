package edu.univ.erp.service;

/**
 * Simple summary numbers for a section's computed final grades, shown on the instructor's
 * "class stats" view. Only counts students whose final has actually been computed.
 */
public class ClassStats {

    private final int gradedCount;
    private final double average;
    private final double highest;
    private final double lowest;

    public ClassStats(int gradedCount, double average, double highest, double lowest) {
        this.gradedCount = gradedCount;
        this.average = average;
        this.highest = highest;
        this.lowest = lowest;
    }

    public static ClassStats empty() {
        return new ClassStats(0, 0, 0, 0);
    }

    public int getGradedCount() {
        return gradedCount;
    }

    public double getAverage() {
        return average;
    }

    public double getHighest() {
        return highest;
    }

    public double getLowest() {
        return lowest;
    }
}
