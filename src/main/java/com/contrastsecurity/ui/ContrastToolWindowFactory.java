package com.contrastsecurity.ui;

import com.contrastsecurity.config.ApplicationComboBoxItem;
import com.contrastsecurity.config.ContrastUtil;
import com.contrastsecurity.config.ServerComboBoxItem;
import com.contrastsecurity.core.Constants;
import com.contrastsecurity.core.Util;
import com.contrastsecurity.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.core.internal.preferences.OrganizationConfig;
import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.ServerFilterForm;
import com.contrastsecurity.http.TraceFilterForm;
import com.contrastsecurity.models.*;
import com.contrastsecurity.ui.settings.ContrastSearchableConfigurableGUI;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ContrastToolWindowFactory implements ToolWindowFactory {

    private JPanel contrastToolWindowContent;
    private JButton settingsButton;
    private JButton refreshButton;
    private JButton saveButton;
    private JToolBar toolBar;
    private JComboBox serversComboBox;
    private JComboBox applicationsComboBox;
    private JComboBox pagesComboBox;
    private JLabel serversLabel;
    private JLabel applicationsLabel;
    private JLabel pagesLabel;
    private JTable vulnerabilitiesTable;
    private JScrollPane scrollPane;
    private ToolWindow contrastToolWindow;

    // Non-UI variables
    private ContrastUtil contrastUtil;
    private ExtendedContrastSDK extendedContrastSDK;
    private int currentOffset = 0;
    private static final int PAGE_LIMIT = 20;
    private String traceSort = Constants.SORT_DESCENDING + Constants.SORT_BY_SEVERITY;
    ContrastTableModel contrastTableModel;
    private OrganizationConfig organizationConfig;

    public ContrastToolWindowFactory() {
//        contrastUtil = new ContrastUtil();
//        contrastTableModel = new ContrastTableModel();
//        extendedContrastSDK = contrastUtil.getContrastSDK();
//        organizationConfig = contrastUtil.getSelectedOrganizationConfig();

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

    private void populateTable() {
        Trace[] traces = new Trace[0];

        try {
            traces = getTraces(organizationConfig.getUuid(), Constants.ALL_SERVERS, Constants.ALL_APPLICATIONS, currentOffset, PAGE_LIMIT);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }

        contrastTableModel.setData(traces);
    }

    private Trace[] getTraces(String orgUuid, Long serverId, String appId, int offset, int limit)
            throws IOException, UnauthorizedException {

        Trace[] traceArray = new Trace[0];

        if (extendedContrastSDK != null) {
            Traces traces = null;
            if (serverId == Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
                TraceFilterForm form = Util.getTraceFilterForm(offset, limit, traceSort);
                traces = extendedContrastSDK.getTracesInOrg(orgUuid, form);
            } else if (serverId == Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
                TraceFilterForm form = Util.getTraceFilterForm(offset, limit, traceSort);
                traces = extendedContrastSDK.getTraces(orgUuid, appId, form);
            } else if (serverId != Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
                TraceFilterForm form = Util.getTraceFilterForm(serverId, offset, limit, traceSort);
                traces = extendedContrastSDK.getTracesInOrg(orgUuid, form);
            } else if (serverId != Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
                TraceFilterForm form = Util.getTraceFilterForm(serverId, offset, limit, traceSort);
                traces = extendedContrastSDK.getTraces(orgUuid, appId, form);
            }

            if (traces != null && traces.getTraces() != null && !traces.getTraces().isEmpty()) {
                traceArray = traces.getTraces().toArray(new Trace[0]);
            }
        }

        return traceArray;
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
}
