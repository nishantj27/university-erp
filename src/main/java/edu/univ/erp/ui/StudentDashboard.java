package edu.univ.erp.ui;

import edu.univ.erp.AppContext;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.User;
import edu.univ.erp.util.TranscriptExporter;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * What a student sees: browse the catalog and register, see their registrations/timetable and
 * drop, and view grades with a transcript download. Nothing here talks to the database directly;
 * every action goes through the enrollment service, which enforces the rules.
 */
public class StudentDashboard extends Dashboard {

    private final User me;

    private final DefaultTableModel catalogModel =
            Tables.readOnlyModel("Code", "Title", "Credits", "Instructor", "Day/Time", "Room", "Seats");
    private final DefaultTableModel regModel =
            Tables.readOnlyModel("Code", "Title", "Credits", "Day/Time", "Room", "Semester");
    private final DefaultTableModel gradeModel =
            Tables.readOnlyModel("Code", "Title", "Credits", "Final Grade");

    // rows shown in each table, in model order, so a selected row maps back to a domain object
    private final List<Section> catalogRows = new ArrayList<>();
    private final List<Section> regRows = new ArrayList<>();
    private final List<Enrollment> gradeRows = new ArrayList<>();

    private final JTable catalogTable = Tables.sortable(catalogModel);
    private final JTable regTable = Tables.sortable(regModel);
    private final JTable gradeTable = Tables.sortable(gradeModel);

    public StudentDashboard(AppContext ctx) {
        super(ctx, "Student", true);
        this.me = ctx.getSession().getUser();
        assemble(buildTabs());
        reloadAll();
    }

