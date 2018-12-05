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
package com.contrastsecurity.ui.settings;

import com.contrastsecurity.config.ChangeActionNotifier;
import com.contrastsecurity.config.ContrastFilterPersistentStateComponent;
import com.contrastsecurity.config.ContrastPersistentStateComponent;
import com.contrastsecurity.config.ContrastUtil;
import com.contrastsecurity.core.Constants;
import com.contrastsecurity.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.models.Organization;
import com.contrastsecurity.models.Organizations;
import com.contrastsecurity.ui.com.contrastsecurity.ui.toolwindow.OrganizationTableModel;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ContrastSearchableConfigurableGUI {

    private final ContrastPersistentStateComponent contrastPersistentStateComponent;
    private final ContrastFilterPersistentStateComponent contrastFilterPersistentStateComponent;
    private JPanel contrastSettingsPanel;
    private JTextField teamServerTextField;
    private JTextField usernameTextField;
    private JTextField serviceKeyTextField;
    private JButton addButton;
    private JButton deleteButton;
    private JPasswordField apiKeyTextField;
    private JTextField uuidTextField;
    private JLabel testConnectionLabel;
    private JTable organizationTable;
    private Map<String, String> organizations = new HashMap<>();
    private OrganizationTableModel organizationTableModel = new OrganizationTableModel();

    public ContrastSearchableConfigurableGUI() {

        DataContext dataContext = DataManager.getInstance().getDataContextFromFocus().getResult();
        Project project = DataKeys.PROJECT.getData(dataContext);
        contrastFilterPersistentStateComponent = ContrastFilterPersistentStateComponent.getInstance(project);

        contrastPersistentStateComponent = ContrastPersistentStateComponent.getInstance();

        organizationTable.setModel(organizationTableModel);
        organizationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        populateFieldsWithValuesFromContrastPersistentStateComponent();

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String url = getTeamServerUrl();
                        URL u;
                        try {
                            u = new URL(url);
                        } catch (MalformedURLException e1) {
                            testConnectionLabel.setText("Connection failed!");
                            return;
                        }
                        if (!u.getProtocol().startsWith("http")) {
                            testConnectionLabel.setText("Connection failed!");
                            return;
                        }
                        Proxy proxy = ContrastUtil.getIdeaDefinedProxy(getTeamServerUrl()) != null
                                ? ContrastUtil.getIdeaDefinedProxy(getTeamServerUrl()) : Proxy.NO_PROXY;

                        ExtendedContrastSDK extendedContrastSDK = new ExtendedContrastSDK(usernameTextField.getText(), serviceKeyTextField.getText(),
                                apiKeyTextField.getText(), getTeamServerUrl(), proxy);

                        try {
                            Organizations orgs = extendedContrastSDK.getProfileOrganizations();

                            if (orgs != null && orgs.getOrganizations() != null && !orgs.getOrganizations().isEmpty()) {
                                for (Organization organization : orgs.getOrganizations()) {
                                    if (organization.getOrgUuid().equalsIgnoreCase(uuidTextField.getText())) {

                                        organizations.putIfAbsent(organization.getName(), getTeamServerUrl() +
                                                Constants.DELIMITER + usernameTextField.getText() + Constants.DELIMITER +
                                                serviceKeyTextField.getText() + Constants.DELIMITER +
                                                new String(apiKeyTextField.getPassword()) + Constants.DELIMITER + organization.getOrgUuid());

                                        String[] orgsArray = organizations.keySet().toArray(new String[organizations.keySet().size()]);
                                        organizationTableModel.setData(orgsArray);
                                        organizationTableModel.fireTableDataChanged();

                                        int indexOfSelectedOrgName = ArrayUtils.indexOf(orgsArray, organization.getName());
                                        organizationTable.setRowSelectionInterval(indexOfSelectedOrgName, indexOfSelectedOrgName);

                                        teamServerTextField.setText(Constants.TEAM_SERVER_URL_VALUE);
                                        usernameTextField.setText("");
                                        serviceKeyTextField.setText("");
                                        apiKeyTextField.setText("");
                                        uuidTextField.setText("");
                                        testConnectionLabel.setText("");

                                        break;
                                    }
                                }
                            }
                        } catch (IOException | UnauthorizedException e1) {
                            testConnectionLabel.setText("Connection failed! " + e1.getMessage());
                        } catch (Exception e1) {
                            testConnectionLabel.setText("Connection failed! Check Team Server URL.");
                        } finally {
                        }
                    }
                }).start();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedOrganization = getSelectedTableValue(organizationTable);
                if (selectedOrganization != null) {
                    if (organizations.get(selectedOrganization) != null) {
                        organizations.remove(selectedOrganization);
                    }

                    String[] newData = (String[]) ArrayUtils.removeElement(organizationTableModel.getData(), selectedOrganization);
                    organizationTableModel.setData(newData);
                    organizationTableModel.fireTableDataChanged();

                    if (newData.length > 0) {
                        organizationTable.setRowSelectionInterval(0, 0);
                    }
                }
            }
        });

        teamServerTextField.setText(Constants.TEAM_SERVER_URL_VALUE);
    }

    private String getTeamServerUrl() {
        String url = teamServerTextField.getText().trim();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        if (!url.endsWith("/Contrast/api")) {
            if (!url.endsWith("/Contrast")) {
                url += "/Contrast";
            }
            url += "/api";
        }
        return url;
    }

    public JPanel getContrastSettingsPanel() {
        return contrastSettingsPanel;
    }

    private void populateFieldsWithValuesFromContrastPersistentStateComponent() {
        Map<String, String> orgs = contrastPersistentStateComponent.getOrganizations();

        if (orgs.isEmpty()) {
            organizations = new HashMap<>();
            organizationTableModel.setData(new String[0]);
            organizationTableModel.fireTableDataChanged();
        } else if (!orgs.isEmpty()) {
//            Create a copy of organizations map from ContrastPersistentStateComponent class
//            It will be compared with the original in isModified() method
            organizations = new HashMap<>();
            organizations.putAll(orgs);

            String[] orgsArray = organizations.keySet().toArray(new String[organizations.keySet().size()]);
            organizationTableModel.setData(orgsArray);
            organizationTableModel.fireTableDataChanged();

            String selectedOrganization = organizations.get(contrastFilterPersistentStateComponent.getSelectedOrganizationName());
            if (StringUtils.isNotBlank(contrastFilterPersistentStateComponent.getSelectedOrganizationName())
                    && selectedOrganization != null) {
                // if selectedOrganization is not null, set it as selected in organizationTable
                String selectedOrgName = contrastFilterPersistentStateComponent.getSelectedOrganizationName();
                int indexOfSelectedOrgName = ArrayUtils.indexOf(orgsArray, selectedOrgName);
                organizationTable.setRowSelectionInterval(indexOfSelectedOrgName, indexOfSelectedOrgName);
            }
        }
    }

    public boolean isModified() {
        boolean modified = false;
        if (getSelectedTableValue(organizationTable) != null) {
            modified |= !getSelectedTableValue(organizationTable).equals(contrastFilterPersistentStateComponent.getSelectedOrganizationName());
        }
        modified |= !organizations.equals(contrastPersistentStateComponent.getOrganizations());
        return modified;
    }

    public void apply() {
        contrastPersistentStateComponent.setOrganizations(organizations);
        doChange();
    }

    private void doChange() {
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        ChangeActionNotifier publisher = bus.syncPublisher(ChangeActionNotifier.CHANGE_ACTION_TOPIC);
        publisher.beforeAction();
        try {
            contrastFilterPersistentStateComponent.setSelectedOrganizationName(getSelectedTableValue(organizationTable));
        } finally {
            publisher.afterAction();
        }
    }

    public void reset() {
        populateFieldsWithValuesFromContrastPersistentStateComponent();
    }

    private String getSelectedTableValue(JTable jTable) {
        if (jTable.getSelectedRow() == -1) {
            return null;
        }

        return (String) jTable.getValueAt(jTable.getSelectedRow(), 0);
    }
}
