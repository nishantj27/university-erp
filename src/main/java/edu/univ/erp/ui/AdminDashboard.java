package edu.univ.erp.ui;

import edu.univ.erp.AppContext;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.User;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * What an admin sees: manage users, the course catalog and sections, and flip maintenance mode.
 * The admin is the only role allowed to make changes while maintenance is on (they're the one who
 * turns it off again), so there's no maintenance banner here - just the toggle.
 */
public class AdminDashboard extends Dashboard {

    private final User me;

    private final DefaultTableModel studentModel =
            Tables.readOnlyModel("Roll No", "Name", "Program", "Year");
    private final DefaultTableModel instructorModel =
            Tables.readOnlyModel("Name", "Department");
    private final DefaultTableModel courseModel =
            Tables.readOnlyModel("Code", "Title", "Credits");
    private final DefaultTableModel sectionModel =
            Tables.readOnlyModel("Code", "Title", "Instructor", "Capacity", "Enrolled", "Semester");

    private final List<Section> sectionRows = new ArrayList<>();

    private final JTable studentTable = Tables.sortable(studentModel);
    private final JTable instructorTable = Tables.sortable(instructorModel);
    private final JTable courseTable = Tables.sortable(courseModel);
    private final JTable sectionTable = Tables.sortable(sectionModel);

    private JLabel maintenanceStatus;

    public AdminDashboard(AppContext ctx) {
        super(ctx, "Admin", false);
        this.me = ctx.getSession().getUser();
        assemble(buildTabs());
        reloadAll();
    }

