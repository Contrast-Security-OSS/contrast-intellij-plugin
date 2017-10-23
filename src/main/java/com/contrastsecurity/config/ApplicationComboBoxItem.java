package com.contrastsecurity.config;

import com.contrastsecurity.models.Application;

public class ApplicationComboBoxItem {
    private Application application;
    private String allApplicationsName;

    public ApplicationComboBoxItem(Application application) {
        this.application = application;
    }

    public ApplicationComboBoxItem(String allApplicationsName) {
        this.allApplicationsName = allApplicationsName;
    }

    @Override
    public String toString() {
        if (application == null) {
            return allApplicationsName;
        } else {
            return application.getName();
        }
    }
}
