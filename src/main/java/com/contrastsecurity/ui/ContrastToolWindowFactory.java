/*******************************************************************************
 * Copyright (c) 2017 Contrast Security.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License.
 *
 * The terms of the GNU GPL version 3 which accompanies this distribution
 * and is available at https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Contributors:
 *     Contrast Security - initial API and implementation
 *******************************************************************************/
package com.contrastsecurity.ui;

import com.contrastsecurity.config.ApplicationComboBoxItem;
import com.contrastsecurity.config.ContrastFilterPersistentStateComponent;
import com.contrastsecurity.config.ContrastUtil;
import com.contrastsecurity.config.ServerComboBoxItem;
import com.contrastsecurity.core.Constants;
import com.contrastsecurity.core.Util;
import com.contrastsecurity.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.core.internal.preferences.OrganizationConfig;
import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.RuleSeverity;
import com.contrastsecurity.http.ServerFilterForm;
import com.contrastsecurity.http.TraceFilterForm;
import com.contrastsecurity.models.*;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.optionalusertools.DateTimeChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateTimeChangeEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jdesktop.swingx.JXDatePicker;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

public class ContrastToolWindowFactory implements ToolWindowFactory {

    private JPanel contrastToolWindowContent;
    private JComboBox serversComboBox;
    private JComboBox applicationsComboBox;
    private JComboBox pagesComboBox;
    private JLabel serversLabel;
    private JLabel applicationsLabel;
    private JLabel pagesLabel;
    private JTable vulnerabilitiesTable;
    private JScrollPane scrollPane;
    private JButton getTracesButton;
    private JToolBar toolBar;
    private JLabel settingsLabel;
    private JLabel refreshLabel;
    private JLabel saveLabel;
    private JLabel severityLabel;
    private JCheckBox severityLevelNoteCheckBox;
    private JCheckBox severityLevelMediumCheckBox;
    private JCheckBox severityLevelLowCheckBox;
    private JCheckBox severityLevelHighCheckBox;
    private JCheckBox severityLevelCriticalCheckBox;
    private JLabel statusLabel;
    private JCheckBox statusAutoRemediatedCheckBox;
    private JCheckBox statusConfirmedCheckBox;
    private JCheckBox statusSuspiciousCheckBox;
    private JCheckBox statusNotAProblemCheckBox;
    private JCheckBox statusRemediatedCheckBox;
    private JCheckBox statusReportedCheckBox;
    private JCheckBox statusFixedCheckBox;
    private JCheckBox statusBeingTrackedCheckBox;
    private JCheckBox statusUntrackedCheckBox;
    private JXDatePicker JXDatePicker1;
    private ToolWindow contrastToolWindow;
    private JLabel lastDetectedLabel;
    private JLabel lastDetectedFromLabel;
    private JLabel lastDetectedToLabel;
    private DateTimePicker lastDetectedFromDateTimePicker;
    private DateTimePicker lastDetectedToDateTimePicker;
    private JComboBox lastDetectedComboBox;
    private JPanel cardPanel;
    private JPanel noVulnerabilitiesPanel;
    private JSplitPane mainCard;
    private JLabel noVulnerabilitiesLabel;

    // Non-UI variables
    private ContrastUtil contrastUtil;
    private ExtendedContrastSDK extendedContrastSDK;
    private int currentOffset = 0;
    private static final int PAGE_LIMIT = 20;
    private String traceSort = Constants.SORT_DESCENDING + Constants.SORT_BY_SEVERITY;
    private ContrastTableModel contrastTableModel = new ContrastTableModel();
    private OrganizationConfig organizationConfig;
    private boolean updatePagesComboBox = false;
    private ContrastFilterPersistentStateComponent contrastFilterPersistentStateComponent;