    private JComponent buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Users", buildUsersTab());
        tabs.addTab("Courses", buildCoursesTab());
        tabs.addTab("Sections", buildSectionsTab());
        tabs.addTab("Maintenance", buildMaintenanceTab());
        return tabs;
    }

    // --- users ------------------------------------------------------------

    private JComponent buildUsersTab() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 12", "[grow]", "[][grow][][grow][]"));

        panel.add(sectionTitle("Students"), "wrap");
        panel.add(new JScrollPane(studentTable), "grow, wrap");
        JButton addStudent = new JButton("Add student");
        addStudent.addActionListener(e -> addStudentDialog());
        panel.add(addStudent, "wrap");

        panel.add(sectionTitle("Instructors"), "wrap");
        panel.add(new JScrollPane(instructorTable), "grow, wrap");
        JPanel buttons = new JPanel(new MigLayout("insets 0"));
        JButton addInstructor = new JButton("Add instructor");
        addInstructor.addActionListener(e -> addInstructorDialog());
        JButton addAdmin = new JButton("Add admin");
        addAdmin.addActionListener(e -> addAdminDialog());
        buttons.add(addInstructor);
        buttons.add(addAdmin);
        panel.add(buttons);
        return panel;
    }

    private void addStudentDialog() {
        JTextField username = new JTextField(14);
        JPasswordField password = new JPasswordField(14);
        JTextField roll = new JTextField(14);
        JTextField name = new JTextField(14);
        JTextField program = new JTextField(14);
        JTextField year = new JTextField(4);
        JPanel form = new JPanel(new MigLayout("wrap 2", "[right][grow,fill]"));
        form.add(new JLabel("Username"));
        form.add(username);
        form.add(new JLabel("Password"));
        form.add(password);
        form.add(new JLabel("Roll no"));
        form.add(roll);
        form.add(new JLabel("Full name"));
        form.add(name);
        form.add(new JLabel("Program"));
        form.add(program);
        form.add(new JLabel("Year"));
        form.add(year);

        if (!okCancel(form, "Add student")) {
            return;
        }
        Integer yr = parseInt(year.getText());
        if (yr == null) {
            Ui.error(this, "Year must be a number.");
            return;
        }
        boolean ok = run(() -> ctx.adminService().addStudent(me,
                username.getText(), new String(password.getPassword()),
                roll.getText(), name.getText(), program.getText(), yr));
        if (ok) {
            Ui.info(this, "Student created (login + profile).");
            reloadAll();
        }
    }

    private void addInstructorDialog() {
        JTextField username = new JTextField(14);
        JPasswordField password = new JPasswordField(14);
        JTextField name = new JTextField(14);
        JTextField dept = new JTextField(14);
        JPanel form = new JPanel(new MigLayout("wrap 2", "[right][grow,fill]"));
        form.add(new JLabel("Username"));
        form.add(username);
        form.add(new JLabel("Password"));
        form.add(password);
        form.add(new JLabel("Full name"));
        form.add(name);
        form.add(new JLabel("Department"));
        form.add(dept);

        if (!okCancel(form, "Add instructor")) {
            return;
        }
        boolean ok = run(() -> ctx.adminService().addInstructor(me,
                username.getText(), new String(password.getPassword()), name.getText(), dept.getText()));
        if (ok) {
            Ui.info(this, "Instructor created (login + profile).");
            reloadAll();
        }
    }

    private void addAdminDialog() {
        JTextField username = new JTextField(14);
        JPasswordField password = new JPasswordField(14);
        JPanel form = new JPanel(new MigLayout("wrap 2", "[right][grow,fill]"));
        form.add(new JLabel("Username"));
        form.add(username);
        form.add(new JLabel("Password"));
        form.add(password);

        if (!okCancel(form, "Add admin")) {
            return;
        }
        boolean ok = run(() -> ctx.adminService().addAdmin(me,
                username.getText(), new String(password.getPassword())));
        if (ok) {
            Ui.info(this, "Admin account created.");
        }
    }

    // --- courses ----------------------------------------------------------

    private JComponent buildCoursesTab() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 12", "[grow]", "[][grow][]"));
        panel.add(sectionTitle("Course catalog"), "wrap");
        panel.add(new JScrollPane(courseTable), "grow, wrap");
        JButton add = new JButton("Create course");
        add.addActionListener(e -> createCourseDialog());
        panel.add(add);
        return panel;
    }

    private void createCourseDialog() {
        JTextField code = new JTextField(10);
        JTextField title = new JTextField(18);
        JTextField credits = new JTextField(4);
        JPanel form = new JPanel(new MigLayout("wrap 2", "[right][grow,fill]"));
        form.add(new JLabel("Code"));
        form.add(code);
        form.add(new JLabel("Title"));
        form.add(title);
        form.add(new JLabel("Credits"));
        form.add(credits);

        if (!okCancel(form, "Create course")) {
            return;
        }
        Integer cr = parseInt(credits.getText());
        if (cr == null) {
            Ui.error(this, "Credits must be a number.");
            return;
        }
        boolean ok = run(() -> ctx.adminService().createCourse(me, code.getText(), title.getText(), cr));
        if (ok) {
            Ui.info(this, "Course created.");
            reloadAll();
        }
    }

    // --- sections ---------------------------------------------------------

    private JComponent buildSectionsTab() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 12", "[grow]", "[][grow][]"));
        panel.add(sectionTitle("Sections"), "wrap");
        panel.add(new JScrollPane(sectionTable), "grow, wrap");

        JPanel buttons = new JPanel(new MigLayout("insets 0"));
        JButton create = new JButton("Create section");
        create.addActionListener(e -> createSectionDialog());
        JButton assign = new JButton("Assign instructor");
        assign.addActionListener(e -> assignInstructorDialog());
        JButton delete = new JButton("Delete section");
        delete.addActionListener(e -> deleteSelectedSection());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> reloadAll());
        buttons.add(create);
        buttons.add(assign);
        buttons.add(delete);
        buttons.add(refresh);
        panel.add(buttons);
        return panel;
    }

    private void createSectionDialog() {
        List<Course> courses = ctx.adminService().listCourses(me);
        if (courses.isEmpty()) {
            Ui.info(this, "Create a course first.");
            return;
        }
        List<Instructor> instructors = ctx.adminService().listInstructors(me);

        JComboBox<Course> course = new JComboBox<>(courses.toArray(new Course[0]));
        JComboBox<Object> instructor = new JComboBox<>();
        instructor.addItem("(none)");
        for (Instructor i : instructors) {
            instructor.addItem(i);
        }
        JTextField dayTime = new JTextField("Mon/Wed 10:00-11:00", 14);
        JTextField room = new JTextField(8);
        JTextField capacity = new JTextField("30", 5);
        JTextField semester = new JTextField("Monsoon", 10);
        JTextField year = new JTextField("2026", 5);
        JTextField deadline = new JTextField("2026-08-15", 10);

        JPanel form = new JPanel(new MigLayout("wrap 2", "[right][grow,fill]"));
        form.add(new JLabel("Course"));
        form.add(course);
        form.add(new JLabel("Instructor"));
        form.add(instructor);
        form.add(new JLabel("Day/Time"));
        form.add(dayTime);
        form.add(new JLabel("Room"));
        form.add(room);
        form.add(new JLabel("Capacity"));
        form.add(capacity);
        form.add(new JLabel("Semester"));
        form.add(semester);
        form.add(new JLabel("Year"));
        form.add(year);
        form.add(new JLabel("Add/drop deadline"));
        form.add(deadline);

        if (!okCancel(form, "Create section")) {
            return;
        }
        Integer cap = parseInt(capacity.getText());
        Integer yr = parseInt(year.getText());
        LocalDate dl = parseDate(deadline.getText());
        if (cap == null || yr == null) {
            Ui.error(this, "Capacity and year must be numbers.");
            return;
        }
        if (dl == null) {
            Ui.error(this, "Deadline must look like 2026-08-15.");
            return;
        }
        Course chosenCourse = (Course) course.getSelectedItem();
        Object chosenInstructor = instructor.getSelectedItem();
        Integer instructorId = chosenInstructor instanceof Instructor
                ? ((Instructor) chosenInstructor).getUserId() : null;

        boolean ok = run(() -> ctx.adminService().createSection(me,
                chosenCourse.getId(), instructorId, dayTime.getText(), room.getText(),
                cap, semester.getText(), yr, dl));
        if (ok) {
            Ui.info(this, "Section created.");
            reloadAll();
        }
    }

    private void assignInstructorDialog() {
        Section section = selectedSection();
        if (section == null) {
            return;
        }
        List<Instructor> instructors = ctx.adminService().listInstructors(me);
        JComboBox<Object> instructor = new JComboBox<>();
        instructor.addItem("(none)");
        for (Instructor i : instructors) {
            instructor.addItem(i);
        }
        JPanel form = new JPanel(new MigLayout("wrap 2", "[right][grow,fill]"));
        form.add(new JLabel("Section"));
        form.add(new JLabel(section.getCourseCode() + " - " + section.getCourseTitle()));
        form.add(new JLabel("Instructor"));
        form.add(instructor);

        if (!okCancel(form, "Assign instructor")) {
            return;
        }
        Object chosen = instructor.getSelectedItem();
        Integer instructorId = chosen instanceof Instructor ? ((Instructor) chosen).getUserId() : null;
        if (run(() -> ctx.adminService().assignInstructor(me, section.getId(), instructorId))) {
            Ui.info(this, "Instructor updated.");
            reloadAll();
        }
    }

    private void deleteSelectedSection() {
        Section section = selectedSection();
        if (section == null) {
            return;
        }
        if (!Ui.confirm(this, "Delete section " + section.getCourseCode() + "?")) {
            return;
        }
        if (run(() -> ctx.adminService().deleteSection(me, section.getId()))) {
            Ui.info(this, "Section deleted.");
            reloadAll();
        }
    }

    private Section selectedSection() {
        int viewRow = sectionTable.getSelectedRow();
        if (viewRow < 0) {
            Ui.info(this, "Select a section first.");
            return null;
        }
        return sectionRows.get(sectionTable.convertRowIndexToModel(viewRow));
    }

    // --- maintenance ------------------------------------------------------

    private JComponent buildMaintenanceTab() {
        JPanel panel = new JPanel(new MigLayout("insets 24", "[grow]"));
        panel.add(sectionTitle("Maintenance Mode"), "wrap");
        panel.add(new JLabel("<html>When ON, students and instructors can log in and view data, "
                + "but any change is blocked.<br>As admin, you can still make changes.</html>"), "wrap, gapbottom 12");

        maintenanceStatus = new JLabel();
        maintenanceStatus.setFont(maintenanceStatus.getFont().deriveFont(15f));
        panel.add(maintenanceStatus, "wrap, gapbottom 12");

        JButton toggle = new JButton("Toggle maintenance");
        toggle.addActionListener(e -> toggleMaintenance());
        panel.add(toggle);
        return panel;
    }

    private void toggleMaintenance() {
        boolean now = ctx.adminService().isMaintenanceOn();
        if (run(() -> ctx.adminService().setMaintenance(me, !now))) {
            refreshMaintenanceStatus();
            Ui.info(this, "Maintenance mode is now " + (!now ? "ON" : "OFF") + ".");
        }
    }

    private void refreshMaintenanceStatus() {
        boolean on = ctx.adminService().isMaintenanceOn();
        maintenanceStatus.setText("Current status:  " + (on ? "ON (changes blocked for students/instructors)" : "OFF (normal)"));
    }

    // --- loading ----------------------------------------------------------

    private void reloadAll() {
        studentModel.setRowCount(0);
        for (Student s : ctx.adminService().listStudents(me)) {
            studentModel.addRow(new Object[]{s.getRollNo(), s.getFullName(), s.getProgram(), s.getYear()});
        }
        instructorModel.setRowCount(0);
        for (Instructor i : ctx.adminService().listInstructors(me)) {
            instructorModel.addRow(new Object[]{i.getFullName(), i.getDepartment()});
        }
        courseModel.setRowCount(0);
        for (Course c : ctx.adminService().listCourses(me)) {
            courseModel.addRow(new Object[]{c.getCode(), c.getTitle(), c.getCredits()});
        }
        sectionRows.clear();
        sectionModel.setRowCount(0);
        for (Section s : ctx.adminService().listSections(me)) {
            sectionRows.add(s);
            sectionModel.addRow(new Object[]{
                    s.getCourseCode(), s.getCourseTitle(),
                    s.getInstructorName() == null ? "(unassigned)" : s.getInstructorName(),
                    s.getCapacity(), s.getEnrolledCount(), s.getSemester() + " " + s.getYear()});
        }
        if (maintenanceStatus != null) {
            refreshMaintenanceStatus();
        }
    }

    // --- small helpers ----------------------------------------------------

    private boolean okCancel(JComponent form, String title) {
        int choice = JOptionPane.showConfirmDialog(this, form, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return choice == JOptionPane.OK_OPTION;
    }

    private Integer parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }
}
