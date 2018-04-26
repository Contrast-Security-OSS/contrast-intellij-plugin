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

import java.awt.*;

public final class Constants {
    public static final String TEAM_SERVER_URL = "contrast.we.url";
    public static final String TEAM_SERVER_URL_VALUE = "https://app.contrastsecurity.com/Contrast/api";
    public static final String SERVICE_KEY = "service.key";
    public static final String API_KEY = "api.key";
    public static final String USERNAME = "username";
    public static final String ORGNAME = "orgname";
    public static final String ORGUUID = "orguuid";
    public static final String SERVER_ID = "serverId";
    public static final String APPLICATION_ID = "applicationId";
    public static final long ALL_SERVERS = -1L;
    public static final String ALL_APPLICATIONS = "All applications";
    public static final String BLANK = "";
    public static final String MUSTACHE_NL = "{{{nl}}}";
    public static final String UNLICENSED = "{{#unlicensed}}";
    private static final String OPEN_TAG_PARAGRAPH = "{{#paragraph}}";
    private static final String CLOSE_TAG_PARAGRAPH = "{{/paragraph}}";
    public static final String OPEN_TAG_LINK = "{{#link}}";
    public static final String CLOSE_TAG_LINK = "{{/link}}";
    public static final String OPEN_TAG_BAD_PARAM = "{{#badParam}}";
    public static final String CLOSE_TAG_BAD_PARAM = "{{/badParam}}";
    public static final String OPEN_TAG_JAVA_BLOCK = "{{#javaBlock}}";
    public static final String CLOSE_TAG_JAVA_BLOCK = "{{/javaBlock}}";
    public static final String OPEN_TAG_GOOD_PARAM = "{{#goodParam}}";
    public static final String CLOSE_TAG_GOOD_PARAM = "{{/goodParam}}";
    public static final String OPEN_TAG_C_SHARP_BLOCK = "{{#csharpBlock}}";
    public static final String CLOSE_TAG_C_SHARP_BLOCK = "{{/csharpBlock}}";
    public static final String OPEN_TAG_HTML_BLOCK = "{{#htmlBlock}}";
    public static final String CLOSE_TAG_HTML_BLOCK = "{{/htmlBlock}}";
    public static final String OPEN_TAG_JAVASCRIPT_BLOCK = "{{#javascriptBlock}}";
    public static final String CLOSE_TAG_JAVASCRIPT_BLOCK = "{{/javascriptBlock}}";
    public static final String OPEN_TAG_XML_BLOCK = "{{#xmlBlock}}";
    public static final String CLOSE_TAG_XML_BLOCK = "{{/xmlBlock}}";
    private static final String OPEN_TAG_HEADER = "{{#header}}";
    private static final String CLOSE_TAG_HEADER = "{{/header}}";
    private static final String LINK_TAG_1 = "{{link1}}";
    private static final String LINK_TAG_2 = "{{link2}}";
    private static final String OPEN_TAG_CODE = "{{#code}}";
    private static final String CLOSE_TAG_CODE = "{{/code}}";
    private static final String OPEN_TAG_P = "{{#p}}";
    private static final String CLOSE_TAG_P = "{{/p}}";
    private static final String OPEN_TAG_UNORDERED_LIST = "{{#unorderedList}}";
    private static final String CLOSE_TAG_UNORDERED_LIST = "{{/unorderedList}}";
    private static final String OPEN_TAG_LIST_ELEMENT = "{{#listElement}}";
    private static final String CLOSE_TAG_LIST_ELEMENT = "{{/listElement}}";
    private static final String OPEN_TAG_FOCUS = "{{#focus}}";
    private static final String CLOSE_TAG_FOCUS = "{{/focus}}";
    private static final String OPEN_TAG_BAD_CONFIG = "{{#badConfig}}";
    private static final String CLOSE_TAG_BAD_CONFIG = "{{/badConfig}}";
    private static final String OPEN_TAG_BLOCK = "{{#block}}";
    private static final String CLOSE_TAG_BLOCK = "{{/block}}";
    private static final String OPEN_TAG_BLOCK_QUOTE = "{{#blockQuote}}";
    private static final String CLOSE_TAG_BLOCK_QUOTE = "{{/blockQuote}}";
    private static final String OPEN_TAG_EMPHASIZE = "{{#emphasize}}";
    private static final String CLOSE_TAG_EMPHASIZE = "{{/emphasize}}";
    private static final String OPEN_TAG_EXAMPLE_TEXT = "{{#exampleText}}";
    private static final String CLOSE_TAG_EXAMPLE_TEXT = "{{/exampleText}}";
    private static final String OPEN_TAG_GOOD_CONFIG = "{{#goodConfig}}";
    private static final String CLOSE_TAG_GOOD_CONFIG = "{{/goodConfig}}";
    private static final String OPEN_TAG_ORDERED_LIST = "{{#orderedList}}";
    private static final String CLOSE_TAG_ORDERED_LIST = "{{/orderedList}}";
    private static final String OPEN_TAG_RISK_EVIDENCE = "{{#riskEvidence}}";
    private static final String CLOSE_TAG_RISK_EVIDENCE = "{{/riskEvidence}}";
    private static final String OPEN_TAG_TABLE = "{{#table}}";
    private static final String CLOSE_TAG_TABLE = "{{/table}}";
    private static final String OPEN_TAG_TABLE_BODY = "{{#tableBody}}";
    private static final String CLOSE_TAG_TABLE_BODY = "{{/tableBody}}";
    private static final String OPEN_TAG_TABLE_CELL = "{{#tableCell}}";
    private static final String CLOSE_TAG_TABLE_CELL = "{{/tableCell}}";
    private static final String OPEN_TAG_TABLE_CELL_ALT = "{{#tableCellAlt}}";
    private static final String CLOSE_TAG_TABLE_CELL_ALT = "{{/tableCellAlt}}";
    private static final String OPEN_TAG_TABLE_HEADER = "{{#tableHeader}}";
    private static final String CLOSE_TAG_TABLE_HEADER = "{{/tableHeader}}";
    private static final String OPEN_TAG_TABLE_HEADER_ROW = "{{#tableHeaderRow}}";
    private static final String CLOSE_TAG_TABLE_HEADER_ROW = "{{/tableHeaderRow}}";
    private static final String OPEN_TAG_TABLE_ROW = "{{#tableRow}}";
    private static final String CLOSE_TAG_TABLE_ROW = "{{/tableRow}}";