    public ContrastToolWindowFactory() {

        contrastFilterPersistentStateComponent = ContrastFilterPersistentStateComponent.getInstance();
        setupCheckBoxes();
        setupComboBoxes();

        getTracesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                refreshTraces();
                updatePagesComboBox = false;
            }
        });

        settingsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ShowSettingsUtil.getInstance().showSettingsDialog(null, "Contrast");
            }
        });

        saveLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                saveFilters();
            }
        });

        refreshLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                refresh();
            }
        });

        lastDetectedFromDateTimePicker.addDateTimeChangeListener(new DateTimeChangeListener() {
            @Override
            public void dateOrTimeChanged(DateTimeChangeEvent event) {
                cleanTableAndPagesComboBox();
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
                cleanTableAndPagesComboBox();
                if (lastDetectedFromDateTimePicker.getDateTimePermissive() != null && lastDetectedToDateTimePicker.getDateTimePermissive() != null) {

                    if (!isFromDateLessThanToDate(lastDetectedFromDateTimePicker.getDateTimePermissive(), lastDetectedToDateTimePicker.getDateTimePermissive())) {
                        lastDetectedFromDateTimePicker.clear();
                    }
                }
            }
        });

        refresh();

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

    private Date getDateFromLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            return date;
        } else {
            return null;
        }
    }

    public void refresh() {
        contrastUtil = new ContrastUtil();
        extendedContrastSDK = contrastUtil.getContrastSDK();
        organizationConfig = contrastUtil.getSelectedOrganizationConfig();
        updateServersComboBox();
        updateLastDetectedComboBox();
        setupTable();

        populateFiltersWithDataFromContrastFilterPersistentStateComponent();
    }

    private void cleanTableAndPagesComboBox() {
        contrastTableModel.setData(new Trace[0]);
        contrastTableModel.fireTableDataChanged();
        updatePagesComboBox(PAGE_LIMIT, 0);
        updatePagesComboBox = true;
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

        severityLevelNoteCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanTableAndPagesComboBox();
            }
        });
        severityLevelLowCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanTableAndPagesComboBox();
            }
        });
        severityLevelMediumCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanTableAndPagesComboBox();
            }
        });
        severityLevelHighCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanTableAndPagesComboBox();
            }
        });
        severityLevelCriticalCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanTableAndPagesComboBox();
            }
        });
