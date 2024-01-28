package com.neurotec.samples;

import com.neurotec.biometrics.*;
import com.neurotec.biometrics.swing.NFingerView;
import com.neurotec.biometrics.swing.NFingerViewBase.ShownImage;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.devices.NFingerScanner;
import com.neurotec.images.NImages;
import com.neurotec.io.NBuffer;
import com.neurotec.samples.util.Utils;
import com.neurotec.swing.NViewZoomSlider;
import com.neurotec.util.concurrent.CompletionHandler;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.*;

public final class EnterToRoom extends BasePanel implements ActionListener {

    // ===========================================================
    // Private static fields
    // ===========================================================

    private static final long serialVersionUID = 1L;

    // ===========================================================
    // Private fields
    // ===========================================================

    private NSubject subject;
    private final NDeviceManager deviceManager;
    private boolean scanning;

    private final CaptureCompletionHandler captureCompletionHandler = new CaptureCompletionHandler();

    private NFingerView view;
    private JFileChooser fcImage;
    private JFileChooser fcTemplate;
    private File oldImageFile;
    private File oldTemplateFile;

    private JButton btnCancel;
    private JButton btnForce;
    private JButton btnRefresh;
    private JButton btnScan;
    private JCheckBox cbAutomatic;
    private JCheckBox cbShowBinarized;
    private JLabel lblInfo;
    private JPanel panelButtons;
    private JPanel panelInfo;
    private JPanel panelMain;
    private JPanel panelSave;
    private JPanel panelScanners;
    private JPanel panelSouth;
    private JList<NDevice> scannerList;
    private JScrollPane scrollPane;
    private JScrollPane scrollPaneList;

    // My Code
    private final Set<String> roomsSelected = new HashSet<>();
    private JPanel panelRooms;
    private JScrollPane scrollPaneRoomList;
    private JList<String> roomList;
    private JLabel fingerInfoLabel;
    private String fingerToScan;
    private List<NSubject> subjects;
    private static int employeeId = -1;
    private final EnrollHandler enrollHandler = new EnrollHandler();
    private final IdentificationHandler identificationHandler = new IdentificationHandler();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/biometrics";
    private String chosenRoom;

    // ===========================================================
    // Public constructor
    // ===========================================================

