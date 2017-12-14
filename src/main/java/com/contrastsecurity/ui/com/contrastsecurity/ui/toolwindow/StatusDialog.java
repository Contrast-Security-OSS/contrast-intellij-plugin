package com.contrastsecurity.ui.com.contrastsecurity.ui.toolwindow;

import com.contrastsecurity.core.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StatusDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox statusComboBox;
    private JComboBox reasonComboBox;
    private JTextArea commentTextArea;

    private String status;
    private String reason;
    private String comment;

    public StatusDialog() {
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

        setSize(700, 300);
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final int x = (screenSize.width - getWidth()) / 2;
        final int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
        setTitle("Mark as");

        resetComboBoxes();

        statusComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String status = statusComboBox.getSelectedItem().toString();
                if (status.equals(Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM_COMBO_BOX_ITEM)) {
                    reasonComboBox.setEnabled(true);
                    buttonOK.setEnabled(false);
                } else {
                    reasonComboBox.setEnabled(false);
                    buttonOK.setEnabled(true);
                }
            }
        });

        reasonComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String status = statusComboBox.getSelectedItem().toString();
                if (status.equals(Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM_COMBO_BOX_ITEM) && !buttonOK.isEnabled()) {
                    buttonOK.setEnabled(true);
                }
            }
        });
    }

    public static void main(String[] args) {
        StatusDialog dialog = new StatusDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void onOK() {
        // add your code here
        status = statusComboBox.getSelectedItem().toString();
        reason = reasonComboBox.getSelectedItem().toString();
        comment = commentTextArea.getText();
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void resetComboBoxes() {
        statusComboBox.removeAllItems();
        for (String status : Constants.STATUS_ARRAY) {
            statusComboBox.addItem(status);
        }
        reasonComboBox.removeAllItems();
        for (String reason : Constants.REASON_ARRAY) {
            reasonComboBox.addItem(reason);
        }

        reasonComboBox.setEnabled(false);
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getComment() {
        return comment;
    }
}
