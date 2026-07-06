package edu.univ.erp.util;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Student;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Turns a student's enrollments into a downloadable transcript, either as CSV or PDF. This class
 * is pure formatting - the caller fetches the data through the services and passes it in, so no
 * database code lives here.
 */
public final class TranscriptExporter {

    private TranscriptExporter() {
    }

    public static void toCsv(String path, Student student, List<Enrollment> enrollments) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
            writer.writeNext(new String[]{"Transcript", student.getFullName(), "Roll: " + student.getRollNo()});
            writer.writeNext(new String[]{});
            writer.writeNext(new String[]{"Course Code", "Title", "Credits", "Final Grade"});
            for (Enrollment e : enrollments) {
                writer.writeNext(new String[]{
                        e.getCourseCode(),
                        e.getCourseTitle(),
                        String.valueOf(e.getCredits()),
                        e.getFinalGrade() == null ? "-" : String.valueOf(e.getFinalGrade())
                });
            }
        } catch (IOException e) {
            throw new ExportException("Could not write transcript CSV: " + e.getMessage(), e);
        }
    }

    public static void toPdf(String path, Student student, List<Enrollment> enrollments) {
        Document doc = new Document();
        try (FileOutputStream out = new FileOutputStream(path)) {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            doc.add(new Paragraph("Academic Transcript", titleFont));
            doc.add(new Paragraph(student.getFullName() + "  (" + student.getRollNo() + ")"));
            doc.add(new Paragraph(student.getProgram() + ", Year " + student.getYear()));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            addHeaderCell(table, "Course Code");
            addHeaderCell(table, "Title");
            addHeaderCell(table, "Credits");
            addHeaderCell(table, "Final Grade");
            for (Enrollment e : enrollments) {
                table.addCell(e.getCourseCode());
                table.addCell(e.getCourseTitle());
                table.addCell(String.valueOf(e.getCredits()));
                table.addCell(e.getFinalGrade() == null ? "-" : String.valueOf(e.getFinalGrade()));
            }
            doc.add(table);
        } catch (IOException e) {
            throw new ExportException("Could not write transcript PDF: " + e.getMessage(), e);
        } finally {
            if (doc.isOpen()) {
                doc.close();
            }
        }
    }

    private static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
    }
}