    public EnterToRoom() {
        super();
        subjects = new ArrayList<>();
        requiredLicenses = new ArrayList<>();
        requiredLicenses.add("Biometrics.FingerExtraction");
        requiredLicenses.add("Devices.FingerScanners");
        requiredLicenses.add("Biometrics.FingerMatching");
        optionalLicenses = new ArrayList<>();
        optionalLicenses.add("Images.WSQ");

        setFingerToScan();

        FingersTools.getInstance().getClient().setUseDeviceManager(true);
        deviceManager = FingersTools.getInstance().getClient().getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.FINGER_SCANNER));
        deviceManager.initialize();
    }

    // ===========================================================
    // Private methods
    // ===========================================================

    private void startCapturing() {
        lblInfo.setText("");

        if (FingersTools.getInstance().getClient().getFingerScanner() == null || roomList.getSelectedValue() == null) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Please select scanner from the list.", "No scanner selected", JOptionPane.PLAIN_MESSAGE);
            });
            return;
        }

        // Create a finger.
        NFinger finger = new NFinger();

        // Set Manual capturing mode if automatic isn't selected.
        if (!cbAutomatic.isSelected()) {
            finger.setCaptureOptions(EnumSet.of(NBiometricCaptureOption.MANUAL));
        }

        // Add finger to subject and finger view.
        subject = new NSubject();
        subject.getFingers().add(finger);
        view.setFinger(finger);
        view.setShownImage(ShownImage.ORIGINAL);

        // Begin capturing.
        NBiometricTask task = FingersTools.getInstance().getClient().createTask(EnumSet.of(NBiometricOperation.CAPTURE, NBiometricOperation.CREATE_TEMPLATE), subject);
        FingersTools.getInstance().getClient().performTask(task, null, captureCompletionHandler);
        scanning = true;
        updateControls();
    }

    private void processDB() throws IOException {
        if (subject == null) {  // test code
            System.out.println("SUBJECT IS NULL!");

            subject = new NSubject();

            String imgPath = null;

            switch (fingerToScan.toLowerCase()) {
                case "thumb":
                    imgPath = "C:\\Users\\Filip\\Desktop\\odciski\\PL_THUMB.jpg";
                    break;
                case "pointing":
                    imgPath = "C:\\Users\\Filip\\Desktop\\odciski\\PL_POINTING.jpg";
                    break;
                case "middle":
                    imgPath = "C:\\Users\\Filip\\Desktop\\odciski\\PL_MIDDLE.jpg";
                    break;
                case "ring":
                    imgPath = "C:\\Users\\Filip\\Desktop\\odciski\\PL_RING.jpg";
                    break;
            }

            NFinger finger = new NFinger();

            finger.setFileName(imgPath);

            subject.getFingers().add(finger);
        }

        loadTemplates();
    }

    private void loadTemplates() {
        subjects.clear();
        Map<Integer, byte[]> fingers = getSpecificFingers();

        NBiometricTask enrollmentTask = new NBiometricTask(EnumSet.of(NBiometricOperation.ENROLL));

        for (Map.Entry<Integer, byte[]> entry : fingers.entrySet()) {
            Integer employeeId = entry.getKey();
            byte[] fingersData = entry.getValue();

            NBuffer buffer = new NBuffer(fingersData);
            NSubject subject = NSubject.fromMemory(buffer);
            subject.setId(String.valueOf(employeeId));
            subjects.add(subject);
            enrollmentTask.getSubjects().add(subject);
        }

        FingersTools.getInstance().getClient().performTask(enrollmentTask, null, enrollHandler);
    }

    // biore wszystkie fingery, ktore sa fingerToScan
    // sprawdzam score
    // biore najwyzszy
    // sprawdzam ID usera, ktory ma najwyzszy score,
    // sprwadzam czy ma dostep do danego pokoju
    // jesli tak - wyswietlam sukces!
    // jesli nie - nie masz dostepu do tego pokoju
    private Map<Integer, byte[]> getSpecificFingers() {
        Map<Integer, byte[]> result = new HashMap<>();

        final String query = String.format("SELECT employee_id, %s FROM finger", fingerToScan);
        try (Connection connection = DriverManager.getConnection(DB_URL, "root", "")) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet rs = preparedStatement.executeQuery();

                while (rs.next()) {
                    int employeeId = rs.getInt("employee_id");
                    byte[] template = rs.getBytes(fingerToScan);

                    result.put(employeeId, template);
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return result;
    }

    private List<Integer> getUserRooms() {
        List<Integer> userRooms = new ArrayList<>();

        if (chosenRoom == null) {
            System.out.println("CHOSEN ROOM IS NULL");
            return null;
        }

        if (employeeId == -1) {
            System.out.println("EMPLOYYE ID IS NULL");
            return null;
        }

        final String query = "SELECT room_id FROM employee_room WHERE employee_id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, "root", "")) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, employeeId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int roomId = resultSet.getInt("room_id");
                        userRooms.add(roomId);
                    }
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return userRooms;
    }

    private int getSelectedRoomId() {
        // get selected room
        final String query = "SELECT id FROM room WHERE name = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, "root", "")) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, chosenRoom);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt("id");
                    } else {
                        return -1;
                    }
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return -1;
    }

    private void updateShownImage() {
        if (cbShowBinarized.isSelected()) {
            view.setShownImage(ShownImage.RESULT);
        } else {
            view.setShownImage(ShownImage.ORIGINAL);
        }
    }

    private void setFingerToScan() {
        Random r = new Random();

        int finger = r.nextInt(4) + 1;

        switch (finger) {
            case 1:
                fingerToScan = "thumb";
                break;
            case 2:
                fingerToScan = "pointing";
                break;
            case 3:
                fingerToScan = "middle";
                break;
            case 4:
                fingerToScan = "ring";
                break;
        }
    }

    // ===========================================================
    // Package private methods
    // ===========================================================

    void updateStatus(String status) {
        lblInfo.setText(status);
    }

    NSubject getSubject() {
        return subject;
    }

    List<NSubject> getSubjects() {
        return subjects;
    }

    NFingerScanner getSelectedScanner() {
        return (NFingerScanner) scannerList.getSelectedValue();
    }

    // ===========================================================
    // Protected methods
    // ===========================================================

    @Override
    protected void initGUI() {
        setLayout(new BorderLayout());

        panelLicensing = new LicensingPanel(requiredLicenses, optionalLicenses);
        add(panelLicensing, BorderLayout.NORTH);

        panelMain = new JPanel();
        panelMain.setLayout(new BorderLayout());
        add(panelMain, BorderLayout.CENTER);
        {
            panelScanners = new JPanel();
            panelScanners.setBorder(BorderFactory.createTitledBorder("Scanners list"));
            panelScanners.setLayout(new BorderLayout());
            panelMain.add(panelScanners, BorderLayout.NORTH);
            {
                scrollPaneList = new JScrollPane();
                scrollPaneList.setPreferredSize(new Dimension(0, 90));
                panelScanners.add(scrollPaneList, BorderLayout.NORTH);
                {
                    scannerList = new JList<>();
                    scannerList.setModel(new DefaultListModel<>());
                    scannerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    scannerList.setBorder(LineBorder.createBlackLineBorder());
                    scannerList.addListSelectionListener(new ScannerSelectionListener());
                    scrollPaneList.setViewportView(scannerList);

                }
            }
            {
                panelButtons = new JPanel();
                panelButtons.setLayout(new FlowLayout(FlowLayout.LEADING));
                panelScanners.add(panelButtons, BorderLayout.SOUTH);
                {
                    btnRefresh = new JButton();
                    btnRefresh.setText("Refresh list");
                    btnRefresh.addActionListener(this);
                    panelButtons.add(btnRefresh);
                }
                {
                    btnScan = new JButton();
                    btnScan.setText("Scan");
                    btnScan.addActionListener(this);
                    panelButtons.add(btnScan);
                }
                {
                    btnCancel = new JButton();
                    btnCancel.setText("Cancel");
                    btnCancel.setEnabled(false);
                    btnCancel.addActionListener(this);
                    panelButtons.add(btnCancel);
                }
                {
                    btnForce = new JButton();
                    btnForce.setText("Force");
                    btnForce.addActionListener(this);
                    panelButtons.add(btnForce);
                }
                {
                    cbAutomatic = new JCheckBox();
                    cbAutomatic.setSelected(true);
                    cbAutomatic.setText("Scan automatically");
                    panelButtons.add(cbAutomatic);
                }
                {
                    fingerInfoLabel = new JLabel();
                    fingerInfoLabel.setText(String.format("Scan: %s finger", fingerToScan));
                    fingerInfoLabel.setForeground(Color.RED);
                    panelButtons.add(fingerInfoLabel);
                }
            }
        }
        {
            panelRooms = new JPanel();
            panelRooms.setLayout(new BorderLayout());
            panelRooms.setBorder(BorderFactory.createTitledBorder("Rooms list"));
            panelMain.add(panelRooms, BorderLayout.CENTER);
            {
                scrollPaneRoomList = new JScrollPane();
                scrollPaneList.setPreferredSize(new Dimension(0, 90));
                panelRooms.add(scrollPaneRoomList, BorderLayout.NORTH);
                {
                    roomList = new JList<>();
                    roomList.setModel(new DefaultListModel<>());
                    roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    roomList.setBorder(LineBorder.createBlackLineBorder());
                    roomList.addListSelectionListener(new RoomSelectionListener());
                    scrollPaneRoomList.setViewportView(roomList);
                }
            }
            {
                scrollPane = new JScrollPane();
                panelRooms.add(scrollPane, BorderLayout.SOUTH);
                {
                    view = new NFingerView();
                    view.setShownImage(ShownImage.RESULT);
                    view.setAutofit(true);
                    view.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent ev) {
                            super.mouseClicked(ev);
                            if (ev.getButton() == MouseEvent.BUTTON3) {
                                cbShowBinarized.doClick();
                            }
                        }

                    });
                    scrollPane.setViewportView(view);
                }
            }
        }
        {
            panelSouth = new JPanel();
            panelSouth.setLayout(new BorderLayout());
            panelMain.add(panelSouth, BorderLayout.SOUTH);
            {
                panelInfo = new JPanel();
                panelInfo.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
                panelInfo.setLayout(new GridLayout(1, 1));
                panelSouth.add(panelInfo, BorderLayout.NORTH);
                {
                    lblInfo = new JLabel();
                    lblInfo.setText(" ");
                    panelInfo.add(lblInfo);
                }
            }
            {
                panelSave = new JPanel();
                panelSave.setLayout(new FlowLayout(FlowLayout.LEADING));
                panelSouth.add(panelSave, BorderLayout.WEST);
                {

                }
                {
                    cbShowBinarized = new JCheckBox();
                    cbShowBinarized.setSelected(true);
                    cbShowBinarized.setText("Show binarized image");
                    cbShowBinarized.addActionListener(this);
                    panelSave.add(cbShowBinarized);

                }
            }
            {
                NViewZoomSlider zoomSlider = new NViewZoomSlider();
                zoomSlider.setView(view);
                panelSouth.add(zoomSlider, BorderLayout.EAST);
            }
        }

        fcImage = new JFileChooser();
        fcImage.setFileFilter(new Utils.ImageFileFilter(NImages.getSaveFileFilter()));
        fcTemplate = new JFileChooser();
    }

    @Override
    protected void setDefaultValues() {
        // No default values.
    }

    @Override
    protected void updateControls() {
        btnScan.setEnabled(!scanning);
        btnCancel.setEnabled(scanning);
        btnForce.setEnabled(scanning);
        btnRefresh.setEnabled(!scanning);
        cbShowBinarized.setEnabled(!scanning);
        cbAutomatic.setEnabled(!scanning);
        setFingerToScan();
    }

    @Override
    protected void updateFingersTools() {
        FingersTools.getInstance().getClient().reset();
        FingersTools.getInstance().getClient().setUseDeviceManager(true);
        FingersTools.getInstance().getClient().setFingersReturnBinarizedImage(true);
    }

    // ===========================================================
    // Public methods
    // ===========================================================

    public void updateScannerList() {
        DefaultListModel<NDevice> model = (DefaultListModel<NDevice>) scannerList.getModel();
        model.clear();
        for (NDevice device : deviceManager.getDevices()) {
            model.addElement(device);
        }
        NFingerScanner scanner = (NFingerScanner) FingersTools.getInstance().getClient().getFingerScanner();
        if ((scanner == null) && (model.getSize() > 0)) {
            scannerList.setSelectedIndex(0);
        } else if (scanner != null) {
            scannerList.setSelectedValue(scanner, true);
        }
    }

    public void updateRoomList() {
        DefaultListModel<String> model = (DefaultListModel<String>) roomList.getModel();
        model.clear();

        final String query = "SELECT name FROM Room";
        try (Connection connection = DriverManager.getConnection(DB_URL, "root", "")) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    String name = rs.getString(1);
                    model.addElement(name);
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void cancelCapturing() {
        FingersTools.getInstance().getClient().cancel();
    }

    // ===========================================================
    // Event handling
    // ===========================================================

    @Override
    public void actionPerformed(ActionEvent ev) {
        try {
            if (ev.getSource() == btnRefresh) {
                updateScannerList();
                updateRoomList();
                // TODO: to ja juz bedzie sakner, to sie przeniesie do startCaputer(), jak nie bedzie dzialac to sie przycisk da
                processDB();

            } else if (ev.getSource() == btnScan) {
                startCapturing();
            } else if (ev.getSource() == btnCancel) {
                cancelCapturing();
            } else if (ev.getSource() == btnForce) {
                FingersTools.getInstance().getClient().force();
            } else if (ev.getSource() == cbShowBinarized) {
                updateShownImage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    // ===========================================================
    // Inner classes
    // ===========================================================


    private class CaptureCompletionHandler implements CompletionHandler<NBiometricTask, Object> {

        @Override
        public void completed(final NBiometricTask result, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                scanning = false;
                updateShownImage();
                if (result.getStatus() == NBiometricStatus.OK) {
                    updateStatus("Quality: " + getSubject().getFingers().get(0).getObjects().get(0).getQuality());
                } else {
                    updateStatus(result.getStatus().toString());
                }
                updateControls();
            });
        }

        @Override
        public void failed(final Throwable th, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                scanning = false;
                updateShownImage();
                showError(th);
                updateControls();
            });
        }

    }

    private class ScannerSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            FingersTools.getInstance().getClient().setFingerScanner(getSelectedScanner());
        }
    }

    private class RoomSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            roomList.getSelectedValuesList().forEach(room -> chosenRoom = room);
            roomsSelected.clear();
            roomsSelected.addAll(roomList.getSelectedValuesList());
        }
    }

    private class EnrollHandler implements CompletionHandler<NBiometricTask, Object> {

        @Override
        public void completed(final NBiometricTask task, final Object attachment) {
            if (task.getStatus() == NBiometricStatus.OK) {

                // Identify current subject in enrolled ones.
                FingersTools.getInstance().getClient().identify(getSubject(), null, identificationHandler);
            } else {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(EnterToRoom.this, "Enrollment failed: " + task.getStatus(), "Error", JOptionPane.WARNING_MESSAGE);
                });
            }
        }

        @Override
        public void failed(final Throwable th, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                updateControls();
                showError(th);
            });
        }
    }

    private class IdentificationHandler implements CompletionHandler<NBiometricStatus, Object> {

        @Override
        public void completed(final NBiometricStatus status, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                if ((status == NBiometricStatus.OK) || (status == NBiometricStatus.MATCH_NOT_FOUND)) {
                    int maxScore = Integer.MIN_VALUE;
                    // Match subjects.
                    for (NSubject s : getSubjects()) {
                        boolean match = false;
                        for (NMatchingResult result : getSubject().getMatchingResults()) {
                            if (s.getId().equals(result.getId())) {
                                match = true;
                                if (maxScore < result.getScore()) {
                                    maxScore = result.getScore();
                                    employeeId = Integer.parseInt(s.getId());
                                }
                                System.out.println("maxScore = " + maxScore + " emplyee = " + employeeId);
                                break;
                            }
                        }
                        if (!match) {
                            System.out.println("NOT MACHES");
                        }
                    }
                    System.out.println("MAX SCORE = " + maxScore);
                    checkIfUserCanEnter(maxScore, employeeId);
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(EnterToRoom.this, "Identification failed: " + status, "Error", JOptionPane.WARNING_MESSAGE);
                    });
                }
            });
        }

        private void checkIfUserCanEnter(int maxScore, int employeeId) {
            if (employeeId != -1) {
                List<Integer> userRooms = getUserRooms();
                int roomId = getSelectedRoomId();
                if (userRooms.contains(roomId) && maxScore > 0) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(EnterToRoom.this,
                            "Door opened!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE));
                } else {
                    System.out.println("ESSUNIA");
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(EnterToRoom.this,
                            "You are not able to enter this room!",
                            "Failed",
                            JOptionPane.INFORMATION_MESSAGE));
                }
            } else {
                System.out.println("ESUNIA2");
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(EnterToRoom.this,
                        "You are not able to enter this room!",
                        "Failed",
                        JOptionPane.INFORMATION_MESSAGE));
            }
        }

        @Override
        public void failed(final Throwable th, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                updateControls();
                showError(th);
            });
        }

    }
}