    public static final String[] MUSTACHE_CONSTANTS = {OPEN_TAG_CODE, CLOSE_TAG_CODE, OPEN_TAG_P, CLOSE_TAG_P, OPEN_TAG_PARAGRAPH,
            CLOSE_TAG_PARAGRAPH, OPEN_TAG_LINK, CLOSE_TAG_LINK, OPEN_TAG_HEADER, CLOSE_TAG_HEADER, LINK_TAG_1,
            LINK_TAG_2, MUSTACHE_NL, OPEN_TAG_UNORDERED_LIST, CLOSE_TAG_UNORDERED_LIST, OPEN_TAG_LIST_ELEMENT,
            CLOSE_TAG_LIST_ELEMENT, OPEN_TAG_FOCUS, CLOSE_TAG_FOCUS, OPEN_TAG_BAD_CONFIG, CLOSE_TAG_BAD_CONFIG,
            OPEN_TAG_BLOCK, CLOSE_TAG_BLOCK, OPEN_TAG_BLOCK_QUOTE, CLOSE_TAG_BLOCK_QUOTE, OPEN_TAG_EMPHASIZE,
            CLOSE_TAG_EMPHASIZE, OPEN_TAG_EXAMPLE_TEXT, CLOSE_TAG_EXAMPLE_TEXT, OPEN_TAG_GOOD_CONFIG,
            CLOSE_TAG_GOOD_CONFIG, OPEN_TAG_ORDERED_LIST, CLOSE_TAG_ORDERED_LIST, OPEN_TAG_RISK_EVIDENCE,
            CLOSE_TAG_RISK_EVIDENCE, OPEN_TAG_TABLE, CLOSE_TAG_TABLE, OPEN_TAG_TABLE_BODY, CLOSE_TAG_TABLE_BODY,
            OPEN_TAG_TABLE_CELL, CLOSE_TAG_TABLE_CELL, OPEN_TAG_TABLE_CELL_ALT, CLOSE_TAG_TABLE_CELL_ALT,
            OPEN_TAG_TABLE_HEADER, CLOSE_TAG_TABLE_HEADER, OPEN_TAG_TABLE_HEADER_ROW, CLOSE_TAG_TABLE_HEADER_ROW,
            OPEN_TAG_TABLE_ROW, CLOSE_TAG_TABLE_ROW};

