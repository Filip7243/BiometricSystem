package com.neurotec.samples;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.IOException;

public final class MainPanel extends JPanel implements ChangeListener {

    // ===========================================================
    // Private static fields
    // ===========================================================

    private static final long serialVersionUID = 1L;

    // ===========================================================
    // Private fields
    // ===========================================================

    private JTabbedPane tabbedPane;
    private EnrollFromScanner enrollFromScanner;
    private AddEmployee addEmployee;
    // ===========================================================
    // Public constructor
    // ===========================================================

    public MainPanel() {
        super(new GridLayout(1, 1));
        initGUI();
    }

    // ===========================================================
    // Private methods
    // ===========================================================

    private void initGUI() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(this);

        addEmployee = new AddEmployee();
        addEmployee.init();
        tabbedPane.addTab("Add Employee", addEmployee);

        enrollFromScanner = new EnrollFromScanner();
        enrollFromScanner.init();
        tabbedPane.addTab("Enter to room", enrollFromScanner);

        add(tabbedPane);
        setPreferredSize(new Dimension(680, 600));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    // ===========================================================
    // Public methods
    // ===========================================================

    public void obtainLicenses(BasePanel panel) throws IOException {
        if (!panel.isObtained()) {
            boolean status = FingersTools.getInstance().obtainLicenses(panel.getRequiredLicenses());
            FingersTools.getInstance().obtainLicenses(panel.getOptionalLicenses());
            panel.getLicensingPanel().setRequiredComponents(panel.getRequiredLicenses());
            panel.getLicensingPanel().setOptionalComponents(panel.getOptionalLicenses());
            panel.updateLicensing(status);
        }
    }

    // ===========================================================
    // Event handling
    // ===========================================================

    @Override
    public void stateChanged(ChangeEvent evt) {
        if (evt.getSource() == tabbedPane) {
            try {
                switch (tabbedPane.getSelectedIndex()) {
                    case 0: {
                        obtainLicenses(addEmployee);
                        addEmployee.updateFingersTools();
                        addEmployee.updateScannerList();
                        addEmployee.updateRoomList();
                        break;
                    }
                    case 1: {
                        obtainLicenses(enrollFromScanner);
                        enrollFromScanner.updateFingersTools();
                        enrollFromScanner.updateScannerList();
                        break;
                    }
                    default: {
                        throw new IndexOutOfBoundsException("unreachable");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Could not obtain licenses for components: " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }
    }

}
