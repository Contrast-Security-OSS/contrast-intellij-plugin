package com.contrastsecurity.ui.settings;

import javax.swing.*;

public class ContrastSearchableConfigurableGUI {
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

    public JPanel getContrastSettingsPanel() {
        return contrastSettingsPanel;
    }
}