//
//
//
        statusAutoRemediatedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanTableAndPagesComboBox();
            }
        });
        statusConfirmedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanTableAndPagesComboBox();
            }
        });
        statusSuspiciousCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanTableAndPagesComboBox();
            }
        });
        statusNotAProblemCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanTableAndPagesComboBox();
            }
        });
        statusRemediatedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanTableAndPagesComboBox();
            }
        });
        statusReportedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanTableAndPagesComboBox();
            }
        });
        statusFixedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanTableAndPagesComboBox();
            }
        });
        statusBeingTrackedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanTableAndPagesComboBox();
            }
        });
        statusUntrackedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanTableAndPagesComboBox();
            }
        });
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

        applicationsComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.SELECTED) {
                    cleanTableAndPagesComboBox();
                }
            }
        });

        pagesComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.SELECTED) {
                    String p = (String) e.getItem();
                    int page = Integer.parseInt(p);
                    currentOffset = PAGE_LIMIT * (page - 1);
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

    private void refreshTraces() {

        new Thread(new Runnable() {
            @Override
            public void run() {
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

                Trace[] traces = new Trace[0];
                try {
                    Traces tracesObject = getTraces(organizationConfig.getUuid(), serverId, appId, currentOffset, PAGE_LIMIT);
                    if (tracesObject != null && tracesObject.getTraces() != null && !tracesObject.getTraces().isEmpty()) {
                        traces = tracesObject.getTraces().toArray(new Trace[0]);
                    }
                    if (updatePagesComboBox) {
                        updatePagesComboBox(PAGE_LIMIT, tracesObject.getCount());
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                } catch (UnauthorizedException exception) {
                    exception.printStackTrace();
                }
                contrastTableModel.setData(traces);
                contrastTableModel.fireTableDataChanged();
            }
        }).start();
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        contrastToolWindow = toolWindow;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(contrastToolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private Traces getTraces(String orgUuid, Long serverId, String appId, int offset, int limit)
            throws IOException, UnauthorizedException {

        Traces traces = null;

        EnumSet<RuleSeverity> severities = getSelectedSeverities();
        List<String> statuses = getSelectedStatuses();
        Date fromDate = getDateFromLocalDateTime(lastDetectedFromDateTimePicker.getDateTimePermissive());
        Date toDate = getDateFromLocalDateTime(lastDetectedToDateTimePicker.getDateTimePermissive());

        if (extendedContrastSDK != null) {
            if (serverId == Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
                TraceFilterForm form = Util.getTraceFilterForm(offset, limit, traceSort);
                form.setSeverities(severities);
                form.setStatus(statuses);
                form.setStartDate(fromDate);
                form.setEndDate(toDate);
                traces = extendedContrastSDK.getTracesInOrg(orgUuid, form);
            } else if (serverId == Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
                TraceFilterForm form = Util.getTraceFilterForm(offset, limit, traceSort);
                form.setSeverities(severities);
                form.setStatus(statuses);
                form.setStartDate(fromDate);
                form.setEndDate(toDate);
                traces = extendedContrastSDK.getTraces(orgUuid, appId, form);
            } else if (serverId != Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
                TraceFilterForm form = Util.getTraceFilterForm(serverId, offset, limit, traceSort);
                form.setSeverities(severities);
                form.setStatus(statuses);
                form.setStartDate(fromDate);
                form.setEndDate(toDate);
                traces = extendedContrastSDK.getTracesInOrg(orgUuid, form);
            } else if (serverId != Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
                TraceFilterForm form = Util.getTraceFilterForm(serverId, offset, limit, traceSort);
                form.setSeverities(severities);
                form.setStatus(statuses);
                form.setStartDate(fromDate);
                form.setEndDate(toDate);
                traces = extendedContrastSDK.getTraces(orgUuid, appId, form);
            }
        }

        return traces;
    }

    private void setupTable() {
        vulnerabilitiesTable.setModel(contrastTableModel);
        TableColumn severityColumn = vulnerabilitiesTable.getColumnModel().getColumn(0);
        severityColumn.setMaxWidth(76);
        severityColumn.setMinWidth(76);

        vulnerabilitiesTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {


                if (contrastTableModel != null && contrastTableModel.getRowCount() > 1) {
                    int col = vulnerabilitiesTable.columnAtPoint(e.getPoint());
                    String name = vulnerabilitiesTable.getColumnName(col);

                    if (name.equals("Severity")) {
                        if (traceSort.startsWith(Constants.SORT_DESCENDING)) {
                            traceSort = Constants.SORT_BY_SEVERITY;
                        } else {
                            traceSort = Constants.SORT_DESCENDING + Constants.SORT_BY_SEVERITY;
                        }
                        refreshTraces();
                        updatePagesComboBox = false;
                    } else if (name.equals("Vulnerability")) {
                        if (traceSort.startsWith(Constants.SORT_DESCENDING)) {
                            traceSort = Constants.SORT_BY_TITLE;
                        } else {
                            traceSort = Constants.SORT_DESCENDING + Constants.SORT_BY_TITLE;
                        }
                        refreshTraces();
                        updatePagesComboBox = false;
                    } else if (name.equals("Last Detected")) {
                        if (traceSort.startsWith(Constants.SORT_DESCENDING)) {
                            traceSort = Constants.SORT_BY_LAST_TIME_SEEN;
                        } else {
                            traceSort = Constants.SORT_DESCENDING + Constants.SORT_BY_LAST_TIME_SEEN;
                        }
                        refreshTraces();
                        updatePagesComboBox = false;
                    } else if (name.equals("Status")) {
                        if (traceSort.startsWith(Constants.SORT_DESCENDING)) {
                            traceSort = Constants.SORT_BY_STATUS;
                        } else {
                            traceSort = Constants.SORT_DESCENDING + Constants.SORT_BY_STATUS;
                        }
                        refreshTraces();
                        updatePagesComboBox = false;
                    }
                }

            }
        });

        vulnerabilitiesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = vulnerabilitiesTable.rowAtPoint(evt.getPoint());
                int col = vulnerabilitiesTable.columnAtPoint(evt.getPoint());

                if (row >= 0 && col >= 0) {
                    String name = vulnerabilitiesTable.getColumnName(col);
                    if (name.equals("Open in Teamserver")) {
                        Trace traceClicked = contrastTableModel.getTraceAtRow(row);
                        openWebpage(traceClicked);
                    }
                }
            }
        });
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

    private void updateServersComboBox() {

        serversComboBox.removeAllItems();
        int count = 0;
        Servers servers = null;
        try {
            ServerFilterForm serverFilterForm = new ServerFilterForm();
            serverFilterForm.setExpand(EnumSet.of(ServerFilterForm.ServerExpandValue.APPLICATIONS));
            servers = extendedContrastSDK.getServers(organizationConfig.getUuid(), serverFilterForm);
            if (!mainCard.isVisible()) {
                CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
                cardLayout.show(cardPanel, "mainCard");
            }

        } catch (Exception e) {
            if (!noVulnerabilitiesPanel.isVisible()) {
                CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
                cardLayout.show(cardPanel, "noVulnerabilitiesCard");
            }
            e.printStackTrace();
        }
        if (servers != null && servers.getServers() != null && !servers.getServers().isEmpty()) {
            for (Server server : servers.getServers()) {
                ServerComboBoxItem contrastServer = new ServerComboBoxItem(server);
                serversComboBox.addItem(contrastServer);
                count++;
            }
            ServerComboBoxItem allServers = new ServerComboBoxItem("All Servers(" + count + ")");
            serversComboBox.addItem(allServers);
        }
    }

    public void updateApplicationsComboBox(Server server) {
        applicationsComboBox.removeAllItems();
        int count = 0;
        List<Application> applications = null;

        if (server == null) {
            try {
                Applications apps = extendedContrastSDK.getApplications(organizationConfig.getUuid());
                if (apps != null) {
                    applications = apps.getApplications();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        }
    }

    public void updatePagesComboBox(final int pageLimit, final int totalElements) {
        pagesComboBox.removeAllItems();

        int numOfPages = 1;
        if (totalElements % pageLimit > 0) {
            numOfPages = totalElements / pageLimit + 1;
        } else {
            if (totalElements != 0) {
                numOfPages = totalElements / pageLimit;
            }
        }

        for (int i = 1; i <= numOfPages; i++) {
            pagesComboBox.addItem(String.valueOf(i));
        }
        if (numOfPages == 1) {
            pagesComboBox.setEnabled(false);
        } else {
            pagesComboBox.setEnabled(true);
        }
        pagesComboBox.setSelectedItem("1");
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

    private URL getOverviewUrl(String traceId) throws MalformedURLException {
        String teamServerUrl = contrastUtil.getTeamServerUrl();
        teamServerUrl = teamServerUrl.trim();
        if (teamServerUrl != null && teamServerUrl.endsWith("/api")) {
            teamServerUrl = teamServerUrl.substring(0, teamServerUrl.length() - 4);
        }
        if (teamServerUrl != null && teamServerUrl.endsWith("/api/")) {
            teamServerUrl = teamServerUrl.substring(0, teamServerUrl.length() - 5);
        }
        String urlStr = teamServerUrl + "/static/ng/index.html#/" + contrastUtil.getSelectedOrganizationConfig().getUuid() + "/vulns/" + traceId + "/overview";
        URL url = new URL(urlStr);
        return url;
    }

    public void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void openWebpage(Trace trace) {
        if (trace == null) {
            return;
        }
        // https://apptwo.contrastsecurity.com/Contrast/static/ng/index.html#/orgUuid/vulns/<VULN_ID>/overview
        try {
            URL url = getOverviewUrl(trace.getUuid());
            openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
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
        if ((String) pagesComboBox.getSelectedItem() != null) {
            Integer page = Integer.valueOf((String) pagesComboBox.getSelectedItem());
            if (page != null) {
                contrastFilterPersistentStateComponent.setPage(page);
            }
        }
        contrastFilterPersistentStateComponent.setSort(traceSort);
    }

    private void populateFiltersWithDataFromContrastFilterPersistentStateComponent() {
        if (contrastFilterPersistentStateComponent.getSelectedServerUuid() != null) {
            selectServerByUuid(contrastFilterPersistentStateComponent.getSelectedServerUuid());
        } else {
            selectAllServers();
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
        if (contrastFilterPersistentStateComponent.getPage() != null) {
            pagesComboBox.setSelectedItem(String.valueOf(contrastFilterPersistentStateComponent.getPage()));
        }

        if (contrastFilterPersistentStateComponent.getSort() != null && !contrastFilterPersistentStateComponent.getSort().isEmpty()) {
            traceSort = contrastFilterPersistentStateComponent.getSort();
        }

    }

    private LocalDateTime getLocalDateTimeFromMillis(Long millis) {
        Date date = new Date(millis);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return localDateTime;
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

    private void selectAllServers() {
        int itemCount = serversComboBox.getItemCount();
        if (itemCount > 0) {
            for (int i = 0; i < itemCount; i++) {
                ServerComboBoxItem serverComboBoxItem = (ServerComboBoxItem) serversComboBox.getItemAt(i);
                if (serverComboBoxItem.toString().startsWith("All Servers(")) {
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
}
