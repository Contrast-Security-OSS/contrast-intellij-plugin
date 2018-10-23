/***************************************************************************
 Copyright (c) 2017 Contrast Security.
 All rights reserved.

 This program and the accompanying materials are made available under
 the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation; either version 3 of the License.

 The terms of the GNU GPL version 3 which accompanies this distribution
 and is available at https://www.gnu.org/licenses/gpl-3.0.en.html

 Contributors:
 Contrast Security - initial API and implementation
 */
package com.contrastsecurity.ui.com.contrastsecurity.ui.toolwindow;

import com.contrastsecurity.config.ContrastFilterPersistentStateComponent;
import com.contrastsecurity.config.ContrastPersistentStateComponent;
import com.contrastsecurity.config.ContrastUtil;
import com.contrastsecurity.core.Constants;
import com.contrastsecurity.core.Util;
import com.contrastsecurity.core.cache.ContrastCache;
import com.contrastsecurity.core.cache.Key;
import com.contrastsecurity.core.extended.*;
import com.contrastsecurity.core.internal.preferences.OrganizationConfig;
import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.RuleSeverity;
import com.contrastsecurity.http.ServerFilterForm;
import com.contrastsecurity.http.TraceFilterForm;
import com.contrastsecurity.models.*;
import com.contrastsecurity.ui.settings.ContrastSearchableConfigurable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import icons.ContrastPluginIcons;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.unbescape.html.HtmlEscape;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

public class ContrastToolWindowFactory implements ToolWindowFactory {

    private static final int PAGE_LIMIT = 20;
    private JPanel contrastToolWindowContent;
    private JTable vulnerabilitiesTable;
    private JPanel cardPanel;
    private JPanel noVulnerabilitiesPanel;
    private JPanel vulnerabilityDetailsPanel;
    private JLabel traceSeverityLabel;
    private JButton externalLinkButton;
    private JButton backToResultsButton;
    private JTabbedPane tabbedPane1;
    private JTextPane httpRequestTextPane;
    private JPanel mainCard;
    private JTree eventsTree;
    private JComponent jComponent;
    private JButton previousPageButton;
    private JButton nextPageButton;
    private JLabel pageLabel;
    private JLabel numOfPagesLabel;
    private JButton firstPageButton;
    private JButton lastPageButton;
    private JComboBox<String> pagesComboBox;
    private JButton nextTraceButton;
    private JButton previousTraceButton;
    private JLabel currentTraceDetailsLabel;
    private JLabel tracesCountLabel;
    private JPanel recommendationPanel;
    private JButton tagButton;
    private JPanel overviewPanel;
    private JButton markAsButton;
    private JTextPane traceTitleTextPane;
    private ContrastUtil contrastUtil;
    private ExtendedContrastSDK extendedContrastSDK;
    private ContrastTableModel contrastTableModel = new ContrastTableModel();
    private ContrastTableRowSorter contrastTableRowSorter = new ContrastTableRowSorter(contrastTableModel);
    private OrganizationConfig organizationConfig;
    private ContrastFilterPersistentStateComponent contrastFilterPersistentStateComponent;
    private Trace viewDetailsTrace;
    private TagsResource viewDetailsTraceTagsResource;
    private TagsResource orgTagsResource;
    private TraceFilterForm traceFilterForm;
    private int numOfPages = 1;
    private Servers servers;
    private List<Application> applications;
    private ContrastCache contrastCache;

    private ActionListener pagesComboBoxActionListener;
    private int selectedTraceRow;

