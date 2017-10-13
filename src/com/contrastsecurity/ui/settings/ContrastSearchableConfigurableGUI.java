package com.contrastsecurity.ui.settings;

import com.contrastsecurity.config.ContrastPersistentStateComponent;
import com.contrastsecurity.core.Util;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;

public class ContrastSearchableConfigurableGUI {

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
    private JButton editButton;
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
//    Other variables
    private ContrastPersistentStateComponent contrastPersistentStateComponent;

    public JPanel getContrastSettingsPanel() {
        return contrastSettingsPanel;
    }

    public ContrastSearchableConfigurableGUI() {
        contrastPersistentStateComponent = ContrastPersistentStateComponent.getInstance();
    }

    private void populateFieldsWithValuesFromContrastPersistentStateComponent() {

        if (StringUtils.isNotBlank(contrastPersistentStateComponent.getTeamServerUrl())) {

        }
        if (StringUtils.isNotBlank(contrastPersistentStateComponent.getUsername())) {

        }
        if (StringUtils.isNotBlank(contrastPersistentStateComponent.getServiceKey())) {

        }
        if (contrastPersistentStateComponent.getOrganizations() != null && !contrastPersistentStateComponent.getOrganizations().isEmpty()){

        }
        if (StringUtils.isNotBlank(contrastPersistentStateComponent.getSelectedOrganizationUuid())){

        }

    }
}
