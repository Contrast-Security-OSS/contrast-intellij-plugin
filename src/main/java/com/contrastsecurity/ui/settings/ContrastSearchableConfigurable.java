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
package com.contrastsecurity.ui.settings;

import com.contrastsecurity.config.ContrastPersistentStateComponent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
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
        try {
            contrastSearchableConfigurableGUI = new ContrastSearchableConfigurableGUI();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
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
