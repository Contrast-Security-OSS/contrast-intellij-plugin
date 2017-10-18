package com.contrastsecurity.ui;

import com.contrastsecurity.core.Constants;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

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

    public ContrastToolWindowFactory() {
        setupVulnerabilitiesTable();
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        contrastToolWindow = toolWindow;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(contrastToolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private void setupVulnerabilitiesTable() {
        TableColumn severity = new TableColumn();
        severity.setHeaderValue("Severity");

        TableColumn vulnerability = new TableColumn();
        vulnerability.setHeaderValue("Vulnerability");

        TableColumn actions = new TableColumn();
        actions.setHeaderValue("Actions");

        TableColumn viewInBrowser = new TableColumn();

        vulnerabilitiesTable.addColumn(severity);
        vulnerabilitiesTable.addColumn(vulnerability);
        vulnerabilitiesTable.addColumn(actions);
        vulnerabilitiesTable.addColumn(viewInBrowser);

    }
}
