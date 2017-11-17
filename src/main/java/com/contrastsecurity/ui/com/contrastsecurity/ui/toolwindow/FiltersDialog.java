package com.contrastsecurity.ui.com.contrastsecurity.ui.toolwindow;

import com.contrastsecurity.config.ContrastFilterPersistentStateComponent;
import com.contrastsecurity.core.Constants;
import com.contrastsecurity.core.Util;
import com.contrastsecurity.http.RuleSeverity;
import com.contrastsecurity.http.TraceFilterForm;
import com.contrastsecurity.models.Application;
import com.contrastsecurity.models.Server;
import com.contrastsecurity.models.Servers;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.optionalusertools.DateTimeChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateTimeChangeEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

public class FiltersDialog extends JDialog {
    private static final int PAGE_LIMIT = 20;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox serversComboBox;
    private JComboBox applicationsComboBox;
    private JCheckBox severityLevelNoteCheckBox;
    private JCheckBox severityLevelMediumCheckBox;
    private JCheckBox severityLevelLowCheckBox;
    private JCheckBox severityLevelHighCheckBox;
    private JCheckBox severityLevelCriticalCheckBox;
    private JCheckBox statusAutoRemediatedCheckBox;
    private JCheckBox statusConfirmedCheckBox;
    private JCheckBox statusSuspiciousCheckBox;
    private JCheckBox statusNotAProblemCheckBox;
    private JCheckBox statusRemediatedCheckBox;
    private JCheckBox statusReportedCheckBox;
    private JCheckBox statusFixedCheckBox;
    private JCheckBox statusBeingTrackedCheckBox;
    private JCheckBox statusUntrackedCheckBox;
    private DateTimePicker lastDetectedFromDateTimePicker;
    private DateTimePicker lastDetectedToDateTimePicker;
    private JComboBox lastDetectedComboBox;
    private JPanel filtersPanel;
    private ContrastFilterPersistentStateComponent contrastFilterPersistentStateComponent;
    //    private ContrastUtil contrastUtil;
    private Servers servers;
    private List<Application> applications;
    private int currentOffset = 0;

    private TraceFilterForm traceFilterForm;

    public FiltersDialog(Servers servers, List<Application> applications) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setSize(850, 400);

        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final int x = (screenSize.width - getWidth()) / 2;
        final int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
        setTitle("Set Filters");

//        Filters related initialization
        this.servers = servers;
        this.applications = applications;

        contrastFilterPersistentStateComponent = ContrastFilterPersistentStateComponent.getInstance();

        setupCheckBoxes();
        setupComboBoxes();

        lastDetectedFromDateTimePicker.addDateTimeChangeListener(new DateTimeChangeListener() {
            @Override
            public void dateOrTimeChanged(DateTimeChangeEvent event) {
                if (lastDetectedFromDateTimePicker.getDateTimePermissive() != null && lastDetectedToDateTimePicker.getDateTimePermissive() != null) {

                    if (!isFromDateLessThanToDate(lastDetectedFromDateTimePicker.getDateTimePermissive(), lastDetectedToDateTimePicker.getDateTimePermissive())) {
                        lastDetectedToDateTimePicker.clear();
                    }
                }
            }
        });