    public ContrastToolWindowFactory() {
        externalLinkButton.setIcon(ContrastPluginIcons.EXTERNAL_LINK_ICON);
        firstPageButton.setIcon(ContrastPluginIcons.FIRST_PAGE_ICON);
        lastPageButton.setIcon(ContrastPluginIcons.LAST_PAGE_ICON);
        previousPageButton.setIcon(ContrastPluginIcons.PREVIOUS_PAGE_ICON);
        nextPageButton.setIcon(ContrastPluginIcons.NEXT_PAGE_ICON);
        previousTraceButton.setIcon(ContrastPluginIcons.PREVIOUS_PAGE_ICON);
        nextTraceButton.setIcon(ContrastPluginIcons.NEXT_PAGE_ICON);
        tagButton.setIcon(ContrastPluginIcons.TAG_ICON);

        recommendationPanel.setLayout(new BoxLayout(recommendationPanel, BoxLayout.Y_AXIS));
        overviewPanel.setLayout(new BoxLayout(overviewPanel, BoxLayout.Y_AXIS));

        pagesComboBoxActionListener = e -> goToPage(Integer.valueOf(pagesComboBox.getSelectedItem().toString()), true);

        MouseListener treeNodeClickListener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = eventsTree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = eventsTree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1 && e.getClickCount() == 2 && selPath.getPathCount() > 0) {
                    Object selectedObject = ((DefaultMutableTreeNode) selPath.getLastPathComponent()).getUserObject();
                    if (selectedObject instanceof EventItem) {
                        EventItem eventItem = (EventItem) selectedObject;
                        if (eventItem.isStacktrace()) {
                            String typeName = getTypeName(eventItem.getValue());
                            Integer lineNumber = getLineNumber(eventItem.getValue());

                            if (typeName != null) {

                                Project project = ProjectManager.getInstance().getOpenProjects()[0];
                                JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
                                GlobalSearchScope globalSearchScope = GlobalSearchScope.allScope(project);

                                if (eventItem.getValue().contains(".java")) {
                                    PsiClass[] psiClasses = javaPsiFacade.findClasses(typeName, globalSearchScope);
                                    if (psiClasses.length > 0) {
                                        for (PsiClass psiClass : psiClasses) {
                                            PsiJavaFile javaFile = (PsiJavaFile) psiClass.getContainingFile();
                                            if (lineNumber != null) {
                                                new OpenFileDescriptor(project, javaFile.getVirtualFile(), lineNumber - 1, 0).navigate(true);
                                            } else {
                                                new OpenFileDescriptor(project, javaFile.getVirtualFile(), 0, 0).navigate(true);
                                            }
                                        }
                                    } else {
                                        MessageDialog messageDialog = new MessageDialog("Not found", "Source not found for " + typeName);
                                        messageDialog.setVisible(true);

                                    }

                                } else {
                                    PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, typeName, globalSearchScope);
                                    if (psiFiles.length > 0) {
                                        for (PsiFile psiFile : psiFiles) {
                                            if (lineNumber != null) {
                                                new OpenFileDescriptor(project, psiFile.getVirtualFile(), lineNumber - 1, 0).navigate(true);
                                            } else {
                                                new OpenFileDescriptor(project, psiFile.getVirtualFile(), 0, 0).navigate(true);
                                            }
                                        }
                                    } else {
                                        MessageDialog messageDialog = new MessageDialog("Not found", "Source not found for " + typeName);
                                        messageDialog.setVisible(true);
                                    }
                                }
                            } else {
                                MessageDialog messageDialog = new MessageDialog("Not found", "Source not found for " + eventItem.getValue());
                                messageDialog.setVisible(true);
                            }
                        }
                    }
                }
            }
        };

        EventTreeCellRenderer eventTreeCellRenderer = new EventTreeCellRenderer();
        eventsTree.setCellRenderer(eventTreeCellRenderer);
        eventsTree.addMouseListener(treeNodeClickListener);

        contrastFilterPersistentStateComponent = ContrastFilterPersistentStateComponent.getInstance();

        backToResultsButton.addActionListener(e -> {
            viewDetailsTrace = null;

            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, "mainCard");
        });


        externalLinkButton.addActionListener(e -> openWebpage(viewDetailsTrace));

        firstPageButton.addActionListener(e -> new Thread(() -> goToPage(1, false)).start());

        lastPageButton.addActionListener(e -> new Thread(() -> goToPage(numOfPages, false)).start());

        previousPageButton.addActionListener(e -> new Thread(() -> goToPage(Integer.valueOf(pageLabel.getText()) - 1, false)).start());

        nextPageButton.addActionListener(e -> new Thread(() -> goToPage(Integer.valueOf(pageLabel.getText()) + 1, false)).start());

        previousTraceButton.addActionListener(e -> {
            if (selectedTraceRow > 0) {
                boolean cont = true;
                int currentRow = selectedTraceRow - 1;
                while (currentRow >= 0 && cont) {
                    Trace trace = contrastTableModel.getTraceAtRow(currentRow);
                    if (contrastUtil.isTraceLicensed(trace)) {
                        viewDetailsTrace = trace;
                        cont = false;
                        selectedTraceRow = currentRow;
                        populateVulnerabilityDetailsPanel();
                        vulnerabilitiesTable.setRowSelectionInterval(selectedTraceRow, selectedTraceRow);
                    } else {
                        currentRow--;
                    }
                }
            }
        });

        nextTraceButton.addActionListener(e -> {
            int rowCount = contrastTableModel.getRowCount();
            if (selectedTraceRow < rowCount) {
                boolean cont = true;
                int currentRow = selectedTraceRow + 1;

                while (currentRow < rowCount && cont) {
                    Trace trace = contrastTableModel.getTraceAtRow(currentRow);
                    if (contrastUtil.isTraceLicensed(trace)) {
                        viewDetailsTrace = trace;
                        cont = false;
                        selectedTraceRow = currentRow;
                        populateVulnerabilityDetailsPanel();
                        vulnerabilitiesTable.setRowSelectionInterval(selectedTraceRow, selectedTraceRow);
                    } else {
                        currentRow++;
                    }
                }
            }
        });

        tagButton.addActionListener(e -> {
            if (viewDetailsTraceTagsResource != null && orgTagsResource != null) {
                TagDialog tagDialog = new TagDialog(viewDetailsTraceTagsResource, orgTagsResource);
                tagDialog.setVisible(true);

                List<String> newTraceTags = tagDialog.getNewTraceTags();
                if (newTraceTags != null) {
                    Key key = new Key(contrastUtil.getSelectedOrganizationConfig().getUuid(), viewDetailsTrace.getUuid());
                    Key keyForOrg = new Key(contrastUtil.getSelectedOrganizationConfig().getUuid(), null);
                    boolean tagsChanged = false;
//                        remove tags if necessary
                    for (String tag : viewDetailsTraceTagsResource.getTags()) {
                        if (!newTraceTags.contains(tag)) {
                            try {
                                contrastToolWindowContent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                extendedContrastSDK.deleteTag(contrastUtil.getSelectedOrganizationConfig().getUuid(), viewDetailsTrace.getUuid(), tag);
                                if (!tagsChanged) {
                                    tagsChanged = true;
                                }
                            } catch (IOException | UnauthorizedException e1) {
                                e1.printStackTrace();
                            } finally {
                                contrastToolWindowContent.setCursor(Cursor.getDefaultCursor());
                            }
                        }
                    }
//                        add tags if necessary
                    List<String> tagsToAdd = new ArrayList<>();
                    for (String tag : newTraceTags) {
                        if (!viewDetailsTraceTagsResource.getTags().contains(tag)) {
                            tagsToAdd.add((tag));
                        }
                    }
                    if (!tagsToAdd.isEmpty()) {
                        List<String> tracesId = new ArrayList<>();
                        tracesId.add(viewDetailsTrace.getUuid());
                        TagsServersResource tagsServersResource = new TagsServersResource(tagsToAdd, tracesId);
                        try {
                            contrastToolWindowContent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            extendedContrastSDK.putTags(contrastUtil.getSelectedOrganizationConfig().getUuid(), tagsServersResource);
                            if (!tagsChanged) {
                                tagsChanged = true;
                            }
                        } catch (IOException | UnauthorizedException e1) {
                            e1.printStackTrace();
                        } finally {
                            contrastToolWindowContent.setCursor(Cursor.getDefaultCursor());
                        }
                    }
                    if (tagsChanged) {
                        contrastCache.getTagsResources().remove(key);
                        contrastCache.getTagsResources().remove(keyForOrg);
                        try {
                            viewDetailsTraceTagsResource = getTags(key);
                            orgTagsResource = getTags(keyForOrg);
                        } catch (IOException | UnauthorizedException e1) {
                            e1.printStackTrace();
                        }

                    }
                }
            }
        });

        markAsButton.addActionListener(e -> {
            StatusDialog statusDialog = new StatusDialog();
            statusDialog.setVisible(true);
            String status = statusDialog.getStatus();
            if (status != null) {
                StatusRequest statusRequest = new StatusRequest();

                if (status.equals(Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM_COMBO_BOX_ITEM)) {
                    statusRequest.setStatus(Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM_API_REQUEST_STRING);
                    String reason = statusDialog.getReason();
                    statusRequest.setSubstatus(reason);
                    statusRequest.setCommentPreference(false);
                } else {
                    statusRequest.setStatus(status);
                }

                String comment = statusDialog.getComment();
                if (!comment.isEmpty()) {
                    statusRequest.setNote(comment);
                    if (!status.equals(Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM_COMBO_BOX_ITEM)) {
                        statusRequest.setCommentPreference(true);
                    }
                }
                List<String> traces = new ArrayList<>();
                traces.add(viewDetailsTrace.getUuid());
                statusRequest.setTraces(traces);

                new Thread(() -> {
                    try {
                        extendedContrastSDK.putStatus(contrastUtil.getSelectedOrganizationConfig().getUuid(), statusRequest);
                        refreshTraces(false);
                    } catch (IOException | UnauthorizedException e1) {
                        e1.printStackTrace();
                    }
                }).start();
            }

        });
        RowSorterListener contrastRowSorterListener = e -> {

            if (contrastTableModel != null && contrastTableModel.getRowCount() > 1 && contrastTableRowSorter.getColumnToSort() != null) {
                String name = contrastTableRowSorter.getColumnToSort().substring(1);
                String sortOrder = contrastTableRowSorter.getColumnToSort().substring(0, 1);
                String sort = "";

                if (sortOrder.equals(Constants.SORT_DESCENDING)) {
                    sort += Constants.SORT_DESCENDING;
                }

                switch (name) {
                    case "Severity":
                        sort += Constants.SORT_BY_SEVERITY;
                        break;
                    case "Vulnerability":
                        sort += Constants.SORT_BY_TITLE;
                        break;
                    case "Last Detected":
                        sort += Constants.SORT_BY_LAST_TIME_SEEN;
                        break;
                    case "Status":
                        sort += Constants.SORT_BY_STATUS;
                        break;
                    case "Application":
                        sort += Constants.SORT_BY_APPLICATION_NAME;
                        break;
                }
                if (sort.length() > 1) {
                    String finalSort = sort;
                    new Thread(() -> {
                        traceFilterForm.setSort(finalSort);
                        refreshTraces(false);
                        contrastFilterPersistentStateComponent.setSort(finalSort);
                    }).start();
                }
            }
        };
        contrastTableRowSorter.addRowSorterListener(contrastRowSorterListener);

        setupTable();
        updateOrganizationConfig();
        refresh();
    }

    private void updateOrganizationConfig() {
        ContrastPersistentStateComponent contrastPersistentStateComponent = ContrastPersistentStateComponent.getInstance();
        Map<String, String> organizationMap = contrastPersistentStateComponent.getOrganizations();
        for (Map.Entry<String, String> entry : organizationMap.entrySet()) {
            String organizationString = entry.getValue();

            String[] org = StringUtils.split(organizationString, Constants.DELIMITER);
            if (org.length == 2) {
                String teamServerUrl = contrastPersistentStateComponent.getTeamServerUrl();
                String username = contrastPersistentStateComponent.getUsername();
                String serviceKey = contrastPersistentStateComponent.getServiceKey();

                OrganizationConfig organizationConfig = new OrganizationConfig(teamServerUrl, username, serviceKey, org[0], org[1]);
                String newOrganizationString = Util.getStringFromOrganizationConfig(organizationConfig, Constants.DELIMITER);

                entry.setValue(newOrganizationString);
            }
        }
    }

    private void goToPage(final int page, final boolean userUpdatedPagesComboBoxSelection) {
        int currentOffset = PAGE_LIMIT * (page - 1);
        traceFilterForm.setOffset(currentOffset);

        contrastFilterPersistentStateComponent.setPage(page);
        contrastFilterPersistentStateComponent.setCurrentOffset(currentOffset);

        refreshTraces(userUpdatedPagesComboBoxSelection);
    }

    private String getTypeName(String stacktrace) {
        int start = stacktrace.lastIndexOf('(');
        int end = stacktrace.indexOf(':');
        if (start >= 0 && end > start) {
            String typeName = stacktrace.substring(start + 1, end);
            int indexOfExtension = typeName.indexOf(".java");
            if (indexOfExtension > 0) {
                typeName = typeName.substring(0, indexOfExtension);
            }

            String qualifier = stacktrace.substring(0, start);
            start = qualifier.lastIndexOf('.');
            if (start >= 0) {
                start = ((String) qualifier.subSequence(0, start)).lastIndexOf('.');
                if (start == -1) {
                    start = 0;
                }
            }
            if (start >= 0) {
                qualifier = qualifier.substring(0, start);
            }
            if (qualifier.length() > 0) {
                typeName = qualifier + "." + typeName;
            }
            return typeName;
        }
        return null;
    }

    private Integer getLineNumber(String stacktrace) {
        int index = stacktrace.lastIndexOf(':');
        if (index >= 0) {
            String numText = stacktrace.substring(index + 1);
            index = numText.indexOf(')');
            if (index >= 0) {
                numText = numText.substring(0, index);
            }
            try {
                return Integer.parseInt(numText);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Date getDateFromLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        } else {
            return null;
        }
    }

    private void refresh() {
        contrastUtil = new ContrastUtil();
        extendedContrastSDK = contrastUtil.getContrastSDK();
        organizationConfig = contrastUtil.getSelectedOrganizationConfig();
        traceFilterForm = getTraceFilterFormFromContrastFilterPersistentStateComponent();

        if (organizationConfig != null) {
            new Thread(() -> {
                refreshTraces(false);
                servers = retrieveServers();
                applications = retrieveApplications();
            }).start();

        } else {
            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
            cardLayout.show(cardPanel, "noVulnerabilitiesCard");
        }
        contrastCache = contrastUtil.getContrastCache();
    }

    private void refreshTraces(final boolean userUpdatedPagesComboBoxSelection) {

        contrastToolWindowContent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        vulnerabilitiesTable.getTableHeader().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        Trace[] traces = new Trace[0];

        Long serverId = Constants.ALL_SERVERS;
        String appId = Constants.ALL_APPLICATIONS;

        if (contrastFilterPersistentStateComponent.getSelectedServerUuid() != null) {
            serverId = contrastFilterPersistentStateComponent.getSelectedServerUuid();
        }
        if (contrastFilterPersistentStateComponent.getSelectedApplicationId() != null) {
            appId = contrastFilterPersistentStateComponent.getSelectedApplicationId();
        }

        try {
            Traces tracesObject = getTraces(organizationConfig.getUuid(), serverId, appId, traceFilterForm);

            if (tracesObject != null && tracesObject.getTraces() != null && !tracesObject.getTraces().isEmpty()) {
                traces = tracesObject.getTraces().toArray(new Trace[0]);
            }
            if (!mainCard.isVisible() && !vulnerabilityDetailsPanel.isVisible()) {
                CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
                cardLayout.show(cardPanel, "mainCard");
            }
            if (contrastFilterPersistentStateComponent.getPage() != null) {
                pageLabel.setText(String.valueOf(contrastFilterPersistentStateComponent.getPage()));
            } else {
                pageLabel.setText("1");
            }

            numOfPages = getNumOfPages(tracesObject.getCount());
            numOfPagesLabel.setText("/" + numOfPages);
            updatePageButtons();

            if (!userUpdatedPagesComboBoxSelection) {
                pagesComboBox.removeActionListener(pagesComboBoxActionListener);
            }
            updatePagesComboBox(numOfPages);
            pagesComboBox.setSelectedItem(String.valueOf(contrastFilterPersistentStateComponent.getPage()));

            if (!userUpdatedPagesComboBoxSelection) {
                pagesComboBox.addActionListener(pagesComboBoxActionListener);
            }


        } catch (IOException | UnauthorizedException exception) {
            exception.printStackTrace();
            if (!noVulnerabilitiesPanel.isVisible()) {
                CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
                cardLayout.show(cardPanel, "noVulnerabilitiesCard");
            }
        }
        contrastTableModel.setData(traces);
        contrastTableModel.fireTableDataChanged();

        contrastToolWindowContent.setCursor(Cursor.getDefaultCursor());
        vulnerabilitiesTable.getTableHeader().setCursor(Cursor.getDefaultCursor());
    }

    private void updatePageButtons() {
        int newPage = Integer.valueOf(pageLabel.getText());
        if (newPage == 1) {
            previousPageButton.setEnabled(false);
            firstPageButton.setEnabled(false);
        } else {
            previousPageButton.setEnabled(true);
            firstPageButton.setEnabled(true);
        }

        if (newPage == numOfPages) {
            nextPageButton.setEnabled(false);
            lastPageButton.setEnabled(false);
        } else {
            nextPageButton.setEnabled(true);
            lastPageButton.setEnabled(true);
        }
    }

    private void updatePagesComboBox(final int numOfPages) {
        pagesComboBox.removeAllItems();
        for (int i = 1; i <= numOfPages; i++) {
            pagesComboBox.addItem(String.valueOf(i));
        }
        if (numOfPages == 1) {
            pagesComboBox.setEnabled(false);
        } else {
            pagesComboBox.setEnabled(true);
        }
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(contrastToolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private Traces getTraces(String orgUuid, Long serverId, String appId, TraceFilterForm form)
            throws IOException, UnauthorizedException {

        Traces traces = null;
        if (extendedContrastSDK != null) {
            if (serverId == Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
                traces = extendedContrastSDK.getTracesInOrg(orgUuid, form);
            } else if (serverId == Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
                traces = extendedContrastSDK.getTraces(orgUuid, appId, form);
            } else if (serverId != Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
                traces = extendedContrastSDK.getTracesInOrg(orgUuid, form);
            } else if (serverId != Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
                traces = extendedContrastSDK.getTraces(orgUuid, appId, form);
            }
        }
        return traces;
    }

    private void setupTable() {
        vulnerabilitiesTable.setModel(contrastTableModel);
        vulnerabilitiesTable.setRowSorter(contrastTableRowSorter);
        TableColumn severityColumn = vulnerabilitiesTable.getColumnModel().getColumn(0);
        severityColumn.setMaxWidth(76);
        severityColumn.setMinWidth(76);

        TableColumn openInTeamserverColumn = vulnerabilitiesTable.getColumnModel().getColumn(5);
        openInTeamserverColumn.setMaxWidth(120);

        vulnerabilitiesTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                Point point = e.getPoint();
                int row = vulnerabilitiesTable.rowAtPoint(point);
                int col = vulnerabilitiesTable.columnAtPoint(point);

                if (row >= 0 && col >= 0) {
                    String name = vulnerabilitiesTable.getColumnName(col);

                    if (e.getClickCount() == 2 && !name.equals("")) {
                        Trace traceClicked = contrastTableModel.getTraceAtRow(row);
                        if (contrastUtil.isTraceLicensed(traceClicked)) {
                            selectedTraceRow = row;
                            viewDetailsTrace = traceClicked;
                            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
                            cardLayout.show(cardPanel, "vulnerabilityDetailsCard");
                            populateVulnerabilityDetailsPanel();
                            tabbedPane1.setSelectedIndex(2);
                        } else {
                            MessageDialog messageDialog = new MessageDialog(Constants.UNLICENSED_DIALOG_TITLE, Constants.UNLICENSED_DIALOG_MESSAGE);
                            messageDialog.setVisible(true);
                        }
                    }
                }
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = vulnerabilitiesTable.rowAtPoint(evt.getPoint());
                int col = vulnerabilitiesTable.columnAtPoint(evt.getPoint());

                if (row >= 0 && col >= 0) {
                    String name = vulnerabilitiesTable.getColumnName(col);
                    if (name.equals("")) {
                        Trace traceClicked = contrastTableModel.getTraceAtRow(row);
                        openWebpage(traceClicked);
                    }
                }
            }
        });
    }

    private void populateVulnerabilityDetailsPanel() {

        new Thread(() -> {
            resetVulnerabilityDetails();

            String severity = viewDetailsTrace.getSeverity();
            switch (severity) {
                case Constants.SEVERITY_LEVEL_NOTE:
                    traceSeverityLabel.setIcon(ContrastPluginIcons.SEVERITY_ICON_NOTE);
                    break;
                case Constants.SEVERITY_LEVEL_LOW:
                    traceSeverityLabel.setIcon(ContrastPluginIcons.SEVERITY_ICON_LOW);
                    break;
                case Constants.SEVERITY_LEVEL_MEDIUM:
                    traceSeverityLabel.setIcon(ContrastPluginIcons.SEVERITY_ICON_MEDIUM);
                    break;
                case Constants.SEVERITY_LEVEL_HIGH:
                    traceSeverityLabel.setIcon(ContrastPluginIcons.SEVERITY_ICON_HIGH);
                    break;
                case Constants.SEVERITY_LEVEL_CRITICAL:
                    traceSeverityLabel.setIcon(ContrastPluginIcons.SEVERITY_ICON_CRITICAL);
                    break;
            }

            String title = viewDetailsTrace.getTitle();
            int indexOfUnlicensed = title.indexOf(Constants.UNLICENSED);
            if (indexOfUnlicensed != -1) {
                title = "UNLICENSED - " + title.substring(0, indexOfUnlicensed);
            }
            traceTitleTextPane.setText(title);

            try {
                Key key = new Key(contrastUtil.getSelectedOrganizationConfig().getUuid(), viewDetailsTrace.getUuid());
                Key keyForOrg = new Key(contrastUtil.getSelectedOrganizationConfig().getUuid(), null);

                contrastToolWindowContent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                StoryResource storyResource = getStory(key);
                HttpRequestResource httpRequestResource = getHttpRequest(key);
                EventSummaryResource eventSummaryResource = getEventSummary(key);
                RecommendationResource recommendationResource = getRecommendationResource(key);

                viewDetailsTraceTagsResource = getTags(key);
                orgTagsResource = getTags(keyForOrg);

                populateVulnerabilityDetailsOverview(storyResource);
                populateVulnerabilityDetailsEvents(eventSummaryResource);
                populateVulnerabilityDetailsHttpRequest(httpRequestResource);
                populateVulnerabilityDetailsRecommendation(recommendationResource);
            } catch (IOException | UnauthorizedException e) {
                e.printStackTrace();
            } finally {
                contrastToolWindowContent.setCursor(Cursor.getDefaultCursor());
            }
            currentTraceDetailsLabel.setText(String.valueOf(selectedTraceRow + 1));
            tracesCountLabel.setText(String.valueOf(contrastTableModel.getRowCount()));
        }).start();
    }

    private URL getOverviewUrl(String traceId) throws MalformedURLException {
        String teamServerUrl = contrastUtil.getSelectedOrganizationConfig().getTeamServerUrl();
        teamServerUrl = teamServerUrl.trim();
        if (teamServerUrl.endsWith("/api")) {
            teamServerUrl = teamServerUrl.substring(0, teamServerUrl.length() - 4);
        }
        if (teamServerUrl.endsWith("/api/")) {
            teamServerUrl = teamServerUrl.substring(0, teamServerUrl.length() - 5);
        }
        String urlStr = teamServerUrl + "/static/ng/index.html#/" + contrastUtil.getSelectedOrganizationConfig().getUuid() + "/vulns/" + traceId + "/overview";
        return new URL(urlStr);
    }

    private void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openWebpage(Trace trace) {
        if (trace == null) {
            return;
        }
        // https://apptwo.contrastsecurity.com/Contrast/static/ng/index.html#/orgUuid/vulns/<VULN_ID>/overview
        try {
            URL url = getOverviewUrl(trace.getUuid());
            openWebpage(url.toURI());
        } catch (URISyntaxException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private LocalDateTime getLocalDateTimeFromMillis(Long millis) {
        Date date = new Date(millis);
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private StoryResource getStory(Key key) throws IOException, UnauthorizedException {
        StoryResource story = contrastCache.getStoryResources().get(key);

        if (story == null) {
            story = extendedContrastSDK.getStory(key.getOrgUuid(), key.getTraceId());
            contrastCache.getStoryResources().put(key, story);
        }

        return story;
    }

    private EventSummaryResource getEventSummary(Key key) throws IOException, UnauthorizedException {

        EventSummaryResource eventSummaryResource = contrastCache.getEventSummaryResources().get(key);
        if (eventSummaryResource == null) {
            eventSummaryResource = extendedContrastSDK.getEventSummary(key.getOrgUuid(), key.getTraceId());
            contrastCache.getEventSummaryResources().put(key, eventSummaryResource);
        }
        return eventSummaryResource;
    }

    private TagsResource getTags(Key key) throws IOException, UnauthorizedException {
        TagsResource tagsResource = contrastCache.getTagsResources().get(key);

        if (tagsResource == null) {
            if (key.getTraceId() != null) {
                tagsResource = extendedContrastSDK.getTagsByTrace(key.getOrgUuid(), key.getTraceId());
            } else {
                tagsResource = extendedContrastSDK.getTagsByOrg(key.getOrgUuid());
            }
            contrastCache.getTagsResources().put(key, tagsResource);
        }
        return tagsResource;
    }

    private HttpRequestResource getHttpRequest(Key key) throws IOException, UnauthorizedException {

        HttpRequestResource httpRequestResource = contrastCache.getHttpRequestResources().get(key);
        if (httpRequestResource == null) {
            httpRequestResource = extendedContrastSDK.getHttpRequest(key.getOrgUuid(), key.getTraceId());
            contrastCache.getHttpRequestResources().put(key, httpRequestResource);
        }
        return httpRequestResource;
    }

    private RecommendationResource getRecommendationResource(Key key) throws IOException, UnauthorizedException {

        RecommendationResource recommendationResource = contrastCache.getRecommendationResources().get(key);
        if (recommendationResource == null) {
            recommendationResource = extendedContrastSDK.getRecommendation(key.getOrgUuid(), key.getTraceId());
            contrastCache.getRecommendationResources().put(key, recommendationResource);
        }
        return recommendationResource;
    }

    private void populateVulnerabilityDetailsOverview(StoryResource storyResource) {
        if (storyResource != null && storyResource.getStory() != null && storyResource.getStory().getChapters() != null
                && !storyResource.getStory().getChapters().isEmpty()) {

            overviewPanel.removeAll();

            insertHeaderTextIntoPanel(Constants.TRACE_STORY_HEADER_CHAPTERS, overviewPanel);

            for (Chapter chapter : storyResource.getStory().getChapters()) {
                String text = chapter.getIntroText() == null ? Constants.BLANK : chapter.getIntroText();
                String areaText = chapter.getBody() == null ? Constants.BLANK : chapter.getBody();
                if (areaText.isEmpty()) {
                    List<PropertyResource> properties = chapter.getPropertyResources();
                    if (properties != null && properties.size() > 0) {
                        Iterator<PropertyResource> iter = properties.iterator();
                        StringBuilder areaTextBuilder = new StringBuilder(areaText);
                        while (iter.hasNext()) {
                            PropertyResource property = iter.next();
                            areaTextBuilder.append(property.getName() == null ? Constants.BLANK : property.getName());
                            if (iter.hasNext()) {
                                areaTextBuilder.append("\n");
                            }
                        }
                        areaText = areaTextBuilder.toString();
                    }
                }

                text = parseMustache(text);
                if (!areaText.isEmpty()) {
                    areaText = parseMustache(areaText);
                }
                insertChapterIntoPanel(overviewPanel, text, areaText);
            }
            if (storyResource.getStory().getRisk() != null) {
                Risk risk = storyResource.getStory().getRisk();
                String riskText = risk.getText() == null ? Constants.BLANK : risk.getText();

                if (!riskText.isEmpty()) {
                    insertHeaderTextIntoPanel(Constants.TRACE_STORY_HEADER_RISK, overviewPanel);
                    riskText = parseMustache(riskText);
                    addTextPaneToPanel(riskText, overviewPanel);
                }
            }
        }
    }

    private String parseMustache(String text) {
        try {
            text = URLDecoder.decode(text, "UTF-8");
        } catch (Exception ignored) {
        }
        text = StringEscapeUtils.unescapeHtml4(text);

        for (String mustache : Constants.MUSTACHE_CONSTANTS) {
            text = text.replace(mustache, Constants.BLANK);
        }

        return text;
    }

    private void resetVulnerabilityDetails() {
        if (!httpRequestTextPane.getText().isEmpty()) {
            httpRequestTextPane.setText("");
        }
        DefaultTreeModel model = (DefaultTreeModel) eventsTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

        overviewPanel.removeAll();
        recommendationPanel.removeAll();
        if (root.getChildCount() > 0) {
            root.removeAllChildren();
            model.nodeStructureChanged(root);
        }
    }

    private void populateVulnerabilityDetailsHttpRequest(HttpRequestResource httpRequestResource) {
//        clear previous contents
        if (!httpRequestTextPane.getText().isEmpty()) {
            httpRequestTextPane.setText("");
        }
//
        httpRequestTextPane.setText(Constants.BLANK);
        if (httpRequestResource != null && httpRequestResource.getHttpRequest() != null
                && httpRequestResource.getHttpRequest().getFormattedText() != null) {

            httpRequestTextPane.setText(httpRequestResource.getHttpRequest().getText().replace(Constants.MUSTACHE_NL, Constants.BLANK));
        } else if (httpRequestResource != null && httpRequestResource.getReason() != null) {
            httpRequestTextPane.setText(httpRequestResource.getReason());
        }
        String text = httpRequestTextPane.getText();
        text = HtmlEscape.unescapeHtml(text);
        try {
            text = URLDecoder.decode(text, "UTF-8");
        } catch (Exception ignored) {
        }
        if (text.contains(Constants.TAINT) && text.contains(Constants.TAINT_CLOSED)) {

            String currentString = text;
            int start = text.indexOf(Constants.TAINT);
            currentString = currentString.replace(Constants.TAINT, "");
            int end = currentString.indexOf(Constants.TAINT_CLOSED);
            if (end > start) {
                currentString = currentString.replace(Constants.TAINT_CLOSED, "");
                httpRequestTextPane.setText(currentString);
            }
        }
    }

    private void populateVulnerabilityDetailsEvents(EventSummaryResource eventSummaryResource) {

        DefaultTreeModel model = (DefaultTreeModel) eventsTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
//        clear previous contents
        if (root.getChildCount() > 0) {
            root.removeAllChildren();
            model.nodeStructureChanged(root);
        }
//
        if (!eventSummaryResource.getEvents().isEmpty()) {

            for (EventResource eventResource : eventSummaryResource.getEvents()) {

                DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode(eventResource);
                List<EventResource> collapsedEvents = eventResource.getCollapsedEvents();
                if (!collapsedEvents.isEmpty()) {
                    for (EventResource collapsedEvent : collapsedEvents) {
                        DefaultMutableTreeNode collapsedEventNode = new DefaultMutableTreeNode(collapsedEvent);
                        addEventItemsToDefaultMutableTreeNode(collapsedEventNode, collapsedEvent);
                        defaultMutableTreeNode.add(collapsedEventNode);
                    }
                } else {
                    addEventItemsToDefaultMutableTreeNode(defaultMutableTreeNode, eventResource);
                }

                root.add(defaultMutableTreeNode);
            }
            model.nodeStructureChanged(root);
        } else {
            root.add(new DefaultMutableTreeNode("No Events info"));
            model.nodeStructureChanged(root);
        }
    }

    private void populateVulnerabilityDetailsRecommendation(RecommendationResource recommendationResource) {
        if (recommendationResource != null && recommendationResource.getRecommendation() != null && recommendationResource.getCustomRecommendation() != null
                && recommendationResource.getRuleReferences() != null && recommendationResource.getCustomRuleReferences() != null) {

            recommendationPanel.removeAll();
            String formattedRecommendationText = recommendationResource.getRecommendation().getFormattedText();
            String openTag = null;
            String closeTag = null;

            if (formattedRecommendationText.contains(Constants.OPEN_TAG_C_SHARP_BLOCK)) {
                openTag = Constants.OPEN_TAG_C_SHARP_BLOCK;
                closeTag = Constants.CLOSE_TAG_C_SHARP_BLOCK;
            } else if (formattedRecommendationText.contains(Constants.OPEN_TAG_HTML_BLOCK)) {
                openTag = Constants.OPEN_TAG_HTML_BLOCK;
                closeTag = Constants.CLOSE_TAG_HTML_BLOCK;
            } else if (formattedRecommendationText.contains(Constants.OPEN_TAG_JAVA_BLOCK)) {
                openTag = Constants.OPEN_TAG_JAVA_BLOCK;
                closeTag = Constants.CLOSE_TAG_JAVA_BLOCK;
            } else if (formattedRecommendationText.contains(Constants.OPEN_TAG_XML_BLOCK)) {
                openTag = Constants.OPEN_TAG_XML_BLOCK;
                closeTag = Constants.CLOSE_TAG_XML_BLOCK;
            } else if (formattedRecommendationText.contains(Constants.OPEN_TAG_JAVASCRIPT_BLOCK)) {
                openTag = Constants.OPEN_TAG_JAVASCRIPT_BLOCK;
                closeTag = Constants.CLOSE_TAG_JAVASCRIPT_BLOCK;
            }

            formattedRecommendationText = formatLinks(formattedRecommendationText);

            String[] codeBlocks = StringUtils.substringsBetween(formattedRecommendationText, openTag, closeTag);
            String[] textBlocks = StringUtils.substringsBetween(formattedRecommendationText, closeTag, openTag);

            String textBlockFirst = StringUtils.substringBefore(formattedRecommendationText, openTag);
            String textBlockLast = StringUtils.substringAfterLast(formattedRecommendationText, closeTag);

            if (!textBlockFirst.isEmpty()) {
                addTextPaneToPanel(textBlockFirst, recommendationPanel);
            }

            if (codeBlocks != null) {
                for (int i = 0; i < codeBlocks.length; i++) {

                    String textToInsert = StringEscapeUtils.unescapeHtml4(codeBlocks[i]);
                    addCodeTextPaneToPanel(textToInsert, recommendationPanel);

                    if (i < codeBlocks.length - 1) {
                        addTextPaneToPanel(textBlocks[i], recommendationPanel);
                    }
                }
            }

            if (!textBlockLast.isEmpty()) {
                addTextPaneToPanel(textBlockLast, recommendationPanel);
            }

            JPanel compoundPanel = new JPanel();
            JPanel headerPanel = new JPanel();
            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
            JPanel linksPanel = new JPanel();
            linksPanel.setLayout(new BoxLayout(linksPanel, BoxLayout.Y_AXIS));

            CustomRecommendation customRecommendation = recommendationResource.getCustomRecommendation();
            String customRecommendationText = customRecommendation.getText() == null ? Constants.BLANK : customRecommendation.getText();
            if (!customRecommendationText.isEmpty()) {
                customRecommendationText = parseMustache(customRecommendationText);

                JTextPane customRecommendationLabel = getBaseTextPane(customRecommendationText);

                headerPanel.add(customRecommendationLabel);
            }

            JTextPane cweHeaderLabel = getBaseTextPane("CWE:");

            JTextPane cweLabel = getBaseTextPane(recommendationResource.getCwe());

            headerPanel.add(cweHeaderLabel);
            linksPanel.add(cweLabel);

            JTextPane owaspHeaderLabel = getBaseTextPane("OWASP:");

            JTextPane owaspLabel = getBaseTextPane(recommendationResource.getOwasp());

            headerPanel.add(owaspHeaderLabel);
            linksPanel.add(owaspLabel);

            RuleReferences ruleReferences = recommendationResource.getRuleReferences();
            String ruleReferencesText = ruleReferences.getText() == null ? Constants.BLANK : ruleReferences.getText();
            if (!ruleReferencesText.isEmpty()) {
                ruleReferencesText = parseMustache(ruleReferencesText);

                JTextPane referencesHeaderLabel = getBaseTextPane("References:");

                JTextPane referencesLabel = getBaseTextPane(ruleReferencesText);

                headerPanel.add(referencesHeaderLabel);
                linksPanel.add(referencesLabel);
            }
            CustomRuleReferences customRuleReferences = recommendationResource.getCustomRuleReferences();
            if (StringUtils.isNotEmpty(customRuleReferences.getText())) {
                String customRuleReferencesText = parseMustache(customRuleReferences.getText());

                JTextPane customReferencesLabel = getBaseTextPane(customRuleReferencesText);

                headerPanel.add(customReferencesLabel);
            }

            compoundPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            compoundPanel.add(headerPanel);
            compoundPanel.add(linksPanel);

            recommendationPanel.add(compoundPanel);
        }
    }

    private JTextPane getBaseTextPane(String text) {
        JTextPane jTextPane = new JTextPane();
        jTextPane.setText(text);
        jTextPane.setEditable(false);
        jTextPane.setOpaque(false);
        return jTextPane;
    }

    private void addTextPaneToPanel(String text, JPanel jPanel) {
        JTextPane jTextPane = getBaseTextPane("");
        insertTextBlockIntoTextPane(jTextPane, text);

        jTextPane.setPreferredSize(new Dimension(100, jTextPane.getPreferredSize().height));

        jPanel.add(jTextPane);
    }

    private void addCodeTextPaneToPanel(String text, JPanel jPanel) {
        JTextPane jTextPane = new JTextPane();
        Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        Border lineBorder = BorderFactory.createLineBorder(new JBColor(Gray._204, Gray._24));
        Border compoundBorder = BorderFactory.createCompoundBorder(lineBorder, emptyBorder);
        jTextPane.setBorder(compoundBorder);

        jTextPane.setBackground(new JBColor(Gray._238, Gray._58));

        jTextPane.setEditable(false);
        insertTextBlockIntoTextPane(jTextPane, text);

        jPanel.add(jTextPane);
    }

    private String formatLinks(String text) {

        String formattedText = text;
        String[] links = StringUtils.substringsBetween(formattedText, Constants.OPEN_TAG_LINK, Constants.CLOSE_TAG_LINK);
        if (links != null && links.length > 0) {
            for (String link : links) {
                int indexOfDelimiter = link.indexOf(Constants.LINK_DELIM);
                String formattedLink = link.substring(indexOfDelimiter + Constants.LINK_DELIM.length()) + " (" + link.substring(0, indexOfDelimiter) + ")";

                formattedText = formattedText.substring(0, formattedText.indexOf(link)) + formattedLink + formattedText.substring(formattedText.indexOf(link) + link.length());
            }
        }

        return formattedText;
    }

    private void addEventItemsToDefaultMutableTreeNode(DefaultMutableTreeNode defaultMutableTreeNode, EventResource eventResource) {
        EventItem[] eventItems = eventResource.getItems();
        for (EventItem eventItem : eventItems) {
            defaultMutableTreeNode.add(new DefaultMutableTreeNode(eventItem));
        }
    }

    private void insertChapterIntoPanel(JPanel jPanel, String chapterIntroText, String chapterBody) {
        addTextPaneToPanel(chapterIntroText, jPanel);
        addCodeTextPaneToPanel(chapterBody, jPanel);
    }

    private void insertColoredTextIntoTextPane(JTextPane jTextPane, String text, Color color) {
        StyleContext styleContext = StyleContext.getDefaultStyleContext();
        Style style = styleContext.addStyle("test", null);

        StyleConstants.setForeground(style, color);

        try {
            jTextPane.getDocument().insertString(jTextPane.getDocument().getLength(), text, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }


    private void insertTextIntoTextPane(JTextPane jTextPane, String text) {
        try {
            jTextPane.getDocument().insertString(jTextPane.getDocument().getLength(), text, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void insertHeaderTextIntoPanel(String headerText, JPanel jPanel) {


        JTextPane jTextPane = new JTextPane();
        jTextPane.setEditable(false);
        jTextPane.setOpaque(false);

        StyleContext styleContext = StyleContext.getDefaultStyleContext();
        Style style = styleContext.addStyle("test", null);

        StyleConstants.setBold(style, true);

        try {
            jTextPane.getDocument().insertString(jTextPane.getDocument().getLength(), headerText, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        jTextPane.setPreferredSize(new Dimension(100, jTextPane.getPreferredSize().height));

        jPanel.add(jTextPane);
    }


    private void insertTextBlockIntoTextPane(JTextPane jTextPane, String textBlock) {
        if (textBlock.contains(Constants.OPEN_TAG_GOOD_PARAM)) {
            int indexOfGoodParamOpenTag = textBlock.indexOf(Constants.OPEN_TAG_GOOD_PARAM);
            String textBeforeGoodParamOpenTag = textBlock.substring(0, indexOfGoodParamOpenTag);
            int indexOfGoodParamCloseTag = textBlock.indexOf(Constants.CLOSE_TAG_GOOD_PARAM);
            String textAfterGoodParamCloseTag = textBlock.substring(indexOfGoodParamCloseTag + Constants.CLOSE_TAG_GOOD_PARAM.length());
            String goodParam = StringUtils.substringBetween(textBlock, Constants.OPEN_TAG_GOOD_PARAM, Constants.CLOSE_TAG_GOOD_PARAM);
            insertTextIntoTextPane(jTextPane, parseMustache(textBeforeGoodParamOpenTag));
            insertColoredTextIntoTextPane(jTextPane, goodParam, Constants.GOOD_PARAM_COLOR);
            insertTextIntoTextPane(jTextPane, parseMustache(textAfterGoodParamCloseTag));

        } else if (textBlock.contains(Constants.OPEN_TAG_BAD_PARAM)) {
            int indexOfBadParamOpenTag = textBlock.indexOf(Constants.OPEN_TAG_BAD_PARAM);
            String textBeforeBadParamOpenTag = textBlock.substring(0, indexOfBadParamOpenTag);
            int indexOfBadParamCloseTag = textBlock.indexOf(Constants.CLOSE_TAG_BAD_PARAM);
            String textAfterBadParamCloseTag = textBlock.substring(indexOfBadParamCloseTag + Constants.CLOSE_TAG_BAD_PARAM.length());
            String badParam = StringUtils.substringBetween(textBlock, Constants.OPEN_TAG_BAD_PARAM, Constants.CLOSE_TAG_BAD_PARAM);
            insertTextIntoTextPane(jTextPane, parseMustache(textBeforeBadParamOpenTag));
            insertColoredTextIntoTextPane(jTextPane, badParam, Constants.CREATION_COLOR);
            insertTextIntoTextPane(jTextPane, parseMustache(textAfterBadParamCloseTag));

        } else {
            insertTextIntoTextPane(jTextPane, parseMustache(textBlock));
        }
    }

    private TraceFilterForm getTraceFilterFormFromContrastFilterPersistentStateComponent() {

        TraceFilterForm traceFilterForm = null;

        Long serverId = Constants.ALL_SERVERS;
        String appId = Constants.ALL_APPLICATIONS;

        if (contrastFilterPersistentStateComponent.getSelectedServerUuid() != null) {
            serverId = contrastFilterPersistentStateComponent.getSelectedServerUuid();
        }
        if (contrastFilterPersistentStateComponent.getSelectedApplicationId() != null) {
            appId = contrastFilterPersistentStateComponent.getSelectedApplicationId();
        }

        int offset = contrastFilterPersistentStateComponent.getCurrentOffset();

        if (serverId == Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
            traceFilterForm = Util.getTraceFilterForm(offset, PAGE_LIMIT);
        } else if (serverId == Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
            traceFilterForm = Util.getTraceFilterForm(offset, PAGE_LIMIT);
        } else if (serverId != Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
            traceFilterForm = Util.getTraceFilterForm(serverId, offset, PAGE_LIMIT);
        } else if (serverId != Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
            traceFilterForm = Util.getTraceFilterForm(serverId, offset, PAGE_LIMIT);
        }
        if (contrastFilterPersistentStateComponent.getSeverities() != null && !contrastFilterPersistentStateComponent.getSeverities().isEmpty()) {
            traceFilterForm.setSeverities(getRuleSeveritiesEnumFromList(contrastFilterPersistentStateComponent.getSeverities()));
        }

        if (contrastFilterPersistentStateComponent.getLastDetectedFrom() != null) {
            LocalDateTime localDateTimeFrom = getLocalDateTimeFromMillis(contrastFilterPersistentStateComponent.getLastDetectedFrom());
            traceFilterForm.setStartDate(getDateFromLocalDateTime(localDateTimeFrom));
        }

        if (contrastFilterPersistentStateComponent.getLastDetectedTo() != null) {
            LocalDateTime localDateTimeTo = getLocalDateTimeFromMillis(contrastFilterPersistentStateComponent.getLastDetectedTo());
            traceFilterForm.setEndDate(getDateFromLocalDateTime(localDateTimeTo));
        }
        if (contrastFilterPersistentStateComponent.getStatuses() != null && !contrastFilterPersistentStateComponent.getStatuses().isEmpty()) {
            traceFilterForm.setStatus(contrastFilterPersistentStateComponent.getStatuses());
        }
        if (contrastFilterPersistentStateComponent.getCurrentOffset() != 0) {
            traceFilterForm.setOffset(contrastFilterPersistentStateComponent.getCurrentOffset());
        }

        if (contrastFilterPersistentStateComponent.getSort() != null) {
            traceFilterForm.setSort(contrastFilterPersistentStateComponent.getSort());
        } else {
            traceFilterForm.setSort(Constants.SORT_DESCENDING + Constants.SORT_BY_SEVERITY);
        }
        if (contrastFilterPersistentStateComponent.getAppVersionTag() != null && !contrastFilterPersistentStateComponent.getAppVersionTag().isEmpty()) {
            traceFilterForm.setAppVersionTags(Collections.singletonList(contrastFilterPersistentStateComponent.getAppVersionTag()));
        }

        traceFilterForm.setExpand(EnumSet.of(TraceFilterForm.TraceExpandValue.APPLICATION));

        return traceFilterForm;
    }

    private EnumSet<RuleSeverity> getRuleSeveritiesEnumFromList(List<String> severities) {

        EnumSet<RuleSeverity> ruleSeverities = EnumSet.noneOf(RuleSeverity.class);
        if (!severities.isEmpty()) {
            for (String severity : severities) {
                if (severity.equals(RuleSeverity.NOTE.toString())) {
                    ruleSeverities.add(RuleSeverity.NOTE);
                } else if (severity.equals(RuleSeverity.LOW.toString())) {
                    ruleSeverities.add(RuleSeverity.LOW);
                } else if (severity.equals(RuleSeverity.MEDIUM.toString())) {
                    ruleSeverities.add(RuleSeverity.MEDIUM);
                } else if (severity.equals(RuleSeverity.HIGH.toString())) {
                    ruleSeverities.add(RuleSeverity.HIGH);
                } else if (severity.equals(RuleSeverity.CRITICAL.toString())) {
                    ruleSeverities.add(RuleSeverity.CRITICAL);
                }
            }
        }
        return ruleSeverities;
    }

    private Servers retrieveServers() {
        Servers servers = null;
        try {
            ServerFilterForm serverFilterForm = new ServerFilterForm();
            serverFilterForm.setExpand(EnumSet.of(ServerFilterForm.ServerExpandValue.APPLICATIONS));
            servers = extendedContrastSDK.getServers(organizationConfig.getUuid(), serverFilterForm);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return servers;
    }

    private List<Application> retrieveApplications() {
        List<Application> applications = null;

        try {
            Applications apps = extendedContrastSDK.getApplications(organizationConfig.getUuid());
            if (apps != null) {
                applications = apps.getApplications();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return applications;
    }

    private void createUIComponents() {

        DefaultActionGroup actions = new DefaultActionGroup();
        AnAction settingsAction = new AnAction(ContrastPluginIcons.SETTINGS_ICON) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                ShowSettingsUtil.getInstance().showSettingsDialog(null, ContrastSearchableConfigurable.class);
            }
        };
        AnAction refreshAction = new AnAction(ContrastPluginIcons.REFRESH_ICON) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                refresh();
            }
        };
        AnAction filterAction = new AnAction(ContrastPluginIcons.FILTER_ICON) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                if (servers != null && applications != null) {
                    final FiltersDialog filtersDialog = new FiltersDialog(servers, applications, extendedContrastSDK, organizationConfig);
                    filtersDialog.setVisible(true);

                    TraceFilterForm dialogTraceFilterForm = filtersDialog.getTraceFilterForm();
                    if (dialogTraceFilterForm != null) {
                        dialogTraceFilterForm.setSort(traceFilterForm.getSort());
                        traceFilterForm = dialogTraceFilterForm;
                        traceFilterForm.setOffset(0);
                        traceFilterForm.setExpand(EnumSet.of(TraceFilterForm.TraceExpandValue.APPLICATION));
                        contrastFilterPersistentStateComponent.setPage(1);
                        contrastFilterPersistentStateComponent.setCurrentOffset(0);
                        refreshTraces(false);
                    }
                }
            }
        };

        actions.add(settingsAction);
        actions.add(refreshAction);
        actions.add(filterAction);
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, false);
        jComponent = toolbar.getComponent();
    }

    private int getNumOfPages(final int totalElements) {
        int numOfPages = 1;
        if (totalElements % ContrastToolWindowFactory.PAGE_LIMIT > 0) {
            numOfPages = totalElements / ContrastToolWindowFactory.PAGE_LIMIT + 1;
        } else {
            if (totalElements != 0) {
                numOfPages = totalElements / ContrastToolWindowFactory.PAGE_LIMIT;
            }
        }
        return numOfPages;
    }
}
