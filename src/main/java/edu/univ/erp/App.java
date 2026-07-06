package edu.univ.erp;

import com.formdev.flatlaf.FlatLightLaf;
import edu.univ.erp.ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point. Sets up the look-and-feel and shows the login window.
 * Everything the screens do goes through the service layer, never straight to the DB.
 */
public class App {

    private static final Logger log = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        // modern flat look instead of the default metal theme
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            log.log(Level.WARNING, "could not load FlatLaf, falling back to default L&F", e);
        }

        SwingUtilities.invokeLater(() -> new LoginFrame(new AppContext()).setVisible(true));
    }
}
