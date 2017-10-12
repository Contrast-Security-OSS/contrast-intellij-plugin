package com.contrastsecurity;

import com.contrastsecurity.sdk.ContrastSDK;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

public class ContrastToolWindowFactory implements ToolWindowFactory {

    private JLabel currentDate;
    private JLabel currentTime;
    private JLabel timeZone;
    private JPanel contrastToolWindowContent;
    private JButton refreshToolWindowButton;
    private JButton hideToolWindowButton;
    private ToolWindow contrastToolWindow;

    public ContrastToolWindowFactory() {
        hideToolWindowButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                contrastToolWindow.hide(null);
            }
        });
        refreshToolWindowButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ContrastToolWindowFactory.this.currentDateTime();
            }
        });
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        contrastToolWindow = toolWindow;
        this.currentDateTime();
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(contrastToolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public void currentDateTime() {
        // Get current date and time
        Calendar instance = Calendar.getInstance();
        currentDate.setText(String.valueOf(instance.get(Calendar.DAY_OF_MONTH)) + "/"
                + String.valueOf(instance.get(Calendar.MONTH) + 1) + "/" +
                String.valueOf(instance.get(Calendar.YEAR)));
        currentDate.setIcon(new ImageIcon(getClass().getResource("/contrastToolWindow/Calendar-icon.png")));
        int min = instance.get(Calendar.MINUTE);
        String strMin;
        if (min < 10) {
            strMin = "0" + String.valueOf(min);
        } else {
            strMin = String.valueOf(min);
        }
        currentTime.setText(instance.get(Calendar.HOUR_OF_DAY) + ":" + strMin);
        currentTime.setIcon(new ImageIcon(getClass().getResource("/contrastToolWindow/Time-icon.png")));
        // Get time zone
        long gmt_Offset = instance.get(Calendar.ZONE_OFFSET); // offset from GMT in milliseconds
        String str_gmt_Offset = String.valueOf(gmt_Offset / 3600000);
        str_gmt_Offset = (gmt_Offset > 0) ? "GMT + " + str_gmt_Offset : "GMT - " + str_gmt_Offset;
        timeZone.setText(str_gmt_Offset);
        timeZone.setIcon(new ImageIcon(getClass().getResource("/contrastToolWindow/Time-zone-icon.png")));


    }


}
