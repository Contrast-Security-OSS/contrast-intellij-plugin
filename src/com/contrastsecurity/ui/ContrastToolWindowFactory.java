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
import com.sun.jna.platform.unix.X11;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;

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
    private ToolWindow contrastToolWindow;

    // Non-UI variables
    private ContrastUtil contrastUtil;
    private ExtendedContrastSDK extendedContrastSDK;
    private int currentOffset = 0;
    private static final int PAGE_LIMIT = 20;
    private String traceSort = Constants.SORT_DESCENDING + Constants.SORT_BY_SEVERITY;
    private ContrastTableModel contrastTableModel;
    private OrganizationConfig organizationConfig;
    private boolean updatePagesComboBox = false;

    public ContrastToolWindowFactory() {
        contrastUtil = new ContrastUtil();
        contrastTableModel = new ContrastTableModel();
        extendedContrastSDK = contrastUtil.getContrastSDK();
        organizationConfig = contrastUtil.getSelectedOrganizationConfig();

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
                    contrastTableModel.setData(new Trace[0]);
                    contrastTableModel.fireTableDataChanged();
                    updatePagesComboBox(PAGE_LIMIT, 0);
                    updatePagesComboBox = true;
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

        getTracesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                refreshTraces();
                updatePagesComboBox = false;
            }
        });

        settingsLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        updateServersComboBox();
        setupTable();
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

        if (extendedContrastSDK != null) {
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
}
