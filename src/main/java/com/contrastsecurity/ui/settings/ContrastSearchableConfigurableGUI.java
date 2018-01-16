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

import com.contrastsecurity.config.ContrastPersistentStateComponent;
import com.contrastsecurity.core.Constants;
import com.contrastsecurity.core.Util;
import com.contrastsecurity.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.core.internal.preferences.OrganizationConfig;
import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.models.Organization;
import com.contrastsecurity.models.Organizations;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ContrastSearchableConfigurableGUI {

    //    Other variables
    private final ContrastPersistentStateComponent contrastPersistentStateComponent;
    //    UI Variables
    private JPanel contrastSettingsPanel;
    private JLabel teamServerLabel;
    private JTextField teamServerTextField;
    private JTextField usernameTextField;
    private JLabel usernameLabel;
    private JTextField serviceKeyTextField;
    private JLabel serviceKeyLabel;
    private JComboBox organizationComboBox;
    private JLabel organizationLabel;
    private JButton addButton;
    private JButton deleteButton;
    private JButton testConnectionButton;
    private JLabel selectedOrganizationLabel;
    private JLabel apiKeyLabel;
    private JTextField apiKeyTextField;
    private JLabel uuidLabel;
    private JTextField uuidTextField;
    private JSeparator selectedOrganizationSeparator;
    private JSeparator generalSettingsSeparator;
    private JLabel generalSettingsLabel;
    private JLabel organizationSettingsLabel;
    private JButton restoreDefaultsButton;
    private JSeparator organizationSettingsSeparator;
    private JLabel testConnectionLabel;
    private Util util;
    private Map<String, String> organizations = new HashMap<>();

    public ContrastSearchableConfigurableGUI() {
        contrastPersistentStateComponent = ContrastPersistentStateComponent.getInstance();
        util = new Util();
        populateFieldsWithValuesFromContrastPersistentStateComponent();

        restoreDefaultsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ContrastSearchableConfigurableGUI.this.restoreDefaults();
            }
        });

        organizationComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.SELECTED) {
                    ContrastSearchableConfigurableGUI.this.setApiKeyAndUuidForSelectedOrganization();
                }
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ContrastDialog contrastDialog = new ContrastDialog(getTeamServerUrl(), usernameTextField.getText(), serviceKeyTextField.getText());
                contrastDialog.setVisible(true);

                Map<String, String> retrievedOrgs = contrastDialog.getOrganization();
                if (retrievedOrgs != null) {

                    for (String orgName : retrievedOrgs.keySet()) {
                        organizations.putIfAbsent(orgName, retrievedOrgs.get(orgName));
                    }
                    organizationComboBox.removeAllItems();
                    // populate organizationComboBox
                    for (String organizationName : organizations.keySet()) {
                        organizationComboBox.addItem(organizationName);
                    }
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (organizationComboBox.getSelectedItem() != null) {
                    String selectedItem = organizationComboBox.getSelectedItem().toString();
                    if (organizations.get(selectedItem) != null) {
                        organizations.remove(selectedItem);
                    }
                    organizationComboBox.removeItem(selectedItem);
                }
            }
        });

        testConnectionButton.addActionListener(new ActionListener() {
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
                        ExtendedContrastSDK extendedContrastSDK = new ExtendedContrastSDK(usernameTextField.getText(), serviceKeyTextField.getText(),
                                apiKeyTextField.getText(), getTeamServerUrl());
                        try {
                            Organizations organizations = extendedContrastSDK.getProfileDefaultOrganizations();
                            Organization organization = organizations.getOrganization();

                            if (organization == null || organization.getOrgUuid() == null) {
                                testConnectionLabel.setText("Connection is correct, but no default organizations found.");
                            } else {
                                testConnectionLabel.setText("Connection confirmed!");
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
    }

    private String getTeamServerUrl() {
        String url = teamServerTextField.getText();
        if (!url.endsWith("/api")) {
            url += "/api";
        }
        return url;
    }

    public JPanel getContrastSettingsPanel() {
        return contrastSettingsPanel;
    }

    private void populateFieldsWithValuesFromContrastPersistentStateComponent() {
        teamServerTextField.setText(contrastPersistentStateComponent.getTeamServerUrl());
        usernameTextField.setText(contrastPersistentStateComponent.getUsername());
        serviceKeyTextField.setText(contrastPersistentStateComponent.getServiceKey());

        Map<String, String> orgs = contrastPersistentStateComponent.getOrganizations();

        if (orgs.isEmpty()) {
            organizations = new HashMap<>();
            organizationComboBox.removeAllItems();
        } else if (!orgs.isEmpty()) {
//            Create a copy of organizations map from ContrastPersistentStateComponent class
//            It will be compared with the original in isModified() method
            organizations = new HashMap<>();
            organizations.putAll(orgs);

            organizationComboBox.removeAllItems();
            // populate organizationComboBox
            for (String organizationName : organizations.keySet()) {
                organizationComboBox.addItem(organizationName);
            }

            String selectedOrganization = organizations.get(contrastPersistentStateComponent.getSelectedOrganizationName());
            if (StringUtils.isNotBlank(contrastPersistentStateComponent.getSelectedOrganizationName())
                    && selectedOrganization != null) {
                // if selectedOrganization is not null, set it as selected in organizationComboBox
                organizationComboBox.setSelectedItem(contrastPersistentStateComponent.getSelectedOrganizationName());
                // populate apiKeyTextField and uuidTextField
                setApiKeyAndUuidForSelectedOrganization();
            }
        }
    }

    private void setApiKeyAndUuidForSelectedOrganization() {
        if (organizationComboBox.getSelectedItem() != null) {
            String selectedOrganization = organizations.get(organizationComboBox.getSelectedItem().toString());
            if (selectedOrganization != null) {
                OrganizationConfig organizationConfig = util.getOrganizationConfigFromString(selectedOrganization, Constants.DELIMITER);
                if (organizationConfig != null) {
                    apiKeyTextField.setText(organizationConfig.getApiKey());
                    uuidTextField.setText(organizationConfig.getUuid());
                }
            }
        }
    }

    public boolean isModified() {
        boolean modified = false;
        modified |= !getTeamServerUrl().equals(contrastPersistentStateComponent.getTeamServerUrl());
        modified |= !usernameTextField.getText().equals(contrastPersistentStateComponent.getUsername());
        modified |= !serviceKeyTextField.getText().equals(contrastPersistentStateComponent.getServiceKey());

        if (organizationComboBox.getSelectedItem() != null) {
            modified |= !organizationComboBox.getSelectedItem().toString().equals(contrastPersistentStateComponent.getSelectedOrganizationName());
        }

        modified |= !organizations.equals(contrastPersistentStateComponent.getOrganizations());
        return modified;
    }

    public void apply() {
        contrastPersistentStateComponent.setTeamServerUrl(getTeamServerUrl());
        contrastPersistentStateComponent.setUsername(usernameTextField.getText());
        contrastPersistentStateComponent.setServiceKey(serviceKeyTextField.getText());
        if (organizationComboBox.getSelectedItem() != null) {
            contrastPersistentStateComponent.setSelectedOrganizationName(organizationComboBox.getSelectedItem().toString());
        }
        contrastPersistentStateComponent.setOrganizations(organizations);
    }

    public void reset() {
        populateFieldsWithValuesFromContrastPersistentStateComponent();
    }

    private void restoreDefaults() {
        teamServerTextField.setText(Constants.TEAM_SERVER_URL_VALUE);
    }
}
