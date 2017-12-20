/******************************************************************************
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

import com.contrastsecurity.config.EventTypeIcon;
import com.contrastsecurity.config.EventTypeIconRect;
import com.contrastsecurity.core.Constants;
import com.contrastsecurity.core.extended.EventItem;
import com.contrastsecurity.core.extended.EventResource;
import com.contrastsecurity.core.extended.Fragment;
import com.contrastsecurity.core.extended.Line;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.net.URLDecoder;
import java.util.Map;

public class EventTreeCellRenderer implements TreeCellRenderer {

    private DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

    EventTreeCellRenderer() {
        defaultRenderer.setLeafIcon(null);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        Component returnValue = null;

        if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof EventItem) {
                EventItem eventItem = (EventItem) userObject;

                JLabel jLabel = new JLabel(eventItem.getValue());
                Font font = jLabel.getFont();
                Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
                Map attributes = font.getAttributes();
                attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                Font derivedFont = font.deriveFont(attributes);
                Border border = jLabel.getBorder();
                Border margin = JBUI.Borders.emptyLeft(10);
                Border compoundBorder = new CompoundBorder(border, margin);

                switch (eventItem.getType()) {
                    case EventResource.RED:
                        jLabel.setBorder(compoundBorder);
                        jLabel.setForeground(Constants.CREATION_COLOR);
                        break;
                    case EventResource.CUSTOM_RED:
                        jLabel.setBorder(compoundBorder);
                        jLabel.setForeground(Constants.CREATION_COLOR);
                        jLabel.setFont(derivedFont);
                        break;
                    case EventResource.CONTENT:
                        jLabel.setBorder(compoundBorder);
                        jLabel.setForeground(Constants.CONTENT_COLOR);
                        break;
                    case EventResource.CODE:
                        jLabel.setBorder(compoundBorder);
                        jLabel.setForeground(Constants.CODE_COLOR);
                        break;
                    case EventResource.CUSTOM_CODE:
                        jLabel.setForeground(Constants.CODE_COLOR);
                        jLabel.setBorder(compoundBorder);
                        jLabel.setFont(derivedFont);
                        break;
                    case EventResource.BOLD:
                        jLabel.setFont(boldFont);
                        break;
                    default:
                        break;
                }
                if (selected) jLabel.setForeground(JBColor.WHITE);
                returnValue = jLabel;
            } else if (userObject instanceof EventResource) {
                EventResource eventResource = (EventResource) userObject;
//                EventTypeIcon eventTypeIcon = getIcon(eventResource.getType());
//                JLabel jLabel = new JLabel(eventResource.toString());
//                jLabel.setIcon(eventTypeIcon);
//                returnValue = jLabel;

                returnValue = getEventResourcePanel(eventResource, selected);
            }
        }
        if (returnValue == null) {
            returnValue = defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded,
                    leaf, row, hasFocus);
        }
        return returnValue;
    }

    private EventTypeIcon getIcon(String type) {
        EventTypeIcon eventTypeIcon = null;
        if (type != null) {
            switch (type.toLowerCase()) {
                case "creation":
                    eventTypeIcon = new EventTypeIcon(Constants.EVENT_TYPE_ICON_COLOR_CREATION);
                    break;
                case "trigger":
                    eventTypeIcon = new EventTypeIcon(Constants.EVENT_TYPE_ICON_COLOR_TRIGGER);
                    break;
                case "tag":
                    eventTypeIcon = new EventTypeIcon(Constants.TAG_COLOR);
                    break;
                case "a2o":
                    eventTypeIcon = new EventTypeIcon(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "a2p":
                    eventTypeIcon = new EventTypeIcon(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "a2a":
                    eventTypeIcon = new EventTypeIcon(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "a2r":
                    eventTypeIcon = new EventTypeIcon(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "o2a":
                    eventTypeIcon = new EventTypeIcon(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "o2o":
                    eventTypeIcon = new EventTypeIcon(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "o2p":
                    eventTypeIcon = new EventTypeIcon(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "o2r":
                    eventTypeIcon = new EventTypeIcon(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "p2a":
                    eventTypeIcon = new EventTypeIcon(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "p2o":
                    eventTypeIcon = new EventTypeIcon(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "p2p":
                    eventTypeIcon = new EventTypeIcon(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "p2r":
                    eventTypeIcon = new EventTypeIcon(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                default:
                    eventTypeIcon = new EventTypeIcon(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
            }
        }
        return eventTypeIcon;
    }

    private EventTypeIconRect getIconRect(String type) {
        EventTypeIconRect eventTypeIconRect = null;
        if (type != null) {
            switch (type.toLowerCase()) {
                case "creation":
                    eventTypeIconRect = new EventTypeIconRect(Constants.EVENT_TYPE_ICON_COLOR_CREATION);
                    break;
                case "trigger":
                    eventTypeIconRect = new EventTypeIconRect(Constants.EVENT_TYPE_ICON_COLOR_TRIGGER);
                    break;
                case "tag":
                    eventTypeIconRect = new EventTypeIconRect(Constants.TAG_COLOR);
                    break;
                case "a2o":
                    eventTypeIconRect = new EventTypeIconRect(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "a2p":
                    eventTypeIconRect = new EventTypeIconRect(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "a2a":
                    eventTypeIconRect = new EventTypeIconRect(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "a2r":
                    eventTypeIconRect = new EventTypeIconRect(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "o2a":
                    eventTypeIconRect = new EventTypeIconRect(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "o2o":
                    eventTypeIconRect = new EventTypeIconRect(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "o2p":
                    eventTypeIconRect = new EventTypeIconRect(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "o2r":
                    eventTypeIconRect = new EventTypeIconRect(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "p2a":
                    eventTypeIconRect = new EventTypeIconRect(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "p2o":
                    eventTypeIconRect = new EventTypeIconRect(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "p2p":
                    eventTypeIconRect = new EventTypeIconRect(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                case "p2r":
                    eventTypeIconRect = new EventTypeIconRect(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
                default:
                    eventTypeIconRect = new EventTypeIconRect(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
                    break;
            }
        }
        return eventTypeIconRect;
    }


    private JPanel getEventResourcePanel(EventResource eventResource, boolean selected) {

        JPanel eventResourcePanel = new JPanel();
        JPanel descriptionPanel = new JPanel();
        JPanel codeViewPanel = new JPanel();
        JPanel dataViewPanel = new JPanel();

        descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.Y_AXIS));
        descriptionPanel.setOpaque(false);
        codeViewPanel.setLayout(new BoxLayout(codeViewPanel, BoxLayout.Y_AXIS));
        codeViewPanel.setOpaque(false);
        dataViewPanel.setLayout(new BoxLayout(dataViewPanel, BoxLayout.Y_AXIS));
        dataViewPanel.setOpaque(false);

        JLabel descriptionLabel = new JLabel(eventResource.toString().toUpperCase());
        descriptionLabel.setForeground(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);

        JLabel descriptionIconLabel = new JLabel(getIconRect(eventResource.getType()));
        descriptionPanel.add(descriptionLabel);
        descriptionPanel.add(descriptionIconLabel);

        for (Line line : eventResource.getCodeView().getLines()) {

            JTextPane jTextPane = new JTextPane();
            jTextPane.setEditable(false);
            jTextPane.setOpaque(false);

            for (Fragment fragment : line.getFragments()) {
                switch (fragment.getType()) {
                    case Constants.FRAGMENT_TYPE_NORMAL_CODE:
                        insertTextIntoTextPane(jTextPane, parseMustache(fragment.getValue()));
                        break;
                    case Constants.FRAGMENT_TYPE_CODE_STRING:
                        if (!selected) {
                            insertColoredTextIntoTextPane(jTextPane, parseMustache(fragment.getValue()), Constants.CODE_COLOR);
                        } else {
                            insertColoredTextIntoTextPane(jTextPane, parseMustache(fragment.getValue()), JBColor.WHITE);
                        }
                        break;
                    default:
                        insertTextIntoTextPane(jTextPane, parseMustache(fragment.getValue()));
                        break;
                }
            }

            codeViewPanel.add(jTextPane);
        }

        for (Line line : eventResource.getProbableStartLocationView().getLines()) {
            JTextPane jTextPane = new JTextPane();
            jTextPane.setEditable(false);
            jTextPane.setOpaque(false);
            jTextPane.setForeground(Constants.EVENT_TYPE_ICON_COLOR_PROPAGATION);
            jTextPane.setText(line.getText());
            codeViewPanel.add(jTextPane);
        }

        for (Line line : eventResource.getDataView().getLines()) {
            JTextPane jTextPane = new JTextPane();
            jTextPane.setEditable(false);
            jTextPane.setOpaque(false);

            for (Fragment fragment : line.getFragments()) {
                switch (fragment.getType()) {
                    case Constants.FRAGMENT_TYPE_TEXT:
                        insertTextIntoTextPane(jTextPane, parseMustache(fragment.getValue()));
                        break;
                    case Constants.FRAGMENT_TYPE_TAINT_VALUE:
                        insertColoredTextIntoTextPane(jTextPane, parseMustache(fragment.getValue()), Constants.CREATION_COLOR);
                        break;
                    default:
                        insertTextIntoTextPane(jTextPane, parseMustache(fragment.getValue()));
                        break;
                }
            }
            dataViewPanel.add(jTextPane);
        }

        eventResourcePanel.add(descriptionPanel);
        eventResourcePanel.add(codeViewPanel);
        eventResourcePanel.add(dataViewPanel);

        return eventResourcePanel;
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

    private String parseMustache(String text) {
        try {
            text = URLDecoder.decode(text, "UTF-8");
        } catch (Exception ignored) {
        }
        text = StringEscapeUtils.unescapeHtml4(text);
        text = text.replace("&apos;", "'");

        for (String mustache : Constants.MUSTACHE_CONSTANTS) {
            text = text.replace(mustache, Constants.BLANK);
        }

        return text;
    }
}