    private JComponent buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Course Catalog", buildCatalogTab());
        tabs.addTab("My Registrations", buildRegistrationsTab());
        tabs.addTab("My Grades", buildGradesTab());
        return tabs;
    }

    // --- catalog ----------------------------------------------------------

    private JComponent buildCatalogTab() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 12", "[grow]", "[][grow][]"));
        panel.add(sectionTitle("Browse and register for sections"), "wrap");
        panel.add(new JScrollPane(catalogTable), "grow, wrap");

        JButton register = new JButton("Register for selected");
        register.addActionListener(e -> registerSelected());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> reloadAll());

        JPanel buttons = new JPanel(new MigLayout("insets 0"));
        buttons.add(register);
        buttons.add(refresh);
        panel.add(buttons);
        return panel;
    }

    private void registerSelected() {
        Section section = selected(catalogTable, catalogRows);
        if (section == null) {
            Ui.info(this, "Pick a section from the list first.");
            return;
        }
        if (run(() -> ctx.enrollmentService().register(me, section.getId()))) {
            Ui.info(this, "Registered for " + section.getCourseCode() + ".");
            reloadAll();
        }
    }

    // --- registrations / timetable ---------------------------------------

    private JComponent buildRegistrationsTab() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 12", "[grow]", "[][grow][]"));
        panel.add(sectionTitle("Your registered sections (this is your timetable)"), "wrap");
        panel.add(new JScrollPane(regTable), "grow, wrap");

        JButton drop = new JButton("Drop selected");
        drop.addActionListener(e -> dropSelected());
        panel.add(drop);
        return panel;
    }

    private void dropSelected() {
        Section section = selected(regTable, regRows);
        if (section == null) {
            Ui.info(this, "Pick a section to drop first.");
            return;
        }
        if (!Ui.confirm(this, "Drop " + section.getCourseCode() + "?")) {
            return;
        }
        if (run(() -> ctx.enrollmentService().drop(me, section.getId()))) {
            Ui.info(this, "Dropped " + section.getCourseCode() + ".");
            reloadAll();
        }
    }

    // --- grades -----------------------------------------------------------

    private JComponent buildGradesTab() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 12", "[grow]", "[][grow][]"));
        panel.add(sectionTitle("Your grades"), "wrap");
        panel.add(new JScrollPane(gradeTable), "grow, wrap");

        JButton breakdown = new JButton("View breakdown");
        breakdown.addActionListener(e -> showBreakdown());
        JButton transcript = new JButton("Download transcript");
        transcript.addActionListener(e -> downloadTranscript());

        JPanel buttons = new JPanel(new MigLayout("insets 0"));
        buttons.add(breakdown);
        buttons.add(transcript);
        panel.add(buttons);
        return panel;
    }

    private void showBreakdown() {
        Enrollment enrollment = selected(gradeTable, gradeRows);
        if (enrollment == null) {
            Ui.info(this, "Pick a course to see its breakdown.");
            return;
        }
        List<Grade> grades = ctx.enrollmentService().gradeBreakdown(me, enrollment.getId());
        if (grades.isEmpty()) {
            Ui.info(this, "No scores have been entered for this course yet.");
            return;
        }
        StringBuilder sb = new StringBuilder(enrollment.getCourseCode() + " - " + enrollment.getCourseTitle() + "\n\n");
        for (Grade g : grades) {
            sb.append(String.format("%-12s %s / %s   (weight %s%%)%n",
                    g.getAssessmentName(), trim(g.getScore()), trim(g.getMaxScore()), trim(g.getWeight())));
        }
        sb.append("\nFinal: ")
                .append(enrollment.getFinalGrade() == null ? "not computed yet" : trim(enrollment.getFinalGrade()));
        Ui.info(this, sb.toString());
    }

    private void downloadTranscript() {
        Student student = ctx.profileService().student(me).orElse(null);
        if (student == null) {
            Ui.error(this, "Could not load your student profile.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save transcript (name it .csv or .pdf)");
        chooser.setSelectedFile(new File("transcript_" + student.getRollNo() + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String path = chooser.getSelectedFile().getAbsolutePath();
        boolean ok = run(() -> {
            if (path.toLowerCase().endsWith(".pdf")) {
                TranscriptExporter.toPdf(path, student, gradeRows);
            } else {
                TranscriptExporter.toCsv(path, student, gradeRows);
            }
        });
        if (ok) {
            Ui.info(this, "Transcript saved to:\n" + path);
        }
    }

    // --- loading ----------------------------------------------------------

    private void reloadAll() {
        refreshMaintenance();
        loadCatalog();
        loadRegistrations();
        loadGrades();
    }

    private void loadCatalog() {
        catalogRows.clear();
        catalogModel.setRowCount(0);
        for (Section s : ctx.catalogService().listSections()) {
            catalogRows.add(s);
            catalogModel.addRow(new Object[]{
                    s.getCourseCode(), s.getCourseTitle(), s.getCredits(),
                    s.getInstructorName() == null ? "(unassigned)" : s.getInstructorName(),
                    s.getDayTime(), s.getRoom(), s.seatsAvailable() + " / " + s.getCapacity()});
        }
    }

    private void loadRegistrations() {
        regRows.clear();
        regModel.setRowCount(0);
        for (Section s : ctx.enrollmentService().myRegisteredSections(me)) {
            regRows.add(s);
            regModel.addRow(new Object[]{
                    s.getCourseCode(), s.getCourseTitle(), s.getCredits(),
                    s.getDayTime(), s.getRoom(), s.getSemester() + " " + s.getYear()});
        }
    }

    private void loadGrades() {
        gradeRows.clear();
        gradeModel.setRowCount(0);
        for (Enrollment e : ctx.enrollmentService().myEnrollments(me)) {
            gradeRows.add(e);
            gradeModel.addRow(new Object[]{
                    e.getCourseCode(), e.getCourseTitle(), e.getCredits(),
                    e.getFinalGrade() == null ? "-" : e.getFinalGrade()});
        }
    }

    /** Map the selected view row back to the domain object behind it (handles column sorting). */
    private <T> T selected(JTable table, List<T> rows) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return rows.get(modelRow);
    }

    private String trim(double v) {
        if (v == Math.floor(v)) {
            return String.valueOf((long) v);
        }
        return String.valueOf(v);
    }
}
