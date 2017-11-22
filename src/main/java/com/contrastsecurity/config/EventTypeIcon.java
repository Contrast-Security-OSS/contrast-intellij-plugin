package com.contrastsecurity.config;

import javax.swing.*;
import java.awt.*;

public class EventTypeIcon implements Icon {

    Color color;
    private int diameter = 10;

    public EventTypeIcon(Color color) {
        this.color = color;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(color);
        g2d.fillOval(x, y, diameter, diameter);
        g2d.dispose();
    }

    public int getIconWidth() {
        return diameter;
    }

    public int getIconHeight() {
        return diameter;
    }

}