package com.contrastsecurity.ui.com.contrastsecurity.ui.toolwindow;

import com.contrastsecurity.config.EventTypeIcon;
import com.contrastsecurity.core.Constants;
import com.contrastsecurity.core.extended.EventItem;
import com.contrastsecurity.core.extended.EventResource;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.Map;

public class EventTreeCellRenderer implements TreeCellRenderer {

    DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();
    Color backgroundSelectionColor;
    Color backgroundNonSelectionColor;

    public EventTreeCellRenderer() {
        defaultRenderer.setLeafIcon(null);

        backgroundSelectionColor = defaultRenderer.getBackgroundSelectionColor();
        backgroundNonSelectionColor = defaultRenderer.getBackgroundNonSelectionColor();
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
                Border margin = new EmptyBorder(0, 10, 0, 0);
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

                if (selected) {
                    jLabel.setForeground(Color.WHITE);
                }
                returnValue = jLabel;
            } else if (userObject instanceof EventResource) {
                EventResource eventResource = (EventResource) userObject;
                EventTypeIcon eventTypeIcon = getIcon(eventResource.getType());
                defaultRenderer.setOpenIcon(eventTypeIcon);
                defaultRenderer.setClosedIcon(eventTypeIcon);
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
}
