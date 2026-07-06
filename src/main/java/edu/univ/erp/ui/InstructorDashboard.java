package edu.univ.erp.ui;

import edu.univ.erp.AppContext;
import edu.univ.erp.domain.Assessment;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.ClassStats;
import edu.univ.erp.util.GradeSheetExporter;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * What an instructor sees: their own sections on top, and the gradebook for the selected section
 * below. They can define assessments, enter scores, compute finals, view class stats and export a
 * grade CSV. The service refuses anything to do with a section they don't teach.
 */
public class InstructorDashboard extends Dashboard {

    private final User me;

    private final DefaultTableModel sectionModel =
            Tables.readOnlyModel("Code", "Title", "Semester", "Room", "Enrolled");
    private final DefaultTableModel rosterModel =
            Tables.readOnlyModel("Roll No", "Student", "Final Grade");

    private final List<Section> sectionRows = new ArrayList<>();
    private final List<Enrollment> rosterRows = new ArrayList<>();

    private final JTable sectionTable = Tables.sortable(sectionModel);
    private final JTable rosterTable = Tables.sortable(rosterModel);

    public InstructorDashboard(AppContext ctx) {
        super(ctx, "Instructor", true);
        this.me = ctx.getSession().getUser();
        assemble(buildContent());
        loadSections();
    }

    private JComponent buildContent() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 12", "[grow]", "[]8[grow]6[]12[]8[grow]6[]"));

