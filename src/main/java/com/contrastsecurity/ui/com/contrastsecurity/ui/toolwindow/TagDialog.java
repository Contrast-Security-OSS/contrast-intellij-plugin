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

import com.contrastsecurity.core.extended.TagsResource;
import org.apache.commons.lang.ArrayUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TagDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox tagsComboBox;
    private JTextField newLabelTextField;
    private JButton applyNewLabelButton;
    private JTable tagTable;

    private TagsResource viewDetailsTraceTagsResource;
    private TagsResource orgTagsResource;
    private TagTableModel tagTableModel = new TagTableModel();
    private ActionListener tagsComboBoxActionListener;
    private List<String> newTraceTags = null;

    public TagDialog() {
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
    }

    public TagDialog(TagsResource viewDetailsTraceTagsResource, TagsResource orgTagsResource) {
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
        setTitle("Tag Vulnerability");

        tagsComboBoxActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tag = tagsComboBox.getSelectedItem().toString();
                applyTag(tag);
            }
        };

        this.viewDetailsTraceTagsResource = viewDetailsTraceTagsResource;
        this.orgTagsResource = orgTagsResource;

        populateTagsComboBox(tagsComboBox, viewDetailsTraceTagsResource, orgTagsResource);

        tagsComboBox.addActionListener(tagsComboBoxActionListener);

        applyNewLabelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newTag = newLabelTextField.getText();
                applyTag(newTag);
            }
        });

        setupTable(tagTable, tagTableModel);
        String[] viewDetailsTraceTagsArray = viewDetailsTraceTagsResource.getTags().toArray(new String[viewDetailsTraceTagsResource.getTags().size()]);

        tagTableModel.setData(viewDetailsTraceTagsArray);
        tagTableModel.fireTableDataChanged();
    }

    private void applyTag(String tag) {
        if (!tag.isEmpty()) {
            if (((DefaultComboBoxModel) tagsComboBox.getModel()).getIndexOf(tag) != -1) {
                tagsComboBox.removeItem(tag);
            }
            if (!newLabelTextField.getText().isEmpty()) {
                newLabelTextField.setText("");
            }

            String[] data = tagTableModel.getData();
            String[] newData = Arrays.copyOf(data, data.length + 1);
            newData[newData.length - 1] = tag;
            tagTableModel.setData(newData);
            tagTableModel.fireTableDataChanged();
        }
    }

    private void removeTag(String tag) {
        if (viewDetailsTraceTagsResource.getTags().contains(tag) || orgTagsResource.getTags().contains(tag)) {
            tagsComboBox.removeActionListener(tagsComboBoxActionListener);
            tagsComboBox.addItem(tag);
            tagsComboBox.addActionListener(tagsComboBoxActionListener);
        }
        String[] newData = (String[]) ArrayUtils.removeElement(tagTableModel.getData(), tag);
        tagTableModel.setData(newData);
        tagTableModel.fireTableDataChanged();
    }

    private void populateTagsComboBox(JComboBox jComboBox, TagsResource viewDetailsTraceTagsResource, TagsResource orgTagsResource) {

        List<String> orgTags = orgTagsResource.getTags();
        List<String> traceTags = viewDetailsTraceTagsResource.getTags();
        List<String> tagsToAdd = new ArrayList<>();

        for (String tag : orgTags) {
            if (!traceTags.contains(tag)) {
                tagsToAdd.add(tag);
            }
        }
        populateComboBox(jComboBox, tagsToAdd);
    }

    private void populateComboBox(JComboBox jComboBox, List<String> items) {
        jComboBox.removeAllItems();
        for (String item : items) {
            jComboBox.addItem(item);
        }
    }

    private void setupTable(JTable jTable, AbstractTableModel abstractTableModel) {
        jTable.setModel(abstractTableModel);
        jTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = jTable.rowAtPoint(e.getPoint());
                int col = jTable.columnAtPoint(e.getPoint());

                if (row >= 0 && col >= 0) {
                    String name = jTable.getColumnName(col);
                    if (name.equals("Remove")) {
                        String tag = (String) tagTableModel.getValueAt(row, 0);
                        removeTag(tag);
                    }
                }
            }
        });
    }

    private void onOK() {
        // add your code here
        newTraceTags = Arrays.asList(tagTableModel.getData());
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        TagDialog dialog = new TagDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public List<String> getNewTraceTags() {
        return newTraceTags;
    }
}
