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

    Icon CONTRAST_ICON = IconLoader.getIcon("/icons/contrastIcon.png");
    Icon SETTINGS_ICON = IconLoader.getIcon("/icons/settings.png");
    Icon SEVERITY_ICON_CRITICAL = IconLoader.getIcon("/icons/critical.png");
    Icon SEVERITY_ICON_HIGH = IconLoader.getIcon("/icons/high.png");
    Icon SEVERITY_ICON_MEDIUM = IconLoader.getIcon("/icons/medium.png");
    Icon SEVERITY_ICON_LOW = IconLoader.getIcon("/icons/low.png");
    Icon SEVERITY_ICON_NOTE = IconLoader.getIcon("/icons/note.png");
    Icon EXTERNAL_LINK_ICON = IconLoader.getIcon("/icons/externalLink.png");
    Icon DETAILS_ICON = IconLoader.getIcon("/icons/details.png");
    Icon UNLICENSED_ICON = IconLoader.getIcon("/icons/unlicensed.png");
    Icon REFRESH_ICON = IconLoader.getIcon("/icons/refresh_tab.png");
    Icon FILTER_ICON = IconLoader.getIcon("/icons/filter.png");
    Icon FIRST_PAGE_ICON = IconLoader.getIcon("/icons/first_page.png");
    Icon LAST_PAGE_ICON = IconLoader.getIcon("/icons/last_page.png");
    Icon PREVIOUS_PAGE_ICON = IconLoader.getIcon("/icons/previous_page.png");
    Icon NEXT_PAGE_ICON = IconLoader.getIcon("/icons/next_page.png");
}
