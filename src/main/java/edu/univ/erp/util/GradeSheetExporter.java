package edu.univ.erp.util;

import com.opencsv.CSVWriter;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Section;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Exports a section's roster and final grades to CSV for the instructor. Like the transcript
 * exporter, this only formats data it is handed - it never touches the database.
 */
public final class GradeSheetExporter {

    private GradeSheetExporter() {
    }

    public static void toCsv(String path, Section section, List<Enrollment> roster) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
            writer.writeNext(new String[]{
                    "Grade sheet", section.getCourseCode() + " " + section.getCourseTitle(),
                    section.getSemester() + " " + section.getYear()});
            writer.writeNext(new String[]{});
            writer.writeNext(new String[]{"Roll No", "Student", "Final Grade"});
            for (Enrollment e : roster) {
                writer.writeNext(new String[]{
                        e.getStudentRollNo(),
                        e.getStudentName(),
                        e.getFinalGrade() == null ? "-" : String.valueOf(e.getFinalGrade())
                });
            }
        } catch (IOException e) {
            throw new ExportException("Could not write grade CSV: " + e.getMessage(), e);
        }
    }
}
