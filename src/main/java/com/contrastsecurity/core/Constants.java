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
package com.contrastsecurity.core;

public interface Constants {
    static final String TEAM_SERVER_URL = "contrast.we.url";
    static final String TEAM_SERVER_URL_VALUE = "https://app.contrastsecurity.com/Contrast/api";
    static final String SERVICE_KEY = "service.key";
    static final String API_KEY = "api.key";
    static final String USERNAME = "username";
    static final String ORGNAME = "orgname";
    static final String ORGUUID = "orguuid";
    static final String SERVER_ID = "serverId";
    static final String APPLICATION_ID = "applicationId";
    static final long ALL_SERVERS = -1l;
    static final String ALL_APPLICATIONS = "All applications";
    static final String BLANK = "";
    static final String MUSTACHE_NL = "{{{nl}}}";
    static final String UNLICENSED = "{{#unlicensed}}";

    static final String ORGANIZATION_LIST = "organizationList";
    static final String DELIMITER = ";";

    public String TAINT = "{{#taint}}";
    public String TAINT_CLOSED = "{{/taint}}";
    public String SPAN_OPENED = "<span";
    public String SPAN_CLOSED = "</span>";
    public String ITALIC_OPENED = "<i>";
    public String ITALIC_CLOSED = "</i>";
    public String SPAN_CLASS_CODE_STRING = "<span class='code-string'>";
    public String SPAN_CLASS_NORMAL_CODE = "<span class='normal-code'>";
    public String SPAN_CLASS_TAINT = "<span class='taint'>";
    public int MAX_WIDTH = 400;

    static final String SORT_BY_SEVERITY = "severity";
    static final String SORT_DESCENDING = "-";
    static final String SORT_BY_TITLE = "title";

    static final String SORT_BY_LAST_TIME_SEEN = "lastTimeSeen";
    static final String SORT_BY_STATUS = "status";

    static final String SEVERITY_LEVEL_NOTE = "Note";
    static final String SEVERITY_LEVEL_LOW = "Low";
    static final String SEVERITY_LEVEL_MEDIUM = "Medium";
    static final String SEVERITY_LEVEL_HIGH = "High";
    static final String SEVERITY_LEVEL_CRITICAL = "Critical";

    static final String VULNERABILITY_STATUS_AUTO_REMEDIATED = "Auto-Remediated";
    static final String VULNERABILITY_STATUS_CONFIRMED = "Confirmed";
    static final String VULNERABILITY_STATUS_SUSPICIOUS = "Suspicious";
    static final String VULNERABILITY_STATUS_NOT_A_PROBLEM = "Not+a+Problem";
    static final String VULNERABILITY_STATUS_REMEDIATED = "Remediated";
    static final String VULNERABILITY_STATUS_REPORTED = "Reported";
    static final String VULNERABILITY_STATUS_FIXED = "Fixed";
    static final String VULNERABILITY_STATUS_BEING_TRACKED = "Being+Tracked";
    static final String VULNERABILITY_STATUS_UNTRACKED = "Untracked";

    static final String LAST_DETECTED_ALL = "All";
    static final String LAST_DETECTED_HOUR = "Last Hour";
    static final String LAST_DETECTED_DAY = "Last Day";
    static final String LAST_DETECTED_WEEK = "Last Week";
    static final String LAST_DETECTED_MONTH = "Last Month";
    static final String LAST_DETECTED_YEAR = "Last Year";
    static final String LAST_DETECTED_CUSTOM = "Custom...";
}