        lastDetectedToDateTimePicker.addDateTimeChangeListener(new DateTimeChangeListener() {
            @Override
            public void dateOrTimeChanged(DateTimeChangeEvent event) {
                if (lastDetectedFromDateTimePicker.getDateTimePermissive() != null && lastDetectedToDateTimePicker.getDateTimePermissive() != null) {

                    if (!isFromDateLessThanToDate(lastDetectedFromDateTimePicker.getDateTimePermissive(), lastDetectedToDateTimePicker.getDateTimePermissive())) {
                        lastDetectedFromDateTimePicker.clear();
                    }
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        }).start();
    }

    public FiltersDialog() {

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setSize(700, 400);
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final int x = (screenSize.width - getWidth()) / 2;
        final int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
        setTitle("Set Filters");
    }

    public static void main(String[] args) {
        FiltersDialog dialog = new FiltersDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void onOK() {
        this.traceFilterForm = extractFiltersIntoTraceFilterForm();
        saveFilters();
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private void populateFiltersWithDataFromContrastFilterPersistentStateComponent() {
        if (contrastFilterPersistentStateComponent.getSelectedServerUuid() != null) {
            selectServerByUuid(contrastFilterPersistentStateComponent.getSelectedServerUuid());
        }

        if (!contrastFilterPersistentStateComponent.getSelectedApplicationName().equals("")) {
            selectApplicationByName(contrastFilterPersistentStateComponent.getSelectedApplicationName());
        }
        if (contrastFilterPersistentStateComponent.getSeverities() != null && !contrastFilterPersistentStateComponent.getSeverities().isEmpty()) {
            selectSeveritiesFromList(contrastFilterPersistentStateComponent.getSeverities());
        }
        lastDetectedComboBox.setSelectedItem(contrastFilterPersistentStateComponent.getLastDetected());
        if (contrastFilterPersistentStateComponent.getLastDetectedFrom() != null) {
            LocalDateTime localDateTimeFrom = getLocalDateTimeFromMillis(contrastFilterPersistentStateComponent.getLastDetectedFrom());
            lastDetectedFromDateTimePicker.setDateTimePermissive(localDateTimeFrom);
        }

        if (contrastFilterPersistentStateComponent.getLastDetectedTo() != null) {
            LocalDateTime localDateTimeTo = getLocalDateTimeFromMillis(contrastFilterPersistentStateComponent.getLastDetectedTo());
            lastDetectedToDateTimePicker.setDateTimePermissive(localDateTimeTo);
        }
        if (contrastFilterPersistentStateComponent.getStatuses() != null && !contrastFilterPersistentStateComponent.getStatuses().isEmpty()) {
            selectStatusesFromList(contrastFilterPersistentStateComponent.getStatuses());
        }
    }

    private void selectServerByUuid(Long serverUuid) {
        int itemCount = serversComboBox.getItemCount();
        if (itemCount > 0) {
            for (int i = 0; i < itemCount; i++) {
                ServerComboBoxItem serverComboBoxItem = (ServerComboBoxItem) serversComboBox.getItemAt(i);
                if (serverComboBoxItem.getServer() != null && serverComboBoxItem.getServer().getServerId() == serverUuid) {
                    serversComboBox.setSelectedItem(serverComboBoxItem);
                    break;
                }
            }
        }
    }

    private void selectApplicationByName(String applicationName) {
        int itemCount = applicationsComboBox.getItemCount();
        if (itemCount > 0) {
            for (int i = 0; i < itemCount; i++) {
                ApplicationComboBoxItem applicationComboBoxItem = (ApplicationComboBoxItem) applicationsComboBox.getItemAt(i);
                if (applicationComboBoxItem.toString().equals(applicationName)) {
                    applicationsComboBox.setSelectedItem(applicationComboBoxItem);
                    break;
                }
            }
        }
    }

    private void selectSeveritiesFromList(List<String> severities) {
        if (!severities.isEmpty()) {
            for (String severity : severities) {
                if (severity.equals(RuleSeverity.NOTE.toString())) {
                    severityLevelNoteCheckBox.setSelected(true);
                } else if (severity.equals(RuleSeverity.LOW.toString())) {
                    severityLevelLowCheckBox.setSelected(true);
                } else if (severity.equals(RuleSeverity.MEDIUM.toString())) {
                    severityLevelMediumCheckBox.setSelected(true);
                } else if (severity.equals(RuleSeverity.HIGH.toString())) {
                    severityLevelHighCheckBox.setSelected(true);
                } else if (severity.equals(RuleSeverity.CRITICAL.toString())) {
                    severityLevelCriticalCheckBox.setSelected(true);
                }
            }
        }
    }

    private LocalDateTime getLocalDateTimeFromMillis(Long millis) {
        Date date = new Date(millis);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return localDateTime;
    }

    private void selectStatusesFromList(List<String> statuses) {

        for (String status : statuses) {
            if (status.equals(Constants.VULNERABILITY_STATUS_AUTO_REMEDIATED)) {
                statusAutoRemediatedCheckBox.setSelected(true);
            }
            if (status.equals(Constants.VULNERABILITY_STATUS_CONFIRMED)) {
                statusConfirmedCheckBox.setSelected(true);
            }
            if (status.equals(Constants.VULNERABILITY_STATUS_SUSPICIOUS)) {
                statusSuspiciousCheckBox.setSelected(true);
            }
            if (status.equals(Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM)) {
                statusNotAProblemCheckBox.setSelected(true);
            }
            if (status.equals(Constants.VULNERABILITY_STATUS_REMEDIATED)) {
                statusRemediatedCheckBox.setSelected(true);
            }
            if (status.equals(Constants.VULNERABILITY_STATUS_REPORTED)) {
                statusReportedCheckBox.setSelected(true);
            }
            if (status.equals(Constants.VULNERABILITY_STATUS_FIXED)) {
                statusFixedCheckBox.setSelected(true);
            }
            if (status.equals(Constants.VULNERABILITY_STATUS_BEING_TRACKED)) {
                statusBeingTrackedCheckBox.setSelected(true);
            }
            if (status.equals(Constants.VULNERABILITY_STATUS_UNTRACKED)) {
                statusUntrackedCheckBox.setSelected(true);
            }
        }
    }

    public void refresh() {
//        contrastUtil = new ContrastUtil();
        updateServersComboBox(servers);
        updateLastDetectedComboBox();
        populateFiltersWithDataFromContrastFilterPersistentStateComponent();
    }

    private void updateServersComboBox(Servers servers) {
        serversComboBox.removeAllItems();
        int count = 0;
        if (servers != null && servers.getServers() != null && !servers.getServers().isEmpty()) {
            for (Server server : servers.getServers()) {
                ServerComboBoxItem contrastServer = new ServerComboBoxItem(server);
                serversComboBox.addItem(contrastServer);
                count++;
            }
            ServerComboBoxItem allServers = new ServerComboBoxItem("All Servers(" + count + ")");
            serversComboBox.addItem(allServers);
            serversComboBox.setSelectedItem(allServers);
        }
    }

    private void updateLastDetectedComboBox() {
        lastDetectedComboBox.removeAllItems();
        lastDetectedComboBox.addItem(Constants.LAST_DETECTED_ALL);
        lastDetectedComboBox.addItem(Constants.LAST_DETECTED_HOUR);
        lastDetectedComboBox.addItem(Constants.LAST_DETECTED_DAY);
        lastDetectedComboBox.addItem(Constants.LAST_DETECTED_WEEK);
        lastDetectedComboBox.addItem(Constants.LAST_DETECTED_MONTH);
        lastDetectedComboBox.addItem(Constants.LAST_DETECTED_YEAR);
        lastDetectedComboBox.addItem(Constants.LAST_DETECTED_CUSTOM);
    }

    private void setupCheckBoxes() {

        if (contrastFilterPersistentStateComponent.getSeverities() == null || contrastFilterPersistentStateComponent.getSeverities().isEmpty()) {
            severityLevelNoteCheckBox.setSelected(true);
            severityLevelLowCheckBox.setSelected(true);
            severityLevelMediumCheckBox.setSelected(true);
            severityLevelHighCheckBox.setSelected(true);
            severityLevelCriticalCheckBox.setSelected(true);
        }

        if (contrastFilterPersistentStateComponent.getStatuses() == null || contrastFilterPersistentStateComponent.getStatuses().isEmpty()) {

            statusAutoRemediatedCheckBox.setSelected(true);
            statusConfirmedCheckBox.setSelected(true);
            statusSuspiciousCheckBox.setSelected(true);
            statusNotAProblemCheckBox.setSelected(true);
            statusRemediatedCheckBox.setSelected(true);
            statusReportedCheckBox.setSelected(true);
            statusFixedCheckBox.setSelected(true);
            statusBeingTrackedCheckBox.setSelected(true);
            statusUntrackedCheckBox.setSelected(true);

        }
    }

    private void setupComboBoxes() {

        serversComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.SELECTED) {
                    ServerComboBoxItem serverComboBoxItem = (ServerComboBoxItem) e.getItem();
                    updateApplicationsComboBox(serverComboBoxItem.getServer());
                }
            }
        });

        lastDetectedComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.SELECTED) {
                    lastDetectedToDateTimePicker.clear();
                    LocalDateTime localDateTime = LocalDateTime.now();

                    if (!e.getItem().toString().equals((Constants.LAST_DETECTED_CUSTOM))) {
                        lastDetectedFromDateTimePicker.setEnabled(false);
                        lastDetectedToDateTimePicker.setEnabled(false);
                    }

                    if (e.getItem().toString().equals(Constants.LAST_DETECTED_ALL)) {
                        lastDetectedFromDateTimePicker.clear();

                    } else if (e.getItem().toString().equals(Constants.LAST_DETECTED_HOUR)) {

                        lastDetectedFromDateTimePicker.setDateTimeStrict(localDateTime.minusHours(1));

                    } else if (e.getItem().toString().equals(Constants.LAST_DETECTED_DAY)) {

                        lastDetectedFromDateTimePicker.setDateTimeStrict(localDateTime.minusDays(1));

                    } else if (e.getItem().toString().equals(Constants.LAST_DETECTED_WEEK)) {

                        lastDetectedFromDateTimePicker.setDateTimeStrict(localDateTime.minusWeeks(1));

                    } else if (e.getItem().toString().equals(Constants.LAST_DETECTED_MONTH)) {

                        lastDetectedFromDateTimePicker.setDateTimeStrict(localDateTime.minusMonths(1));

                    } else if (e.getItem().toString().equals(Constants.LAST_DETECTED_YEAR)) {

                        lastDetectedFromDateTimePicker.setDateTimeStrict(localDateTime.minusYears(1));

                    } else if (e.getItem().toString().equals(Constants.LAST_DETECTED_CUSTOM)) {
                        lastDetectedFromDateTimePicker.setEnabled(true);
                        lastDetectedToDateTimePicker.setEnabled(true);
                    }
                }
            }
        });
    }

    private boolean isFromDateLessThanToDate(LocalDateTime fromDate, LocalDateTime toDate) {
        Date lastDetectedFromDate = Date.from(fromDate.atZone(ZoneId.systemDefault()).toInstant());
        Date lastDetectedToDate = Date.from(toDate.atZone(ZoneId.systemDefault()).toInstant());

        if (lastDetectedFromDate.getTime() < lastDetectedToDate.getTime()) {
            return true;
        } else {
            return false;
        }
    }

    public void updateApplicationsComboBox(Server server) {
        applicationsComboBox.removeAllItems();
        int count = 0;
        List<Application> applications = null;

        if (server == null) {
            applications = this.applications;
        } else {
            applications = server.getApplications();
        }

        if (applications != null && !applications.isEmpty()) {
            for (Application application : applications) {
                ApplicationComboBoxItem app = new ApplicationComboBoxItem(application);
                applicationsComboBox.addItem(app);
                count++;
            }
            ApplicationComboBoxItem allApplications = new ApplicationComboBoxItem("All Applications(" + count + ")");
            applicationsComboBox.addItem(allApplications);
            applicationsComboBox.setSelectedItem(allApplications);
        }
    }

    private EnumSet<RuleSeverity> getSelectedSeverities() {
        EnumSet<RuleSeverity> severities = EnumSet.noneOf(RuleSeverity.class);
        if (severityLevelNoteCheckBox.isSelected()) {
            severities.add(RuleSeverity.NOTE);
        }
        if (severityLevelLowCheckBox.isSelected()) {
            severities.add(RuleSeverity.LOW);
        }
        if (severityLevelMediumCheckBox.isSelected()) {
            severities.add(RuleSeverity.MEDIUM);
        }
        if (severityLevelHighCheckBox.isSelected()) {
            severities.add(RuleSeverity.HIGH);
        }
        if (severityLevelCriticalCheckBox.isSelected()) {
            severities.add(RuleSeverity.CRITICAL);
        }
        return severities;
    }

    private List<String> getSelectedStatuses() {
        List<String> statuses = new ArrayList<>();
        if (statusAutoRemediatedCheckBox.isSelected()) {
            statuses.add(Constants.VULNERABILITY_STATUS_AUTO_REMEDIATED);
        }
        if (statusConfirmedCheckBox.isSelected()) {
            statuses.add(Constants.VULNERABILITY_STATUS_CONFIRMED);
        }
        if (statusSuspiciousCheckBox.isSelected()) {
            statuses.add(Constants.VULNERABILITY_STATUS_SUSPICIOUS);
        }
        if (statusNotAProblemCheckBox.isSelected()) {
            statuses.add(Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM);
        }
        if (statusRemediatedCheckBox.isSelected()) {
            statuses.add(Constants.VULNERABILITY_STATUS_REMEDIATED);
        }
        if (statusReportedCheckBox.isSelected()) {
            statuses.add(Constants.VULNERABILITY_STATUS_REPORTED);
        }
        if (statusFixedCheckBox.isSelected()) {
            statuses.add(Constants.VULNERABILITY_STATUS_FIXED);
        }
        if (statusBeingTrackedCheckBox.isSelected()) {
            statuses.add(Constants.VULNERABILITY_STATUS_BEING_TRACKED);
        }
        if (statusUntrackedCheckBox.isSelected()) {
            statuses.add(Constants.VULNERABILITY_STATUS_UNTRACKED);
        }
        return statuses;
    }

    private Date getDateFromLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            return date;
        } else {
            return null;
        }
    }

    private TraceFilterForm extractFiltersIntoTraceFilterForm() {

        EnumSet<RuleSeverity> severities = getSelectedSeverities();
        List<String> statuses = getSelectedStatuses();
        Date fromDate = getDateFromLocalDateTime(lastDetectedFromDateTimePicker.getDateTimePermissive());
        Date toDate = getDateFromLocalDateTime(lastDetectedToDateTimePicker.getDateTimePermissive());

        ApplicationComboBoxItem applicationComboBoxItem = (ApplicationComboBoxItem) applicationsComboBox.getSelectedItem();
        ServerComboBoxItem serverComboBoxItem = (ServerComboBoxItem) serversComboBox.getSelectedItem();

        Long serverId = Constants.ALL_SERVERS;
        String appId = Constants.ALL_APPLICATIONS;

        if (serverComboBoxItem.getServer() != null) {
            serverId = serverComboBoxItem.getServer().getServerId();
        }
        if (applicationComboBoxItem.getApplication() != null) {
            appId = applicationComboBoxItem.getApplication().getId();
        }
        TraceFilterForm form = null;
        if (serverId == Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
            form = Util.getTraceFilterForm(currentOffset, PAGE_LIMIT);
        } else if (serverId == Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
            form = Util.getTraceFilterForm(currentOffset, PAGE_LIMIT);
        } else if (serverId != Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
            form = Util.getTraceFilterForm(serverId, currentOffset, PAGE_LIMIT);
        } else if (serverId != Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
            form = Util.getTraceFilterForm(serverId, currentOffset, PAGE_LIMIT);
        }
        form.setSeverities(severities);
        form.setStatus(statuses);
        form.setStartDate(fromDate);
        form.setEndDate(toDate);
        form.setOffset(currentOffset);

        return form;
    }

    private void saveFilters() {

        ServerComboBoxItem serverComboBoxItem = (ServerComboBoxItem) serversComboBox.getSelectedItem();
        if (serverComboBoxItem != null) {
            if (serverComboBoxItem.getServer() != null) {
                contrastFilterPersistentStateComponent.setSelectedServerUuid(serverComboBoxItem.getServer().getServerId());
            } else {
                contrastFilterPersistentStateComponent.setSelectedServerUuid(null);
            }
        }
        ApplicationComboBoxItem applicationComboBoxItem = (ApplicationComboBoxItem) applicationsComboBox.getSelectedItem();

        if (applicationComboBoxItem != null) {
            contrastFilterPersistentStateComponent.setSelectedApplicationName(applicationComboBoxItem.toString());
            if (applicationComboBoxItem.getApplication() != null) {
                contrastFilterPersistentStateComponent.setSelectedApplicationId(applicationComboBoxItem.getApplication().getId());
            } else {
                contrastFilterPersistentStateComponent.setSelectedApplicationId(null);
            }
        }
        List<String> selectedSeverities = getSelectedSeveritiesAsList();
        if (!selectedSeverities.isEmpty()) {
            contrastFilterPersistentStateComponent.setSeverities(selectedSeverities);
        }
        String lastDetected = (String) lastDetectedComboBox.getSelectedItem();
        if (lastDetected != null) {
            contrastFilterPersistentStateComponent.setLastDetected(lastDetected);
        }

        Date fromDate = getDateFromLocalDateTime(lastDetectedFromDateTimePicker.getDateTimePermissive());
        if (fromDate != null) {
            contrastFilterPersistentStateComponent.setLastDetectedFrom(fromDate.getTime());
        } else {
            contrastFilterPersistentStateComponent.setLastDetectedFrom(null);
        }

        Date toDate = getDateFromLocalDateTime(lastDetectedToDateTimePicker.getDateTimePermissive());
        if (toDate != null) {
            contrastFilterPersistentStateComponent.setLastDetectedTo(toDate.getTime());
        } else {
            contrastFilterPersistentStateComponent.setLastDetectedTo(null);
        }

        List<String> selectedStatuses = getSelectedStatuses();
        if (!selectedStatuses.isEmpty()) {
            contrastFilterPersistentStateComponent.setStatuses(selectedStatuses);
        }
    }

    private List<String> getSelectedSeveritiesAsList() {
        List<String> severities = new ArrayList<>();
        if (severityLevelNoteCheckBox.isSelected()) {
            severities.add(RuleSeverity.NOTE.toString());
        }
        if (severityLevelLowCheckBox.isSelected()) {
            severities.add(RuleSeverity.LOW.toString());
        }
        if (severityLevelMediumCheckBox.isSelected()) {
            severities.add(RuleSeverity.MEDIUM.toString());
        }
        if (severityLevelHighCheckBox.isSelected()) {
            severities.add(RuleSeverity.HIGH.toString());
        }
        if (severityLevelCriticalCheckBox.isSelected()) {
            severities.add(RuleSeverity.CRITICAL.toString());
        }
        return severities;
    }

    public TraceFilterForm getTraceFilterForm() {
        return traceFilterForm;
    }
}
