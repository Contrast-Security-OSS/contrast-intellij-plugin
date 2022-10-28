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
package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface ContrastPluginIcons {

    Icon CONTRAST_ICON = IconLoader.getIcon("/icons/contrastImageIcon.png", ContrastPluginIcons.class);
    Icon SETTINGS_ICON = IconLoader.getIcon("/icons/settings.png", ContrastPluginIcons.class);
    Icon SEVERITY_ICON_CRITICAL = IconLoader.getIcon("/icons/critical.png", ContrastPluginIcons.class);
    Icon SEVERITY_ICON_HIGH = IconLoader.getIcon("/icons/high.png", ContrastPluginIcons.class);
    Icon SEVERITY_ICON_MEDIUM = IconLoader.getIcon("/icons/medium.png", ContrastPluginIcons.class);
    Icon SEVERITY_ICON_LOW = IconLoader.getIcon("/icons/low.png", ContrastPluginIcons.class);
    Icon SEVERITY_ICON_NOTE = IconLoader.getIcon("/icons/note.png", ContrastPluginIcons.class);
    Icon EXTERNAL_LINK_ICON = IconLoader.getIcon("/icons/externalLink.png", ContrastPluginIcons.class);
    Icon DETAILS_ICON = IconLoader.getIcon("/icons/details.png", ContrastPluginIcons.class);
    Icon UNLICENSED_ICON = IconLoader.getIcon("/icons/unlicensed.png", ContrastPluginIcons.class);
    Icon REFRESH_ICON = IconLoader.getIcon("/icons/refresh_tab.png", ContrastPluginIcons.class);
    Icon FILTER_ICON = IconLoader.getIcon("/icons/filter.png", ContrastPluginIcons.class);
    Icon FIRST_PAGE_ICON = IconLoader.getIcon("/icons/first_page.png", ContrastPluginIcons.class);
    Icon LAST_PAGE_ICON = IconLoader.getIcon("/icons/last_page.png", ContrastPluginIcons.class);
    Icon PREVIOUS_PAGE_ICON = IconLoader.getIcon("/icons/previous_page.png", ContrastPluginIcons.class);
    Icon NEXT_PAGE_ICON = IconLoader.getIcon("/icons/next_page.png", ContrastPluginIcons.class);
    Icon TAG_ICON = IconLoader.getIcon("/icons/tag.png", ContrastPluginIcons.class);
    Icon REMOVE_ICON = IconLoader.getIcon("/icons/remove.png", ContrastPluginIcons.class);
}
