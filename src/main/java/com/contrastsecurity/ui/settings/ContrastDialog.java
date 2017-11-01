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

import com.contrastsecurity.core.Constants;
import com.contrastsecurity.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.models.Organization;
import com.contrastsecurity.models.Organizations;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ContrastDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField apiKeyTextField;
    private JLabel apiKeyLabel;
    private JComboBox organizationNameComboBox;
    private JLabel organizationNameLabel;
    private JTextField organizationUuidTextField;
    private JLabel organizationUuidLabel;
    private JButton retrieveOrganizationsButton;

//    other variables
    private ExtendedContrastSDK extendedContrastSDK;
    private String teamserverUrl;
    private String username;
    private String serviceKey;
    Organizations organizations;

    public ContrastDialog(String teamserverUrl, String username, String serviceKey) {
        this.teamserverUrl = teamserverUrl;
        this.username = username;
        this.serviceKey = serviceKey;

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

        setSize(600, 200);
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final int x = (screenSize.width - getWidth()) / 2;
        final int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
        setTitle("Add organization");

        retrieveOrganizationsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        extendedContrastSDK = new ExtendedContrastSDK(username, serviceKey, apiKeyTextField.getText(), teamserverUrl);

                        try {
                            organizations = extendedContrastSDK.getProfileOrganizations();
                            organizationNameComboBox.removeAllItems();
                            for (Organization organization : organizations.getOrganizations()){
                                organizationNameComboBox.addItem(organization.getName());
                            }
                            buttonOK.setEnabled(true);

                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (UnauthorizedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        organizationNameComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.SELECTED) {
                    String selectedOrganizationName = ContrastDialog.this.organizationNameComboBox.getSelectedItem().toString();
                    for (Organization organization : ContrastDialog.this.organizations.getOrganizations()) {
                        if (organization.getName().equals(selectedOrganizationName)) {
                            ContrastDialog.this.organizationUuidTextField.setText(organization.getOrgUuid());
                            break;
                        }
                    }
                }
            }
        });

        apiKeyTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                ContrastDialog.this.buttonOK.setEnabled(false);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                ContrastDialog.this.buttonOK.setEnabled(false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                ContrastDialog.this.buttonOK.setEnabled(false);
            }
        });

    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public Map<String, String> getOrganization(){
        if (buttonOK.isEnabled()) {
            Map<String, String> orgs = new HashMap<>();
            orgs.put(organizationNameComboBox.getSelectedItem().toString(), apiKeyTextField.getText() + Constants.DELIMITER + organizationUuidTextField.getText());
            return orgs;
        } else {
            return null;
        }
    }

//    public static void main(String[] args) {
//        ContrastDialog dialog = new ContrastDialog();
//        dialog.pack();
//        dialog.setVisible(true);
//        System.exit(0);
//    }
}
