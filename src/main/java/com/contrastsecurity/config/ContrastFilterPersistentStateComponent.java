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
package com.contrastsecurity.config;

import com.contrastsecurity.core.Constants;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@State(
        name = "ContrastFilterPersistentStateComponent",
        storages = {
                @Storage("contrast-filter.xml")}
)
public class ContrastFilterPersistentStateComponent implements PersistentStateComponent<ContrastFilterPersistentStateComponent> {

    public Long selectedServerUuid;
    public String selectedApplicationName = "";
    public String selectedApplicationId;
    public List<String> severities;
    public String lastDetected = Constants.LAST_DETECTED_ALL;
    public Long lastDetectedFrom;
    public Long lastDetectedTo;
    public List<String> statuses;
    public Integer page;
    public String sort;
    public int currentOffset = 0;
    public String appVersionTag;

    public boolean isBeingTracked() {
        return isBeingTracked;
    }

    public void setBeingTracked(boolean beingTracked) {
        isBeingTracked = beingTracked;
    }

    public boolean isUntracked() {
        return isUntracked;
    }

    public void setUntracked(boolean untracked) {
        isUntracked = untracked;
    }

    public boolean isBeingTracked;
    public boolean isUntracked;
    public String selectedOrganizationName = "";

    @Nullable
    @Override
    public ContrastFilterPersistentStateComponent getState() {
        return this;
    }

    @Override
    public void loadState(ContrastFilterPersistentStateComponent contrastFilterPersistentStateComponent) {
        XmlSerializerUtil.copyBean(contrastFilterPersistentStateComponent, this);
    }

    @Nullable
    public static ContrastFilterPersistentStateComponent getInstance(Project project) {
        return ServiceManager.getService(project, ContrastFilterPersistentStateComponent.class);
    }

    public String getSelectedApplicationName() {
        return selectedApplicationName;
    }

    public void setSelectedApplicationName(String selectedApplicationName) {
        this.selectedApplicationName = selectedApplicationName;
    }

    public List<String> getSeverities() {
        return severities;
    }

    public void setSeverities(List<String> severities) {
        this.severities = new ArrayList<>(severities);
    }

    public String getLastDetected() {
        return lastDetected;
    }

    public void setLastDetected(String lastDetected) {
        this.lastDetected = lastDetected;
    }

    public Long getLastDetectedFrom() {
        return lastDetectedFrom;
    }

    public void setLastDetectedFrom(Long lastDetectedFrom) {
        this.lastDetectedFrom = lastDetectedFrom;
    }

    public Long getLastDetectedTo() {
        return lastDetectedTo;
    }

    public void setLastDetectedTo(Long lastDetectedTo) {
        this.lastDetectedTo = lastDetectedTo;
    }

    public List<String> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<String> statuses) {
        this.statuses = new ArrayList<>(statuses);
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Long getSelectedServerUuid() {
        return selectedServerUuid;
    }

    public void setSelectedServerUuid(Long selectedServerUuid) {
        this.selectedServerUuid = selectedServerUuid;
    }

    public String getSelectedApplicationId() {
        return selectedApplicationId;
    }

    public void setSelectedApplicationId(String selectedApplicationId) {
        this.selectedApplicationId = selectedApplicationId;
    }

    public int getCurrentOffset() {
        return currentOffset;
    }

    public void setCurrentOffset(int currentOffset) {
        this.currentOffset = currentOffset;
    }

    public String getAppVersionTag() {
        return appVersionTag;
    }

    public void setAppVersionTag(final String appVersionTag) {
        this.appVersionTag = appVersionTag;
    }

    public String getSelectedOrganizationName() {
        return selectedOrganizationName;
    }

    public void setSelectedOrganizationName(String selectedOrganizationName) {
        this.selectedOrganizationName = selectedOrganizationName;
    }
}
