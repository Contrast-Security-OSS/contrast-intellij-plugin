package com.contrastsecurity.ui.com.contrastsecurity.ui.toolwindow;

import com.contrastsecurity.core.Constants;
import com.contrastsecurity.core.extended.EventItem;
import com.contrastsecurity.core.extended.EventResource;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

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

                switch (eventItem.getType()) {
                    case EventResource.RED:
//                        x = x + 15;
                        jLabel.setText("     " + jLabel.getText());
                        jLabel.setForeground(Constants.CREATION_COLOR);
                        break;
                    case EventResource.CONTENT:
//                        x = x + 15;
                        jLabel.setText("     " + jLabel.getText());
                        jLabel.setForeground(Constants.CONTENT_COLOR);
                        break;
                    case EventResource.CODE:
//                        x = x + 15;
                        jLabel.setText("     " + jLabel.getText());
                        jLabel.setForeground(Constants.CODE_COLOR);
                        break;
                    case EventResource.BOLD:
                        jLabel.setFont(boldFont);
                        break;
                    default:
                        break;
                }
                returnValue = jLabel;
            }
        }
        if (returnValue == null) {
            returnValue = defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded,
                    leaf, row, hasFocus);
        }
        return returnValue;
    }


}
