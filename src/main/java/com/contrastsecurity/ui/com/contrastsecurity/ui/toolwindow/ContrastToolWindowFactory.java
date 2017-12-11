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
package com.contrastsecurity.ui.com.contrastsecurity.ui.toolwindow;

import com.contrastsecurity.config.ContrastFilterPersistentStateComponent;
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
import com.intellij.ui.JBColor;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import icons.ContrastPluginIcons;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.unbescape.html.HtmlEscape;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

public class ContrastToolWindowFactory implements ToolWindowFactory {

    private static final int PAGE_LIMIT = 20;
    MouseListener treeNodeClickListener;
    private JPanel contrastToolWindowContent;
    private JTable vulnerabilitiesTable;
    private ToolWindow contrastToolWindow;
    private JPanel cardPanel;
    private JPanel noVulnerabilitiesPanel;
    private JPanel vulnerabilityDetailsPanel;
    private JLabel traceSeverityLabel;
    private JLabel traceTitleLabel;
    private JButton externalLinkButton;
    private JButton backToResultsButton;
    private JTabbedPane tabbedPane1;
    private JTextPane overviewTextPane;
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
    private JComboBox pagesComboBox;
    private JButton nextTraceButton;
    private JButton previousTraceButton;
    private JLabel currentTraceDetailsLabel;
    private JLabel tracesCountLabel;
    private JPanel recommendationPanel;
    private JButton tagButton;
    private ContrastUtil contrastUtil;
    private ExtendedContrastSDK extendedContrastSDK;
    private ContrastTableModel contrastTableModel = new ContrastTableModel();
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

//
        recommendationPanel.setLayout(new BoxLayout(recommendationPanel, BoxLayout.Y_AXIS));
//

        pagesComboBoxActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                goToPage(Integer.valueOf(pagesComboBox.getSelectedItem().toString()), true);
            }
        };

        treeNodeClickListener = new MouseAdapter() {
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
                                    if (psiClasses != null && psiClasses.length > 0) {
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
                                    if (psiFiles != null && psiFiles.length > 0) {
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
                                MessageDialog messageDialog = new MessageDialog("Not found", "Source not found for " + typeName);
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

        backToResultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewDetailsTrace = null;

                CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
                cardLayout.show(cardPanel, "mainCard");
            }
        });


        externalLinkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openWebpage(viewDetailsTrace);
            }
        });

        firstPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        goToPage(1, false);
                    }
                }).start();
            }
        });

        lastPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        goToPage(numOfPages, false);
                    }
                }).start();
            }
        });

        previousPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        goToPage(Integer.valueOf(pageLabel.getText()) - 1, false);
                    }
                }).start();
            }
        });

        nextPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        goToPage(Integer.valueOf(pageLabel.getText()) + 1, false);
                    }
                }).start();
            }
        });

        previousTraceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        nextTraceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        tagButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                                    extendedContrastSDK.deleteTag(contrastUtil.getSelectedOrganizationConfig().getUuid(), viewDetailsTrace.getUuid(), tag);
                                    if (!tagsChanged) {
                                        tagsChanged = true;
                                    }
                                } catch (IOException | UnauthorizedException e1) {
                                    e1.printStackTrace();
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
                                BaseResponse baseResponse = extendedContrastSDK.putTags(contrastUtil.getSelectedOrganizationConfig().getUuid(), tagsServersResource);
                                if (!tagsChanged) {
                                    tagsChanged = true;
                                }
                            } catch (IOException | UnauthorizedException e1) {
                                e1.printStackTrace();
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
            }
        });

        refresh();
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
                start = new String((String) qualifier.subSequence(0, start)).lastIndexOf('.');
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
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }

    private boolean isFromDateLessThanToDate(LocalDateTime fromDate, LocalDateTime toDate) {
        Date lastDetectedFromDate = Date.from(fromDate.atZone(ZoneId.systemDefault()).toInstant());
        Date lastDetectedToDate = Date.from(toDate.atZone(ZoneId.systemDefault()).toInstant());

        if (lastDetectedFromDate.getTime() < lastDetectedToDate.getTime()) {
            return true;
        } else {
            return false;
        }
    }

    private Date getDateFromLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            return date;
        } else {
            return null;
        }
    }

    public void refresh() {
        contrastUtil = new ContrastUtil();
        extendedContrastSDK = contrastUtil.getContrastSDK();
        organizationConfig = contrastUtil.getSelectedOrganizationConfig();
        setupTable();
        traceFilterForm = getTraceFilterFormFromContrastFilterPersistentStateComponent();

        new Thread(new Runnable() {
            @Override
            public void run() {
                refreshTraces(false);
                servers = retrieveServers();
                applications = retrieveApplications();
            }
        }).start();
        contrastCache = contrastUtil.getContrastCache();
    }

    private void cleanTable() {
        contrastTableModel.setData(new Trace[0]);
        contrastTableModel.fireTableDataChanged();
    }

    private void refreshTraces(final boolean userUpdatedPagesComboBoxSelection) {

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
            if (!mainCard.isVisible()) {
                CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
                cardLayout.show(cardPanel, "mainCard");
            }
            if (contrastFilterPersistentStateComponent.getPage() != null) {
                pageLabel.setText(String.valueOf(contrastFilterPersistentStateComponent.getPage()));
            } else {
                pageLabel.setText("1");
            }

            numOfPages = getNumOfPages(PAGE_LIMIT, tracesObject.getCount());
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

    public void updatePagesComboBox(final int numOfPages) {
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
        contrastToolWindow = toolWindow;
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
        TableColumn severityColumn = vulnerabilitiesTable.getColumnModel().getColumn(0);
        severityColumn.setMaxWidth(76);
        severityColumn.setMinWidth(76);

        TableColumn viewDetailsColumn = vulnerabilitiesTable.getColumnModel().getColumn(3);
        viewDetailsColumn.setMaxWidth(120);

        TableColumn openInTeamserverColumn = vulnerabilitiesTable.getColumnModel().getColumn(4);
        openInTeamserverColumn.setMaxWidth(120);

        vulnerabilitiesTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {


                if (contrastTableModel != null && contrastTableModel.getRowCount() > 1) {
                    int col = vulnerabilitiesTable.columnAtPoint(e.getPoint());
                    String name = vulnerabilitiesTable.getColumnName(col);

                    if (name.equals("Severity")) {
                        if (traceFilterForm.getSort().startsWith(Constants.SORT_DESCENDING)) {
                            traceFilterForm.setSort(Constants.SORT_BY_SEVERITY);
                        } else {
                            traceFilterForm.setSort(Constants.SORT_DESCENDING + Constants.SORT_BY_SEVERITY);
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                refreshTraces(false);
                                contrastFilterPersistentStateComponent.setSort(traceFilterForm.getSort());
                            }
                        }).start();
                    } else if (name.equals("Vulnerability")) {
                        if (traceFilterForm.getSort().startsWith(Constants.SORT_DESCENDING)) {
                            traceFilterForm.setSort(Constants.SORT_BY_TITLE);
                        } else {
                            traceFilterForm.setSort(Constants.SORT_DESCENDING + Constants.SORT_BY_TITLE);
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                refreshTraces(false);
                                contrastFilterPersistentStateComponent.setSort(traceFilterForm.getSort());
                            }
                        }).start();
                    } else if (name.equals("Last Detected")) {
                        if (traceFilterForm.getSort().startsWith(Constants.SORT_DESCENDING)) {
                            traceFilterForm.setSort(Constants.SORT_BY_LAST_TIME_SEEN);
                        } else {
                            traceFilterForm.setSort(Constants.SORT_DESCENDING + Constants.SORT_BY_LAST_TIME_SEEN);
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                refreshTraces(false);
                                contrastFilterPersistentStateComponent.setSort(traceFilterForm.getSort());
                            }
                        }).start();
                    } else if (name.equals("Status")) {
                        if (traceFilterForm.getSort().startsWith(Constants.SORT_DESCENDING)) {
                            traceFilterForm.setSort(Constants.SORT_BY_STATUS);
                        } else {
                            traceFilterForm.setSort(Constants.SORT_DESCENDING + Constants.SORT_BY_STATUS);
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                refreshTraces(false);
                                contrastFilterPersistentStateComponent.setSort(traceFilterForm.getSort());
                            }
                        }).start();
                    } else if (name.equals("Application")) {
                        if (traceFilterForm.getSort().startsWith(Constants.SORT_DESCENDING)) {
                            traceFilterForm.setSort(Constants.SORT_BY_APPLICATION_NAME);
                        } else {
                            traceFilterForm.setSort(Constants.SORT_DESCENDING + Constants.SORT_BY_APPLICATION_NAME);
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                refreshTraces(false);
                                contrastFilterPersistentStateComponent.setSort(traceFilterForm.getSort());
                            }
                        }).start();
                    }
                }
            }
        });

        vulnerabilitiesTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                Point point = e.getPoint();
                int row = vulnerabilitiesTable.rowAtPoint(point);
                int col = vulnerabilitiesTable.columnAtPoint(point);

                if (row >= 0 && col >= 0) {
                    String name = vulnerabilitiesTable.getColumnName(col);

                    if (e.getClickCount() == 2 && !name.equals("Open in Teamserver") && !name.equals("View Details")) {
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
                    if (name.equals("Open in Teamserver")) {
                        Trace traceClicked = contrastTableModel.getTraceAtRow(row);
                        openWebpage(traceClicked);
                    } else if (name.equals("View Details")) {
                        Trace traceClicked = contrastTableModel.getTraceAtRow(row);
                        if (contrastUtil.isTraceLicensed(traceClicked)) {
                            selectedTraceRow = row;
                            viewDetailsTrace = traceClicked;
                            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
                            cardLayout.show(cardPanel, "vulnerabilityDetailsCard");
                            populateVulnerabilityDetailsPanel();
                        } else {
                            MessageDialog messageDialog = new MessageDialog(Constants.UNLICENSED_DIALOG_TITLE, Constants.UNLICENSED_DIALOG_MESSAGE);
                            messageDialog.setVisible(true);
                        }
                    }
                }
            }
        });
    }

    private void populateVulnerabilityDetailsPanel() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                resetVulnerabilityDetails();

                String severity = viewDetailsTrace.getSeverity();
                if (severity.equals(Constants.SEVERITY_LEVEL_NOTE)) {
                    traceSeverityLabel.setIcon(ContrastPluginIcons.SEVERITY_ICON_NOTE);
                } else if (severity.equals(Constants.SEVERITY_LEVEL_LOW)) {
                    traceSeverityLabel.setIcon(ContrastPluginIcons.SEVERITY_ICON_LOW);
                } else if (severity.equals(Constants.SEVERITY_LEVEL_MEDIUM)) {
                    traceSeverityLabel.setIcon(ContrastPluginIcons.SEVERITY_ICON_MEDIUM);
                } else if (severity.equals(Constants.SEVERITY_LEVEL_HIGH)) {
                    traceSeverityLabel.setIcon(ContrastPluginIcons.SEVERITY_ICON_HIGH);
                } else if (severity.equals(Constants.SEVERITY_LEVEL_CRITICAL)) {
                    traceSeverityLabel.setIcon(ContrastPluginIcons.SEVERITY_ICON_CRITICAL);
                }

                String title = viewDetailsTrace.getTitle();
                int indexOfUnlicensed = title.indexOf(Constants.UNLICENSED);
                if (indexOfUnlicensed != -1) {
                    title = "UNLICENSED - " + title.substring(0, indexOfUnlicensed);
                }
                traceTitleLabel.setText(title);

                try {
                    Key key = new Key(contrastUtil.getSelectedOrganizationConfig().getUuid(), viewDetailsTrace.getUuid());
                    Key keyForOrg = new Key(contrastUtil.getSelectedOrganizationConfig().getUuid(), null);

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
                }
                currentTraceDetailsLabel.setText(String.valueOf(selectedTraceRow + 1));
                tracesCountLabel.setText(String.valueOf(contrastTableModel.getRowCount()));
            }
        }).start();
    }

    private URL getOverviewUrl(String traceId) throws MalformedURLException {
        String teamServerUrl = contrastUtil.getTeamServerUrl();
        teamServerUrl = teamServerUrl.trim();
        if (teamServerUrl != null && teamServerUrl.endsWith("/api")) {
            teamServerUrl = teamServerUrl.substring(0, teamServerUrl.length() - 4);
        }
        if (teamServerUrl != null && teamServerUrl.endsWith("/api/")) {
            teamServerUrl = teamServerUrl.substring(0, teamServerUrl.length() - 5);
        }
        String urlStr = teamServerUrl + "/static/ng/index.html#/" + contrastUtil.getSelectedOrganizationConfig().getUuid() + "/vulns/" + traceId + "/overview";
        URL url = new URL(urlStr);
        return url;
    }

    public void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void openWebpage(Trace trace) {
        if (trace == null) {
            return;
        }
        // https://apptwo.contrastsecurity.com/Contrast/static/ng/index.html#/orgUuid/vulns/<VULN_ID>/overview
        try {
            URL url = getOverviewUrl(trace.getUuid());
            openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private LocalDateTime getLocalDateTimeFromMillis(Long millis) {
        Date date = new Date(millis);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return localDateTime;
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
//            remove previous contents
            if (!overviewTextPane.getText().isEmpty()) {
                overviewTextPane.setText("");
            }
//
            insertHeaderTextIntoOverviewTextPane(Constants.TRACE_STORY_HEADER_CHAPTERS);

            for (Chapter chapter : storyResource.getStory().getChapters()) {
                String text = chapter.getIntroText() == null ? Constants.BLANK : chapter.getIntroText();
                String areaText = chapter.getBody() == null ? Constants.BLANK : chapter.getBody();
                if (areaText.isEmpty()) {
                    List<PropertyResource> properties = chapter.getPropertyResources();
                    if (properties != null && properties.size() > 0) {
                        Iterator<PropertyResource> iter = properties.iterator();
                        while (iter.hasNext()) {
                            PropertyResource property = iter.next();
                            areaText += property.getName() == null ? Constants.BLANK : property.getName();
                            if (iter.hasNext()) {
                                areaText += "\n";
                            }
                        }
                    }
                }

                text = parseMustache(text);
                if (!areaText.isEmpty()) {
                    areaText = parseMustache(areaText);
                }
                insertChapterIntoTextPane(overviewTextPane, text, areaText);
            }
            if (storyResource.getStory().getRisk() != null) {
                Risk risk = storyResource.getStory().getRisk();
                String riskText = risk.getText() == null ? Constants.BLANK : risk.getText();

                if (!riskText.isEmpty()) {
                    insertHeaderTextIntoOverviewTextPane(Constants.TRACE_STORY_HEADER_RISK);
                    riskText = parseMustache(riskText);
                    insertTextIntoTextPane(overviewTextPane, riskText);
                }
            }
        }
    }

    private String parseMustache(String text) {
        text = text.replace(Constants.MUSTACHE_NL, Constants.BLANK);
        text = HtmlEscape.unescapeHtml(text);
        try {
            text = URLDecoder.decode(text, "UTF-8");
        } catch (Exception e) {
        }
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        // FIXME
        text = text.replace("{{#code}}", "");
        text = text.replace("{{/code}}", "");
        text = text.replace("{{#p}}", "");
        text = text.replace("{{/p}}", "");
        text = text.replace(Constants.OPEN_TAG_PARAGRAPH, "");
        text = text.replace(Constants.CLOSE_TAG_PARAGRAPH, "");
        text = text.replace(Constants.OPEN_TAG_LINK, "");
        text = text.replace(Constants.CLOSE_TAG_LINK, "");
        text = text.replace(Constants.OPEN_TAG_HEADER, "");
        text = text.replace(Constants.CLOSE_TAG_HEADER, "");
        text = text.replace("{{link1}}", "");
        text = text.replace("{{link2}}", "");

        return text;
    }

    private void resetVulnerabilityDetails() {
        if (!overviewTextPane.getText().isEmpty()) {
            overviewTextPane.setText("");
        }
        if (!httpRequestTextPane.getText().isEmpty()) {
            httpRequestTextPane.setText("");
        }
        DefaultTreeModel model = (DefaultTreeModel) eventsTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

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
        } catch (Exception e) {
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

            addTextPaneToPanel(textBlockFirst, recommendationPanel);

            for (int i = 0; i < codeBlocks.length; i++) {

                String textToInsert = codeBlocks[i].replace("&lt;", "<");
                textToInsert = textToInsert.replace("&gt;", ">");

                addCodeTextPaneToPanel(textToInsert, recommendationPanel);

                if (i < codeBlocks.length - 1) {
                    addTextPaneToPanel(textBlocks[i], recommendationPanel);
                }
            }
            addTextPaneToPanel(textBlockLast, recommendationPanel);

            CustomRecommendation customRecommendation = recommendationResource.getCustomRecommendation();
            String customRecommendationText = customRecommendation.getText() == null ? Constants.BLANK : customRecommendation.getText();

            JTextPane jTextPane = new JTextPane();
            jTextPane.setEditable(false);
            if (!customRecommendationText.isEmpty()) {
                customRecommendationText = parseMustache(customRecommendationText);
                insertTextIntoTextPane(jTextPane, customRecommendationText + "\n\n");
            }
            String cwe = "CWE: " + recommendationResource.getCwe() + "\n";
            insertTextIntoTextPane(jTextPane, cwe);

            String owasp = "OWASP: " + recommendationResource.getOwasp() + "\n";
            insertTextIntoTextPane(jTextPane, owasp);

            RuleReferences ruleReferences = recommendationResource.getRuleReferences();
            String ruleReferencesText = ruleReferences.getText() == null ? Constants.BLANK : ruleReferences.getText();
            if (!ruleReferencesText.isEmpty()) {
                ruleReferencesText = parseMustache(ruleReferencesText);
                insertTextIntoTextPane(jTextPane, "References: " + ruleReferencesText + "\n");
            }
            CustomRuleReferences customRuleReferences = recommendationResource.getCustomRuleReferences();
            String customRuleReferencesText = customRuleReferences.getText() == null ? Constants.BLANK : customRuleReferences.getText();
            if (!customRuleReferencesText.isEmpty()) {
                customRuleReferencesText = parseMustache(customRuleReferencesText) + "\n";
                insertTextIntoTextPane(jTextPane, customRuleReferencesText);
            }
            Border emptyBorder = BorderFactory.createEmptyBorder(0, 10, 0, 10);
            jTextPane.setBorder(emptyBorder);

            jTextPane.setPreferredSize(new Dimension(100, jTextPane.getPreferredSize().height));

            recommendationPanel.add(jTextPane);
        }
    }

    private void addTextPaneToPanel(String text, JPanel jPanel) {
        JTextPane jTextPane = new JTextPane();
        Border emptyBorder = BorderFactory.createEmptyBorder(0, 10, 0, 10);
        jTextPane.setBorder(emptyBorder);
        jTextPane.setEditable(false);
        insertTextBlockIntoTextPane(jTextPane, text);

        jTextPane.setPreferredSize(new Dimension(100, jTextPane.getPreferredSize().height));

        jPanel.add(jTextPane);
    }

    private void addCodeTextPaneToPanel(String text, JPanel jPanel) {
        JTextPane jTextPane = new JTextPane();
        Border emptyBorder = BorderFactory.createEmptyBorder(0, 10, 0, 10);
        Border lineBorder = BorderFactory.createLineBorder(new JBColor(new Color(204, 204, 204), new Color(24, 24, 24)));
//        Border outsideCompoundBorder = BorderFactory.createCompoundBorder(emptyBorder, lineBorder);
//        Border compoundBorder = BorderFactory.createCompoundBorder(outsideCompoundBorder, emptyBorder);
        Border compoundBorder = BorderFactory.createCompoundBorder(lineBorder, emptyBorder);
        jTextPane.setBorder(compoundBorder);

        jTextPane.setBackground(new JBColor(new Color(238, 238, 238), new Color(58, 58, 58)));

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

    private void insertChapterIntoTextPane(JTextPane jTextPane, String chapterIntroText, String chapterBody) {
        StyleContext styleContext = StyleContext.getDefaultStyleContext();
        Style style = styleContext.addStyle("test", null);

        StyleConstants.setBackground(style, Color.GRAY);
        StyleConstants.setForeground(style, Color.WHITE);

        try {
            jTextPane.getDocument().insertString(jTextPane.getDocument().getLength(), chapterIntroText + "\n", null);
            jTextPane.getDocument().insertString(jTextPane.getDocument().getLength(), chapterBody + "\n\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void insertHighlightedTextIntoTextPane(JTextPane jTextPane, String text) {
        StyleContext styleContext = StyleContext.getDefaultStyleContext();
        Style style = styleContext.addStyle("test", null);

        StyleConstants.setBackground(style, Color.GRAY);
        StyleConstants.setForeground(style, Color.WHITE);

        try {
            jTextPane.getDocument().insertString(jTextPane.getDocument().getLength(), text + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
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

    private void insertHeaderTextIntoOverviewTextPane(String headerText) {

        StyleContext styleContext = StyleContext.getDefaultStyleContext();
        Style style = styleContext.addStyle("test", null);

        StyleConstants.setBold(style, true);

        try {
            overviewTextPane.getDocument().insertString(overviewTextPane.getDocument().getLength(), headerText + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
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
        int count = 0;
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

    public List<Application> retrieveApplications() {
        int count = 0;
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
                    FiltersDialog filtersDialog = new FiltersDialog(servers, applications);
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

    private int getNumOfPages(final int pageLimit, final int totalElements) {
        int numOfPages = 1;
        if (totalElements % pageLimit > 0) {
            numOfPages = totalElements / pageLimit + 1;
        } else {
            if (totalElements != 0) {
                numOfPages = totalElements / pageLimit;
            }
        }
        return numOfPages;
    }
}
