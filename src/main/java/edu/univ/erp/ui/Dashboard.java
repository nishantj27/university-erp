package edu.univ.erp.ui;

import edu.univ.erp.AppContext;
import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.data.DataException;
import edu.univ.erp.service.ServiceException;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

/**
 * Shared frame for the three role dashboards. It builds the header (who's logged in + logout +
 * change password), optionally shows the maintenance banner, and gives subclasses one place to
 * run a service call with consistent error handling. Subclasses just fill in the center content.
 */
public abstract class Dashboard extends JFrame {

    protected final AppContext ctx;
    private final boolean showBanner;
    private final MaintenanceBanner banner = new MaintenanceBanner();

    protected Dashboard(AppContext ctx, String titleSuffix, boolean showBanner) {
        this.ctx = ctx;
        this.showBanner = showBanner;
        setTitle("University ERP - " + titleSuffix);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(940, 620);
        setLocationRelativeTo(null);
    }

    /** Subclasses call this at the end of their constructor once their center content is ready. */
    protected void assemble(JComponent center) {
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.add(buildHeader(), BorderLayout.NORTH);
        if (showBanner) {
            top.add(banner, BorderLayout.SOUTH);
        }
        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        refreshMaintenance();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new MigLayout("insets 10 16 10 16", "[grow][][]"));
        header.setBackground(new Color(0x1E293B));

        String name = ctx.profileService().displayName(ctx.getSession().getUser());
        JLabel who = new JLabel(name + "   •   " + ctx.getSession().getRole());
        who.setForeground(Color.WHITE);
        who.setFont(who.getFont().deriveFont(Font.BOLD, 14f));
        header.add(who, "growx");

        JButton changePw = new JButton("Change Password");
        changePw.addActionListener(e -> changePasswordDialog());
        header.add(changePw);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> logout());
        header.add(logout);

        return header;
    }

    /** Re-read the maintenance flag and show/hide the banner. Called on open and after refreshes. */
    protected void refreshMaintenance() {
        if (showBanner) {
            try {
                banner.refresh(ctx.adminService().isMaintenanceOn());
            } catch (DataException ignored) {
                // if the DB is momentarily unreachable, just leave the banner as-is
            }
        }
    }

    /**
     * Run a service action and turn any expected failure into a friendly dialog. Returns true if
     * the action succeeded, so callers can refresh their tables only when something actually changed.
     */
    protected boolean run(Runnable action) {
        try {
            action.run();
            return true;
        } catch (AccessDeniedException | ServiceException ex) {
            Ui.error(this, ex.getMessage());
        } catch (DataException ex) {
            Ui.error(this, "Database problem: " + ex.getMessage());
        }
        return false;
    }

    private void changePasswordDialog() {
        JPanel form = new JPanel(new MigLayout("wrap 2", "[right][grow,fill]"));
        JPasswordField current = new JPasswordField(16);
        JPasswordField fresh = new JPasswordField(16);
        JPasswordField confirm = new JPasswordField(16);
        form.add(new JLabel("Current password"));
        form.add(current);
        form.add(new JLabel("New password"));
        form.add(fresh);
        form.add(new JLabel("Confirm new"));
        form.add(confirm);

        int choice = javax.swing.JOptionPane.showConfirmDialog(
                this, form, "Change Password",
                javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);
        if (choice != javax.swing.JOptionPane.OK_OPTION) {
            return;
        }

        String cur = new String(current.getPassword());
        String nw = new String(fresh.getPassword());
        String cf = new String(confirm.getPassword());
        if (!nw.equals(cf)) {
            Ui.error(this, "The new passwords do not match.");
            return;
        }
        if (run(() -> ctx.authService().changePassword(ctx.getSession().getUser(), cur, nw))) {
            Ui.info(this, "Password changed.");
        }
    }

    private void logout() {
        dispose();
        // fresh context so the old session is gone
        LoginFrame login = new LoginFrame(new AppContext());
        login.setVisible(true);
    }

    protected JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 15f));
        label.setBorder(BorderFactory.createEmptyBorder(4, 2, 8, 2));
        return label;
    }
}
