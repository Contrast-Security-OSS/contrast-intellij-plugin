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
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import icons.ContrastPluginIcons;
import org.jetbrains.annotations.NotNull;
import org.unbescape.html.HtmlEscape;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class ContrastToolWindowFactory implements ToolWindowFactory {

    private static final int PAGE_LIMIT = 20;
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
    private JScrollPane eventsScrollPane;
    private JScrollPane httpRequestScrollPane;
    private ContrastUtil contrastUtil;
    private ExtendedContrastSDK extendedContrastSDK;
    private ContrastTableModel contrastTableModel = new ContrastTableModel();
    private OrganizationConfig organizationConfig;
    private ContrastFilterPersistentStateComponent contrastFilterPersistentStateComponent;
    private Trace viewDetailsTrace;
    private TraceFilterForm traceFilterForm;

    private int numOfPages = 1;
    private Servers servers;
    private List<Application> applications;
    private ContrastCache contrastCache;

    public ContrastToolWindowFactory() {
        EventTreeCellRenderer eventTreeCellRenderer = new EventTreeCellRenderer();
        eventsTree.setCellRenderer(eventTreeCellRenderer);

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

        previousPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int prevPage = Integer.valueOf(pageLabel.getText()) - 1;
                pageLabel.setText(String.valueOf(prevPage));

                int currentOffset = PAGE_LIMIT * (prevPage - 1);
                traceFilterForm.setOffset(currentOffset);

                contrastFilterPersistentStateComponent.setPage(prevPage);
                contrastFilterPersistentStateComponent.setCurrentOffset(currentOffset);
                refreshTraces();
            }
        });

        nextPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int nextPage = Integer.valueOf(pageLabel.getText()) + 1;
                pageLabel.setText(String.valueOf(nextPage));

                int currentOffset = PAGE_LIMIT * (nextPage - 1);
                traceFilterForm.setOffset(currentOffset);

                contrastFilterPersistentStateComponent.setPage(nextPage);
                contrastFilterPersistentStateComponent.setCurrentOffset(currentOffset);
                refreshTraces();
            }
        });

        refresh();
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
                refreshTraces();
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

    private void refreshTraces() {

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
            pageLabel.setText(String.valueOf(contrastFilterPersistentStateComponent.getPage()));
            numOfPages = getNumOfPages(PAGE_LIMIT, tracesObject.getCount());
            updatePageButtons();

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
        } else {
            previousPageButton.setEnabled(true);
        }

        if (newPage == numOfPages) {
            nextPageButton.setEnabled(false);
        } else {
            nextPageButton.setEnabled(true);
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

        TableColumn viewDetailsColumn = vulnerabilitiesTable.getColumnModel().getColumn(2);
        viewDetailsColumn.setMaxWidth(120);

        TableColumn openInTeamserverColumn = vulnerabilitiesTable.getColumnModel().getColumn(3);
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
                        refreshTraces();
                        contrastFilterPersistentStateComponent.setSort(traceFilterForm.getSort());
                    } else if (name.equals("Vulnerability")) {
                        if (traceFilterForm.getSort().startsWith(Constants.SORT_DESCENDING)) {
                            traceFilterForm.setSort(Constants.SORT_BY_TITLE);
                        } else {
                            traceFilterForm.setSort(Constants.SORT_DESCENDING + Constants.SORT_BY_TITLE);
                        }
                        refreshTraces();
                        contrastFilterPersistentStateComponent.setSort(traceFilterForm.getSort());
                    } else if (name.equals("Last Detected")) {
                        if (traceFilterForm.getSort().startsWith(Constants.SORT_DESCENDING)) {
                            traceFilterForm.setSort(Constants.SORT_BY_LAST_TIME_SEEN);
                        } else {
                            traceFilterForm.setSort(Constants.SORT_DESCENDING + Constants.SORT_BY_LAST_TIME_SEEN);
                        }
                        refreshTraces();
                        contrastFilterPersistentStateComponent.setSort(traceFilterForm.getSort());
                    } else if (name.equals("Status")) {
                        if (traceFilterForm.getSort().startsWith(Constants.SORT_DESCENDING)) {
                            traceFilterForm.setSort(Constants.SORT_BY_STATUS);
                        } else {
                            traceFilterForm.setSort(Constants.SORT_DESCENDING + Constants.SORT_BY_STATUS);
                        }
                        refreshTraces();
                        contrastFilterPersistentStateComponent.setSort(traceFilterForm.getSort());
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
                            viewDetailsTrace = traceClicked;
                            CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
                            cardLayout.show(cardPanel, "vulnerabilityDetailsCard");
                            tabbedPane1.setSelectedIndex(1);
                            populateVulnerabilityDetailsPanel();
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

                    StoryResource storyResource = getStory(key);
                    HttpRequestResource httpRequestResource = getHttpRequest(key);
                    EventSummaryResource eventSummaryResource = getEventSummary(key);

                    populateVulnerabilityDetailsOverview(storyResource);
                    populateVulnerabilityDetailsEvents(eventSummaryResource);
                    populateVulnerabilityDetailsHttpRequest(httpRequestResource);
                } catch (IOException | UnauthorizedException e) {
                    e.printStackTrace();
                }
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

    private HttpRequestResource getHttpRequest(Key key) throws IOException, UnauthorizedException {

        HttpRequestResource httpRequestResource = contrastCache.getHttpRequestResources().get(key);
        if (httpRequestResource == null) {
            httpRequestResource = extendedContrastSDK.getHttpRequest(key.getOrgUuid(), key.getTraceId());
            contrastCache.getHttpRequestResources().put(key, httpRequestResource);
        }
        return httpRequestResource;
    }

    private void populateVulnerabilityDetailsOverview(StoryResource storyResource) {
        if (storyResource != null && storyResource.getStory() != null && storyResource.getStory().getChapters() != null
                && !storyResource.getStory().getChapters().isEmpty()) {

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
                insertChapterIntoOverviewTextPane(text, areaText);
            }
            if (storyResource.getStory().getRisk() != null) {
                Risk risk = storyResource.getStory().getRisk();
                String riskText = risk.getText() == null ? Constants.BLANK : risk.getText();

                if (!riskText.isEmpty()) {
                    insertHeaderTextIntoOverviewTextPane(Constants.TRACE_STORY_HEADER_RISK);
                    riskText = parseMustache(riskText);
                    insertTextIntoOverviewTextPane(riskText);
                }
            }
        }
    }

    private String parseMustache(String text) {
        text = text.replace(Constants.MUSTACHE_NL, Constants.BLANK);
        //text = StringEscapeUtils.unescapeHtml(text);
        text = HtmlEscape.unescapeHtml(text);
        try {
            text = URLDecoder.decode(text, "UTF-8");
        } catch (Exception e) {
            // ignore
        }
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        // FIXME
        text = text.replace("{{#code}}", "");
        text = text.replace("{{/code}}", "");
        text = text.replace("{{#p}}", "");
        text = text.replace("{{/p}}", "");
        return text;
    }

    private void resetVulnerabilityDetails() {
        overviewTextPane.setText("");
        httpRequestTextPane.setText("");

        DefaultTreeModel model = (DefaultTreeModel) eventsTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        root.removeAllChildren();
        model.nodeStructureChanged(root);

    }

    private void populateVulnerabilityDetailsHttpRequest(HttpRequestResource httpRequestResource) {
        httpRequestTextPane.setText(Constants.BLANK);
        if (httpRequestResource != null && httpRequestResource.getHttpRequest() != null
                && httpRequestResource.getHttpRequest().getFormattedText() != null) {

            if (tabbedPane1.indexOfComponent(httpRequestScrollPane) < 0) {
                tabbedPane1.addTab(Constants.HTTP_REQUEST_TAB_TITLE, httpRequestScrollPane);
            }

            httpRequestTextPane.setText(httpRequestResource.getHttpRequest().getText().replace(Constants.MUSTACHE_NL, Constants.BLANK));
        } else if (httpRequestResource != null && httpRequestResource.getReason() != null) {
            httpRequestTextPane.setText(httpRequestResource.getReason());

            if (tabbedPane1.indexOfComponent(httpRequestScrollPane) > 0) {
                tabbedPane1.remove(tabbedPane1.indexOfComponent(httpRequestScrollPane));
            }
        }
        String text = httpRequestTextPane.getText();
        text = HtmlEscape.unescapeHtml(text);
        try {
            text = URLDecoder.decode(text, "UTF-8");
        } catch (Exception e) {
            // ignore
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

        if (!eventSummaryResource.getEvents().isEmpty()) {

            if (tabbedPane1.indexOfComponent(eventsScrollPane) < 0) {
                tabbedPane1.insertTab(Constants.EVENTS_TAB_TITLE, null, eventsScrollPane, null, 1);
            }

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
            if (tabbedPane1.indexOfComponent(eventsScrollPane) > 0) {
                tabbedPane1.remove(tabbedPane1.indexOfComponent(eventsScrollPane));
            }
        }
    }

    private void addEventItemsToDefaultMutableTreeNode(DefaultMutableTreeNode defaultMutableTreeNode, EventResource eventResource) {
        EventItem[] eventItems = eventResource.getItems();
        for (EventItem eventItem : eventItems) {
            defaultMutableTreeNode.add(new DefaultMutableTreeNode(eventItem));
        }
    }

    private void insertChapterIntoOverviewTextPane(String chapterIntroText, String chapterBody) {
        StyleContext styleContext = StyleContext.getDefaultStyleContext();
        Style style = styleContext.addStyle("test", null);

        StyleConstants.setBackground(style, Color.GRAY);
        StyleConstants.setForeground(style, Color.WHITE);

        try {
            overviewTextPane.getDocument().insertString(overviewTextPane.getDocument().getLength(), chapterIntroText + "\n", null);
            overviewTextPane.getDocument().insertString(overviewTextPane.getDocument().getLength(), chapterBody + "\n\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }


    private void insertTextIntoOverviewTextPane(String text) {
        try {
            overviewTextPane.getDocument().insertString(overviewTextPane.getDocument().getLength(), text + "\n", null);
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
        }

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
                ShowSettingsUtil.getInstance().showSettingsDialog(null, "Contrast");
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
                        pageLabel.setText("1");
                        contrastFilterPersistentStateComponent.setPage(1);
                        contrastFilterPersistentStateComponent.setCurrentOffset(0);
                        refreshTraces();
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
