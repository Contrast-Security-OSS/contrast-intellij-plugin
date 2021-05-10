package com.contrastsecurity.core.extended;

import com.google.gson.annotations.SerializedName;

public class Filter {

    private String keycode;
    private String label;
    private int count;
    @SerializedName("new_group")
    private boolean newGroup;

    public String getKeycode() {
        return keycode;
    }

    public void setKeycode(String keycode) {
        this.keycode = keycode;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean getNewGroup() {
        return newGroup;
    }

    public void setNewGroup(boolean newGroup) {
        this.newGroup = newGroup;
    }

    @Override
    public String toString() {
        return keycode;
    }
}
