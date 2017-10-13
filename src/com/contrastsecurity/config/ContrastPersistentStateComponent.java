package com.contrastsecurity.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

@State(
        name="ContrastPersistentStateComponent",
        storages = {
                @Storage(StoragePathMacros.WORKSPACE_FILE)}
)
public class ContrastPersistentStateComponent implements PersistentStateComponent<ContrastPersistentStateComponent> {
    @Nullable
    @Override
    public ContrastPersistentStateComponent getState() {
        return this;
    }

    @Override
    public void loadState(ContrastPersistentStateComponent contrastPersistentStateComponent) {
        XmlSerializerUtil.copyBean(contrastPersistentStateComponent, this);
    }
}
