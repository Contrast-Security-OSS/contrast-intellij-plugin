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
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
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

    // Non-UI variables
    private ContrastUtil contrastUtil;
    private ExtendedContrastSDK extendedContrastSDK;
    private int currentOffset = 0;
    private static final int PAGE_LIMIT = 20;
    private String traceSort = Constants.SORT_DESCENDING + Constants.SORT_BY_SEVERITY;
    private ContrastTableModel contrastTableModel = new ContrastTableModel();
    private OrganizationConfig organizationConfig;
    private boolean updatePagesComboBox = false;

    public ContrastToolWindowFactory() {

        setupCheckBoxes();
        setupComboBoxes();

        getTracesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                refreshTraces();
                updatePagesComboBox = false;
            }
        });

        settingsLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ShowSettingsUtil.getInstance().showSettingsDialog(null, "Contrast");
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });

//        new Timer(3000, new ActionListener() { // Create 2 Second Timer
//            @Override
//            public void actionPerformed(ActionEvent event) {
//            }
//        }).start();

        lastDetectedFromDateTimePicker.addDateTimeChangeListener(new DateTimeChangeListener() {
            @Override
            public void dateOrTimeChanged(DateTimeChangeEvent event) {
                if (lastDetectedFromDateTimePicker.getDateTimePermissive() != null && lastDetectedToDateTimePicker.getDateTimePermissive() != null){
                    System.out.println(lastDetectedFromDateTimePicker.getDateTimePermissive());
                }
            }
        });

        refresh();

    }

    private void refresh() {
        contrastUtil = new ContrastUtil();
        extendedContrastSDK = contrastUtil.getContrastSDK();
        organizationConfig = contrastUtil.getSelectedOrganizationConfig();
        updateServersComboBox();
        setupTable();
    }

    private void cleanTableAndPagesComboBox(){
        contrastTableModel.setData(new Trace[0]);
        contrastTableModel.fireTableDataChanged();
        updatePagesComboBox(PAGE_LIMIT, 0);
        updatePagesComboBox = true;
    }

    private void setupCheckBoxes(){
        severityLevelNoteCheckBox.setSelected(true);
        severityLevelLowCheckBox.setSelected(true);
        severityLevelMediumCheckBox.setSelected(true);
        severityLevelHighCheckBox.setSelected(true);
        severityLevelCriticalCheckBox.setSelected(true);

        statusAutoRemediatedCheckBox.setSelected(true);
        statusConfirmedCheckBox.setSelected(true);
        statusSuspiciousCheckBox.setSelected(true);
        statusNotAProblemCheckBox.setSelected(true);
        statusRemediatedCheckBox.setSelected(true);
        statusReportedCheckBox.setSelected(true);
        statusFixedCheckBox.setSelected(true);
        statusBeingTrackedCheckBox.setSelected(true);
        statusUntrackedCheckBox.setSelected(true);

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
    }

    private void refreshTraces() {
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

//        updateServersComboBox();
//        setupTable();
//        populateTable();

//        serversComboBox.addItemListener(new ItemListener() {
//            @Override
//            public void itemStateChanged(ItemEvent e) {
//                if (e.getStateChange() == e.SELECTED) {
//                    ServerComboBoxItem serverComboBoxItem = (ServerComboBoxItem) e.getItem();
//                    updateApplicationsComboBox(serverComboBoxItem.getServer());
//                }
//            }
//        });

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

        if (extendedContrastSDK != null) {
            if (serverId == Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
                TraceFilterForm form = Util.getTraceFilterForm(offset, limit, traceSort);
                form.setSeverities(severities);
                form.setStatus(statuses);
                traces = extendedContrastSDK.getTracesInOrg(orgUuid, form);
            } else if (serverId == Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
                TraceFilterForm form = Util.getTraceFilterForm(offset, limit, traceSort);
                form.setSeverities(severities);
                form.setStatus(statuses);
                traces = extendedContrastSDK.getTraces(orgUuid, appId, form);
            } else if (serverId != Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
                TraceFilterForm form = Util.getTraceFilterForm(serverId, offset, limit, traceSort);
                form.setSeverities(severities);
                form.setStatus(statuses);
                traces = extendedContrastSDK.getTracesInOrg(orgUuid, form);
            } else if (serverId != Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
                TraceFilterForm form = Util.getTraceFilterForm(serverId, offset, limit, traceSort);
                form.setSeverities(severities);
                form.setStatus(statuses);
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
    }

    private void updateServersComboBox() {
        serversComboBox.removeAllItems();
        int count = 0;
        Servers servers = null;
        try {
            ServerFilterForm serverFilterForm = new ServerFilterForm();
            serverFilterForm.setExpand(EnumSet.of(ServerFilterForm.ServerExpandValue.APPLICATIONS));
            servers = extendedContrastSDK.getServers(organizationConfig.getUuid(), serverFilterForm);
        } catch (Exception e) {
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
        if (severityLevelNoteCheckBox.isSelected()){
            severities.add(RuleSeverity.NOTE);
        }
        if (severityLevelLowCheckBox.isSelected()){
            severities.add(RuleSeverity.LOW);
        }
        if (severityLevelMediumCheckBox.isSelected()){
            severities.add(RuleSeverity.MEDIUM);
        }
        if (severityLevelHighCheckBox.isSelected()){
            severities.add(RuleSeverity.HIGH);
        }
        if (severityLevelCriticalCheckBox.isSelected()){
            severities.add(RuleSeverity.CRITICAL);
        }
        return severities;
    }

    private List<String> getSelectedStatuses(){
        List<String> statuses = new ArrayList<>();
        if (statusAutoRemediatedCheckBox.isSelected()){
            statuses.add(Constants.VULNERABILITY_STATUS_AUTO_REMEDIATED);
        }
        if (statusConfirmedCheckBox.isSelected()){
            statuses.add(Constants.VULNERABILITY_STATUS_CONFIRMED);
        }
        if (statusSuspiciousCheckBox.isSelected()){
            statuses.add(Constants.VULNERABILITY_STATUS_SUSPICIOUS);
        }
        if (statusNotAProblemCheckBox.isSelected()){
            statuses.add(Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM);
        }
        if (statusRemediatedCheckBox.isSelected()){
            statuses.add(Constants.VULNERABILITY_STATUS_REMEDIATED);
        }
        if (statusReportedCheckBox.isSelected()){
            statuses.add(Constants.VULNERABILITY_STATUS_REPORTED);
        }
        if (statusFixedCheckBox.isSelected()){
            statuses.add(Constants.VULNERABILITY_STATUS_FIXED);
        }
        if (statusBeingTrackedCheckBox.isSelected()){
            statuses.add(Constants.VULNERABILITY_STATUS_BEING_TRACKED);
        }
        if (statusUntrackedCheckBox.isSelected()){
            statuses.add(Constants.VULNERABILITY_STATUS_UNTRACKED);
        }
        return statuses;
    }

}
