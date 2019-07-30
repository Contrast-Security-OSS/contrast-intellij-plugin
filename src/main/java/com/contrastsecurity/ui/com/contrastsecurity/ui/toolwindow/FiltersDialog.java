/******************************************************************************
 Copyright (c) 2017 Contrast Security.
 All rights reserved.

 This program and the accompanying materials are made available under
 the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation; either version 3 of the License.

 The terms of the GNU GPL version 3 which accompanies this distribution
 and is available at https://www.gnu.org/licenses/gpl-3.0.en.html

 Contributors:
 Contrast Security - initial API and implementation
 */
package com.contrastsecurity.ui.com.contrastsecurity.ui.toolwindow;

import com.contrastsecurity.config.ContrastFilterPersistentStateComponent;
import com.contrastsecurity.core.Constants;
import com.contrastsecurity.core.Util;
import com.contrastsecurity.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.core.extended.Filter;
import com.contrastsecurity.core.extended.FilterResource;
import com.contrastsecurity.core.internal.preferences.OrganizationConfig;
import com.contrastsecurity.http.RuleSeverity;
import com.contrastsecurity.http.TraceFilterForm;
import com.contrastsecurity.models.Application;
import com.contrastsecurity.models.Server;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.intellij.openapi.project.Project;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
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
    private JComboBox appVersionTagsComboBox;
    private JButton refreshAppVersionTagsButton;
    private JButton clearAppVersionTagsButton;
    private ContrastFilterPersistentStateComponent contrastFilterPersistentStateComponent;
    private List<Server> servers;
    private List<Application> applications;

    private TraceFilterForm traceFilterForm;
    private ExtendedContrastSDK extendedContrastSDK;

    FiltersDialog(List<Server> servers, List<Application> applications, ExtendedContrastSDK extendedContrastSDK, OrganizationConfig organizationConfig, Project project) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

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
        this.extendedContrastSDK = extendedContrastSDK;

        contrastFilterPersistentStateComponent = ContrastFilterPersistentStateComponent.getInstance(project);

        setupCheckBoxes();
        setupComboBoxes();

        lastDetectedFromDateTimePicker.addDateTimeChangeListener(event -> {
            if (lastDetectedFromDateTimePicker.getDateTimePermissive() != null && lastDetectedToDateTimePicker.getDateTimePermissive() != null) {

                if (!isFromDateLessThanToDate(lastDetectedFromDateTimePicker.getDateTimePermissive(), lastDetectedToDateTimePicker.getDateTimePermissive())) {
                    lastDetectedToDateTimePicker.clear();
                }
            }
        });

        lastDetectedToDateTimePicker.addDateTimeChangeListener(event -> {
            if (lastDetectedFromDateTimePicker.getDateTimePermissive() != null && lastDetectedToDateTimePicker.getDateTimePermissive() != null) {

                if (!isFromDateLessThanToDate(lastDetectedFromDateTimePicker.getDateTimePermissive(), lastDetectedToDateTimePicker.getDateTimePermissive())) {
                    lastDetectedFromDateTimePicker.clear();
                }
            }
        });

        refreshAppVersionTagsButton.addActionListener(e -> new Thread(() -> {
            contentPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            ApplicationComboBoxItem applicationComboBoxItem = (ApplicationComboBoxItem) applicationsComboBox.getSelectedItem();
            if (applicationComboBoxItem != null && applicationComboBoxItem.getApplication() != null) {
                String appId = applicationComboBoxItem.getApplication().getId();
                FilterResource filterResource = getApplicationTraceFiltersByType(organizationConfig.getUuid(), appId);
                if (filterResource != null && filterResource.getFilters() != null) {
                    updateAppVersionTagsComboBox(filterResource.getFilters());
                }
            } else {
                updateAppVersionTagsComboBox(null);
            }
            contentPane.setCursor(Cursor.getDefaultCursor());
        }).start());

        clearAppVersionTagsButton.addActionListener(e -> appVersionTagsComboBox.removeAllItems());

        new Thread(this::refresh).start();
    }

    private FiltersDialog() {

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

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
        dispose();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private void populateFiltersWithDataFromContrastFilterPersistentStateComponent() {
        if (contrastFilterPersistentStateComponent.getSelectedServerUuid() != null) {
            selectServerByUuid(contrastFilterPersistentStateComponent.getSelectedServerUuid());
        }

        if (contrastFilterPersistentStateComponent.getSelectedApplicationName() != null &&
                !contrastFilterPersistentStateComponent.getSelectedApplicationName().isEmpty()) {
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
        if (contrastFilterPersistentStateComponent.getAppVersionTag() != null && !contrastFilterPersistentStateComponent.getAppVersionTag().isEmpty()) {
            appVersionTagsComboBox.addItem(contrastFilterPersistentStateComponent.getAppVersionTag());
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
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
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
            if (status.equals(Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM_API_REQUEST_STRING)) {
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

    private void refresh() {
        updateServersComboBox(servers);
        updateLastDetectedComboBox();
        populateFiltersWithDataFromContrastFilterPersistentStateComponent();
    }

    private void updateServersComboBox(List<Server> servers) {
        serversComboBox.removeAllItems();
        AutoCompleteDecorator.decorate(serversComboBox);
        int count = 0;
        if (servers != null && !servers.isEmpty()) {
            for (Server server : servers) {
                ServerComboBoxItem contrastServer = new ServerComboBoxItem(server);
                serversComboBox.addItem(contrastServer);
                count++;
            }
            ServerComboBoxItem allServers = new ServerComboBoxItem("All Servers(" + count + ")");
            serversComboBox.addItem(allServers);
            serversComboBox.setSelectedItem(allServers);
        }
    }

    private void updateAppVersionTagsComboBox(List<Filter> filters) {
        appVersionTagsComboBox.removeAllItems();
        if (filters != null && !filters.isEmpty()) {
            for (Filter filter : filters) {
                appVersionTagsComboBox.addItem(filter);
            }
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
            severityLevelNoteCheckBox.setSelected(false);
            severityLevelLowCheckBox.setSelected(false);
            severityLevelMediumCheckBox.setSelected(false);
            severityLevelHighCheckBox.setSelected(false);
            severityLevelCriticalCheckBox.setSelected(false);
        }

        if (contrastFilterPersistentStateComponent.getStatuses() == null || contrastFilterPersistentStateComponent.getStatuses().isEmpty()) {

            statusAutoRemediatedCheckBox.setSelected(false);
            statusConfirmedCheckBox.setSelected(false);
            statusSuspiciousCheckBox.setSelected(false);
            statusNotAProblemCheckBox.setSelected(false);
            statusRemediatedCheckBox.setSelected(false);
            statusReportedCheckBox.setSelected(false);
            statusFixedCheckBox.setSelected(false);
            statusBeingTrackedCheckBox.setSelected(false);
            statusUntrackedCheckBox.setSelected(false);

        }
    }

    private void setupComboBoxes() {

        serversComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ServerComboBoxItem serverComboBoxItem = (ServerComboBoxItem) e.getItem();
                updateApplicationsComboBox(serverComboBoxItem.getServer());
            }
        });


        applicationsComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateAppVersionTagsComboBox(null);
                ApplicationComboBoxItem applicationComboBoxItem = (ApplicationComboBoxItem) applicationsComboBox.getSelectedItem();
                if (applicationComboBoxItem == null || applicationComboBoxItem.getApplication() == null) {
                    appVersionTagsComboBox.setEnabled(false);
                    refreshAppVersionTagsButton.setEnabled(false);
                    clearAppVersionTagsButton.setEnabled(false);
                } else {
                    appVersionTagsComboBox.setEnabled(true);
                    refreshAppVersionTagsButton.setEnabled(true);
                    clearAppVersionTagsButton.setEnabled(true);
                }
            }
        });

        lastDetectedComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                lastDetectedToDateTimePicker.clear();
                LocalDateTime localDateTime = LocalDateTime.now();

                if (!e.getItem().toString().equals((Constants.LAST_DETECTED_CUSTOM))) {
                    lastDetectedFromDateTimePicker.setEnabled(false);
                    lastDetectedToDateTimePicker.setEnabled(false);
                }

                switch (e.getItem().toString()) {
                    case Constants.LAST_DETECTED_ALL:
                        lastDetectedFromDateTimePicker.clear();

                        break;
                    case Constants.LAST_DETECTED_HOUR:

                        lastDetectedFromDateTimePicker.setDateTimeStrict(localDateTime.minusHours(1));

                        break;
                    case Constants.LAST_DETECTED_DAY:

                        lastDetectedFromDateTimePicker.setDateTimeStrict(localDateTime.minusDays(1));

                        break;
                    case Constants.LAST_DETECTED_WEEK:

                        lastDetectedFromDateTimePicker.setDateTimeStrict(localDateTime.minusWeeks(1));

                        break;
                    case Constants.LAST_DETECTED_MONTH:

                        lastDetectedFromDateTimePicker.setDateTimeStrict(localDateTime.minusMonths(1));

                        break;
                    case Constants.LAST_DETECTED_YEAR:

                        lastDetectedFromDateTimePicker.setDateTimeStrict(localDateTime.minusYears(1));

                        break;
                    case Constants.LAST_DETECTED_CUSTOM:
                        lastDetectedFromDateTimePicker.setEnabled(true);
                        lastDetectedToDateTimePicker.setEnabled(true);
                        break;
                }
            }
        });
    }

    private boolean isFromDateLessThanToDate(LocalDateTime fromDate, LocalDateTime toDate) {
        Date lastDetectedFromDate = Date.from(fromDate.atZone(ZoneId.systemDefault()).toInstant());
        Date lastDetectedToDate = Date.from(toDate.atZone(ZoneId.systemDefault()).toInstant());

        return lastDetectedFromDate.getTime() < lastDetectedToDate.getTime();
    }

    private void updateApplicationsComboBox(Server server) {
        applicationsComboBox.removeAllItems();
        int count = 0;
        List<Application> applications;

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
            statuses.add(Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM_API_REQUEST_STRING);
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
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
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

        if (serverComboBoxItem != null && serverComboBoxItem.getServer() != null) {
            serverId = serverComboBoxItem.getServer().getServerId();
        }
        if (applicationComboBoxItem != null && applicationComboBoxItem.getApplication() != null) {
            appId = applicationComboBoxItem.getApplication().getId();
        }
        TraceFilterForm form = null;
        int currentOffset = 0;
        if (serverId == Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
            form = Util.getTraceFilterForm(currentOffset, PAGE_LIMIT);
        } else if (serverId == Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
            form = Util.getTraceFilterForm(currentOffset, PAGE_LIMIT);
        } else if (serverId != Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
            form = Util.getTraceFilterForm(serverId, currentOffset, PAGE_LIMIT);
        } else if (serverId != Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
            form = Util.getTraceFilterForm(serverId, currentOffset, PAGE_LIMIT);
        }

        if (appVersionTagsComboBox.getSelectedItem() != null) {
            form.setAppVersionTags(Collections.singletonList(appVersionTagsComboBox.getSelectedItem().toString()));
        } else {
            form.setAppVersionTags(null);
        }

        form.setSeverities(severities);
        if (!statuses.isEmpty()) {
            form.setStatus(statuses);
        } else {
            form.setStatus(null);
        }
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
        contrastFilterPersistentStateComponent.setSeverities(selectedSeverities);

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
        contrastFilterPersistentStateComponent.setStatuses(selectedStatuses);

        if (appVersionTagsComboBox.getSelectedItem() != null) {
            contrastFilterPersistentStateComponent.setAppVersionTag(appVersionTagsComboBox.getSelectedItem().toString());
        } else {
            contrastFilterPersistentStateComponent.setAppVersionTag(null);
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

    TraceFilterForm getTraceFilterForm() {
        return traceFilterForm;
    }

    private FilterResource getApplicationTraceFiltersByType(String orgUuid, String appId) {
        FilterResource filterResource = null;
        try {
            filterResource = extendedContrastSDK.getApplicationTraceFiltersByType(orgUuid, appId, Constants.TRACE_FILTER_TYPE_APP_VERSION_TAGS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filterResource;
    }


}
