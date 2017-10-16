package com.contrastsecurity.ui.settings;

import com.contrastsecurity.config.ContrastPersistentStateComponent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ContrastSearchableConfigurable implements SearchableConfigurable {

    ContrastSearchableConfigurableGUI contrastSearchableConfigurableGUI;

    public ContrastSearchableConfigurable() {
    }

    @NotNull
    @Override
    public String getId() {
        return "preferences.ContrastSearchableConfigurable";
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Contrast";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        contrastSearchableConfigurableGUI = new ContrastSearchableConfigurableGUI();
        return contrastSearchableConfigurableGUI.getContrastSettingsPanel();
    }

    @Override
    public boolean isModified() {
        return contrastSearchableConfigurableGUI.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        contrastSearchableConfigurableGUI.apply();
    }

    @Override
    public void disposeUIResources() {
        contrastSearchableConfigurableGUI = null;
    }

    @Override
    public void reset() {
        contrastSearchableConfigurableGUI.reset();
    }
}
