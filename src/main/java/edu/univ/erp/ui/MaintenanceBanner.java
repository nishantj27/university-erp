package edu.univ.erp.ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Font;

/**
 * The yellow strip shown at the top of the student/instructor dashboards when maintenance mode is
 * on. It only affects visibility - the actual blocking of changes happens in the service layer.
 */
public class MaintenanceBanner extends JLabel {

    public MaintenanceBanner() {
        super("  Maintenance mode is ON - you can view your data but changes are disabled.  ",
                SwingConstants.CENTER);
        setOpaque(true);
        setBackground(new Color(0xFFF3CD));   // soft amber
        setForeground(new Color(0x664D03));
        setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        setFont(getFont().deriveFont(Font.BOLD));
        setVisible(false);
    }

    /** Show or hide the banner based on the current maintenance flag. */
    public void refresh(boolean maintenanceOn) {
        setVisible(maintenanceOn);
    }
}