    public static final String ORGANIZATION_LIST = "organizationList";
    public static final String DELIMITER = ";";

    // #0DA1A9
    public static final Color LINK_COLOR = new Color(13, 161, 169);
    // #969494
    public static final Color UNLICENSED_COLOR = new Color(150, 148, 148);
    // #bfbfbf
    public static final Color RULE_COLOR = new Color(191, 191, 191);
    public static final int REFRESH_DELAY = 5 * 60 * 1000; // 5 minutes
    // green - #aecd43 (r=174, g=205, b=67)
    // yellow - #f7b600 (r=247, g=182, b=0)
    // red - #e63025 (r=230, g=48, b=37)
    public static final Color CREATION_COLOR = new Color(230, 48, 37);
    public static final Color P20_COLOR = new Color(247, 182, 0);
    public static final Color TAG_COLOR = new Color(174, 205, 67);
    public static final Color GOOD_PARAM_COLOR = new Color(0, 128, 0);
    // #165BAD
    public static final Color LINK_COLOR2 = new Color(22, 91, 173);
    // #999999
    public static final Color CONTENT_COLOR = new Color(153, 153, 153);
    // #1b7eb1 27,126,177
    public static final Color CODE_COLOR = new Color(27, 126, 177);
    // #e0f2ef

    public static final Color ITEM_BACKGROUND_COLOR = new Color(224, 242, 239);

    public static final Color EVENT_TYPE_ICON_COLOR_CREATION = new Color(247, 182, 0);
    public static final Color EVENT_TYPE_ICON_COLOR_PROPAGATION = new Color(153, 153, 153);
    public static final Color EVENT_TYPE_ICON_COLOR_TRIGGER = new Color(230, 48, 37);


    public static final String TAINT = "{{#taint}}";
    public static final String TAINT_CLOSED = "{{/taint}}";
    public static final String SPAN_OPENED = "<span";
    public static final String SPAN_CLOSED = "</span>";
    public static final String ITALIC_OPENED = "<i>";
    public static final String ITALIC_CLOSED = "</i>";
    public static final String SPAN_CLASS_CODE_STRING = "<span class='code-string'>";
    public static final String SPAN_CLASS_NORMAL_CODE = "<span class='normal-code'>";
    public static final String SPAN_CLASS_TAINT = "<span class='taint'>";
    public static final int MAX_WIDTH = 400;

    public static final String SORT_BY_SEVERITY = "severity";
    public static final String SORT_DESCENDING = "-";
    public static final String SORT_BY_TITLE = "title";

    public static final String SORT_BY_LAST_TIME_SEEN = "lastTimeSeen";
    public static final String SORT_BY_STATUS = "status";
    public static final String SORT_BY_APPLICATION_NAME = "application.name";

    public static final String SEVERITY_LEVEL_NOTE = "Note";
    public static final String SEVERITY_LEVEL_LOW = "Low";
    public static final String SEVERITY_LEVEL_MEDIUM = "Medium";
    public static final String SEVERITY_LEVEL_HIGH = "High";
    public static final String SEVERITY_LEVEL_CRITICAL = "Critical";