        panel.add(sectionTitle("My Sections"), "wrap");
        sectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadRoster();
            }
        });
        panel.add(new JScrollPane(sectionTable), "grow, wrap");

        JButton stats = new JButton("Class stats");
        stats.addActionListener(e -> showStats());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadSections());
        JPanel sectionButtons = new JPanel(new MigLayout("insets 0"));
        sectionButtons.add(stats);
        sectionButtons.add(refresh);
        panel.add(sectionButtons, "wrap");

        panel.add(sectionTitle("Gradebook for the selected section"), "wrap");
        panel.add(new JScrollPane(rosterTable), "grow, wrap");

        JButton assessments = new JButton("Manage assessments");
        assessments.addActionListener(e -> manageAssessments());
        JButton enterScore = new JButton("Enter score");
        enterScore.addActionListener(e -> enterScore());
        JButton computeFinals = new JButton("Compute finals");
        computeFinals.addActionListener(e -> computeFinals());
        JButton exportCsv = new JButton("Export grade CSV");
        exportCsv.addActionListener(e -> exportCsv());

        JPanel rosterButtons = new JPanel(new MigLayout("insets 0"));
        rosterButtons.add(assessments);
        rosterButtons.add(enterScore);
        rosterButtons.add(computeFinals);
        rosterButtons.add(exportCsv);
        panel.add(rosterButtons);
        return panel;
    }

    // --- actions ----------------------------------------------------------

    private void manageAssessments() {
        Section section = selectedSection();
        if (section == null) {
            return;
        }
        List<Assessment> current = ctx.gradeService().assessments(me, section.getId());
        StringBuilder sb = new StringBuilder("Current assessments:\n");
        if (current.isEmpty()) {
            sb.append("  (none yet)\n");
        }
        for (Assessment a : current) {
            sb.append(String.format("  %s - weight %s, out of %s%n", a.getName(), trim(a.getWeight()), trim(a.getMaxScore())));
        }
        sb.append("\nAdd a new one?");
        if (!Ui.confirm(this, sb.toString())) {
            return;
        }

        JTextField name = new JTextField(12);
        JTextField weight = new JTextField(6);
        JTextField max = new JTextField(6);
        JPanel form = new JPanel(new MigLayout("wrap 2", "[right][grow,fill]"));
        form.add(new JLabel("Name"));
        form.add(name);
        form.add(new JLabel("Weight (%)"));
        form.add(weight);
        form.add(new JLabel("Max score"));
        form.add(max);
        int choice = JOptionPane.showConfirmDialog(this, form, "New assessment",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }
        Double w = parseDouble(weight.getText());
        Double m = parseDouble(max.getText());
        if (w == null || m == null) {
            Ui.error(this, "Weight and max score must be numbers.");
            return;
        }
        if (run(() -> ctx.gradeService().addAssessment(me, section.getId(), name.getText(), w, m))) {
            Ui.info(this, "Assessment added.");
        }
    }

    private void enterScore() {
        Section section = selectedSection();
        if (section == null) {
            return;
        }
        Enrollment student = selectedStudent();
        if (student == null) {
            Ui.info(this, "Pick a student from the gradebook first.");
            return;
        }
        List<Assessment> assessments = ctx.gradeService().assessments(me, section.getId());
        if (assessments.isEmpty()) {
            Ui.info(this, "Define an assessment first (Manage assessments).");
            return;
        }

        JComboBox<Assessment> pick = new JComboBox<>(assessments.toArray(new Assessment[0]));
        JTextField score = new JTextField(6);
        JPanel form = new JPanel(new MigLayout("wrap 2", "[right][grow,fill]"));
        form.add(new JLabel("Student"));
        form.add(new JLabel(student.getStudentName()));
        form.add(new JLabel("Assessment"));
        form.add(pick);
        form.add(new JLabel("Score"));
        form.add(score);
        int choice = JOptionPane.showConfirmDialog(this, form, "Enter score",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }
        Double value = parseDouble(score.getText());
        Assessment assessment = (Assessment) pick.getSelectedItem();
        if (value == null || assessment == null) {
            Ui.error(this, "Enter a numeric score.");
            return;
        }
        if (run(() -> ctx.gradeService().enterScore(
                me, section.getId(), student.getId(), assessment.getId(), value))) {
            Ui.info(this, "Score saved.");
        }
    }

    private void computeFinals() {
        Section section = selectedSection();
        if (section == null) {
            return;
        }
        if (run(() -> ctx.gradeService().computeFinals(me, section.getId()))) {
            Ui.info(this, "Final grades computed.");
            loadRoster();
        }
    }

    private void showStats() {
        Section section = selectedSection();
        if (section == null) {
            return;
        }
        ClassStats stats = ctx.gradeService().classStats(me, section.getId());
        if (stats.getGradedCount() == 0) {
            Ui.info(this, "No finals computed yet for this section.");
            return;
        }
        Ui.info(this, String.format(
                "%s %s%n%nStudents graded: %d%nAverage: %.2f%nHighest: %.2f%nLowest: %.2f",
                section.getCourseCode(), section.getCourseTitle(),
                stats.getGradedCount(), stats.getAverage(), stats.getHighest(), stats.getLowest()));
    }

    private void exportCsv() {
        Section section = selectedSection();
        if (section == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("grades_" + section.getCourseCode() + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String path = chooser.getSelectedFile().getAbsolutePath();
        if (run(() -> GradeSheetExporter.toCsv(path, section, rosterRows))) {
            Ui.info(this, "Grade sheet saved to:\n" + path);
        }
    }

    // --- loading ----------------------------------------------------------

    private void loadSections() {
        refreshMaintenance();
        sectionRows.clear();
        sectionModel.setRowCount(0);
        for (Section s : ctx.gradeService().mySections(me)) {
            sectionRows.add(s);
            sectionModel.addRow(new Object[]{
                    s.getCourseCode(), s.getCourseTitle(), s.getSemester() + " " + s.getYear(),
                    s.getRoom(), s.getEnrolledCount() + " / " + s.getCapacity()});
        }
        rosterRows.clear();
        rosterModel.setRowCount(0);
    }

    private void loadRoster() {
        Section section = selectedSection();
        rosterRows.clear();
        rosterModel.setRowCount(0);
        if (section == null) {
            return;
        }
        for (Enrollment e : ctx.gradeService().roster(me, section.getId())) {
            rosterRows.add(e);
            rosterModel.addRow(new Object[]{
                    e.getStudentRollNo(), e.getStudentName(),
                    e.getFinalGrade() == null ? "-" : e.getFinalGrade()});
        }
    }

    private Section selectedSection() {
        int viewRow = sectionTable.getSelectedRow();
        if (viewRow < 0) {
            Ui.info(this, "Select one of your sections first.");
            return null;
        }
        return sectionRows.get(sectionTable.convertRowIndexToModel(viewRow));
    }

    private Enrollment selectedStudent() {
        int viewRow = rosterTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return rosterRows.get(rosterTable.convertRowIndexToModel(viewRow));
    }

    private Double parseDouble(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private String trim(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
    }
}
