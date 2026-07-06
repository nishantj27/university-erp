package edu.univ.erp.ui;

import edu.univ.erp.AppContext;
import edu.univ.erp.auth.AuthResult;
import edu.univ.erp.auth.Session;
import edu.univ.erp.data.DataException;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Font;

/**
 * The login screen. It only knows how to collect a username/password and hand them to the auth
 * service; it does not touch any database itself. On success it opens the dashboard that matches
 * the user's role.
 */
public class LoginFrame extends JFrame {

    private final AppContext ctx;
    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JLabel messageLabel = new JLabel(" ");

    public LoginFrame(AppContext ctx) {
        this.ctx = ctx;
        setTitle("University ERP - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(buildContent());
        getRootPane().setDefaultButton((JButton) null); // set below
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel(new MigLayout("wrap 2, insets 30 40 30 40", "[right][grow,fill]"));

        JLabel heading = new JLabel("University ERP");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        panel.add(heading, "span 2, center, gapbottom 4");

        JLabel subtitle = new JLabel("Please sign in to continue");
        subtitle.setForeground(Color.GRAY);
        panel.add(subtitle, "span 2, center, gapbottom 18");

        panel.add(new JLabel("Username"));
        panel.add(usernameField);

        panel.add(new JLabel("Password"));
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> attemptLogin());
        panel.add(loginButton, "span 2, growx, gaptop 14, height 34!");
        getRootPane().setDefaultButton(loginButton);   // Enter key logs in

        messageLabel.setForeground(new Color(0xB00020));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(messageLabel, "span 2, growx, gaptop 6");

        return panel;
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        try {
            AuthResult result = ctx.authService().login(username, password);
            if (!result.isOk()) {
                showError(result.getMessage());
                return;
            }
            // success: remember who is logged in, load their dashboard
            ctx.setSession(new Session(result.getUser()));
            openDashboard();
            dispose();
        } catch (DataException dbError) {
            showError("Can't reach the database. Is MySQL running and set up? (" + dbError.getMessage() + ")");
        }
    }

    private void openDashboard() {
        JFrame dashboard;
        switch (ctx.getSession().getRole()) {
            case ADMIN:
                dashboard = new AdminDashboard(ctx);
                break;
            case INSTRUCTOR:
                dashboard = new InstructorDashboard(ctx);
                break;
            default:
                dashboard = new StudentDashboard(ctx);
                break;
        }
        dashboard.setVisible(true);
    }

    private void showError(String message) {
        messageLabel.setText(message);
        passwordField.setText("");
    }
}