    public static final String VULNERABILITY_STATUS_AUTO_REMEDIATED = "Auto-Remediated";
    public static final String VULNERABILITY_STATUS_CONFIRMED = "Confirmed";
    public static final String VULNERABILITY_STATUS_SUSPICIOUS = "Suspicious";
    public static final String VULNERABILITY_STATUS_REMEDIATED = "Remediated";
    public static final String VULNERABILITY_STATUS_REPORTED = "Reported";
    public static final String VULNERABILITY_STATUS_FIXED = "Fixed";
    public static final String VULNERABILITY_STATUS_BEING_TRACKED = "Being+Tracked";
    public static final String VULNERABILITY_STATUS_UNTRACKED = "Untracked";

    public static final String LAST_DETECTED_ALL = "All";
    public static final String LAST_DETECTED_HOUR = "Last Hour";
    public static final String LAST_DETECTED_DAY = "Last Day";
    public static final String LAST_DETECTED_WEEK = "Last Week";
    public static final String LAST_DETECTED_MONTH = "Last Month";
    public static final String LAST_DETECTED_YEAR = "Last Year";
    public static final String LAST_DETECTED_CUSTOM = "Custom...";

    public static final String TRACE_STORY_HEADER_CHAPTERS = "What happened?";
    public static final String TRACE_STORY_HEADER_RISK = "What's the risk?";


    public static final String CLASS_METHOD = "Class.Method: ";
    public static final String OBJECT = "Object: ";
    public static final String RETURN = "Return: ";
    public static final String PARAMETERS = "Parameters: ";
    public static final String STACK_TRACE = "Stack Trace: ";
    public static final String UNLICENSED_DIALOG_MESSAGE = "The vulnerability is associated with an unlicensed application. Please have your Contrast administrator apply a license from the TeamServer web application in order to view the vulnerability finding.";
    public static final String UNLICENSED_DIALOG_TITLE = "Unlicensed";

    public static final String HTTP_REQUEST_TAB_TITLE = "HTTP Request";
    public static final String EVENTS_TAB_TITLE = "Events";
    public static final String LINK_DELIM = "$$LINK_DELIM$$";
    public static final String FRAGMENT_TYPE_NORMAL_CODE = "NORMAL_CODE";
    public static final String FRAGMENT_TYPE_CODE_STRING = "CODE_STRING";
    public static final String FRAGMENT_TYPE_TEXT = "TEXT";
    public static final String FRAGMENT_TYPE_TAINT_VALUE = "TAINT_VALUE";

    public static final String VULNERABILITY_STATUS_NOT_A_PROBLEM = "Not+a+Problem";
    public static final String VULNERABILITY_STATUS_NOT_A_PROBLEM_COMBO_BOX_ITEM = "Not a Problem";
    public static final String VULNERABILITY_STATUS_NOT_A_PROBLEM_API_REQUEST_STRING = "NotAProblem";

    public static final String[] STATUS_ARRAY = {VULNERABILITY_STATUS_CONFIRMED, VULNERABILITY_STATUS_SUSPICIOUS,
            VULNERABILITY_STATUS_NOT_A_PROBLEM_COMBO_BOX_ITEM, VULNERABILITY_STATUS_REMEDIATED, VULNERABILITY_STATUS_REPORTED,
            VULNERABILITY_STATUS_FIXED};

    private static final String REASON_URL_IS_ONLY_ACCESSIBLE_BY_TRUSTED_POWER_USERS = "URL is only accessible by trusted power users";
    private static final String REASON_FALSE_POSITIVE = "False positive";
    private static final String REASON_GOES_THROUGH_AN_INTERNAL_SECURITY_CONTROL = "Goes through an internal security control";
    private static final String REASON_ATTACK_IS_DEFENDED_BY_AN_EXTERNAL_CONTROL = "Attack is defended by an external control";
    private static final String REASON_OTHER = "Other";

    public static final String[] REASON_ARRAY = {REASON_URL_IS_ONLY_ACCESSIBLE_BY_TRUSTED_POWER_USERS, REASON_FALSE_POSITIVE,
            REASON_GOES_THROUGH_AN_INTERNAL_SECURITY_CONTROL, REASON_ATTACK_IS_DEFENDED_BY_AN_EXTERNAL_CONTROL, REASON_OTHER};

    public static final String TRACE_FILTER_TYPE_APP_VERSION_TAGS = "appversiontags";
}
