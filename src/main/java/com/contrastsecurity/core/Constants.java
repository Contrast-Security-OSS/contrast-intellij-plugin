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

public interface Constants {
    String TEAM_SERVER_URL = "contrast.we.url";
    String TEAM_SERVER_URL_VALUE = "https://app.contrastsecurity.com/Contrast/api";
    String SERVICE_KEY = "service.key";
    String API_KEY = "api.key";
    String USERNAME = "username";
    String ORGNAME = "orgname";
    String ORGUUID = "orguuid";
    String SERVER_ID = "serverId";
    String APPLICATION_ID = "applicationId";
    long ALL_SERVERS = -1l;
    String ALL_APPLICATIONS = "All applications";
    String BLANK = "";
    String MUSTACHE_NL = "{{{nl}}}";
    String UNLICENSED = "{{#unlicensed}}";
    String OPEN_TAG_PARAGRAPH = "{{#paragraph}}";
    String CLOSE_TAG_PARAGRAPH = "{{/paragraph}}";
    String OPEN_TAG_LINK = "{{#link}}";
    String CLOSE_TAG_LINK = "{{/link}}";
    String OPEN_TAG_BAD_PARAM = "{{#badParam}}";
    String CLOSE_TAG_BAD_PARAM = "{{/badParam}}";
    String OPEN_TAG_JAVA_BLOCK = "{{#javaBlock}}";
    String CLOSE_TAG_JAVA_BLOCK = "{{/javaBlock}}";
    String OPEN_TAG_GOOD_PARAM = "{{#goodParam}}";
    String CLOSE_TAG_GOOD_PARAM = "{{/goodParam}}";
    String OPEN_TAG_C_SHARP_BLOCK = "{{#csharpBlock}}";
    String CLOSE_TAG_C_SHARP_BLOCK = "{{/csharpBlock}}";
    String OPEN_TAG_HTML_BLOCK = "{{#htmlBlock}}";
    String CLOSE_TAG_HTML_BLOCK = "{{/htmlBlock}}";
    String OPEN_TAG_JAVASCRIPT_BLOCK = "{{#javascriptBlock}}";
    String CLOSE_TAG_JAVASCRIPT_BLOCK = "{{/javascriptBlock}}";
    String OPEN_TAG_XML_BLOCK = "{{#xmlBlock}}";
    String CLOSE_TAG_XML_BLOCK = "{{/xmlBlock}}";
    String OPEN_TAG_HEADER = "{{#header}}";
    String CLOSE_TAG_HEADER = "{{/header}}";
    String LINK_TAG_1 = "{{link1}}";
    String LINK_TAG_2 = "{{link2}}";
    String OPEN_TAG_CODE = "{{#code}}";
    String CLOSE_TAG_CODE = "{{/code}}";
    String OPEN_TAG_P = "{{#p}}";
    String CLOSE_TAG_P = "{{/p}}";
    String OPEN_TAG_UNORDERED_LIST = "{{#unorderedList}}";
    String CLOSE_TAG_UNORDERED_LIST = "{{/unorderedList}}";
    String OPEN_TAG_LIST_ELEMENT = "{{#listElement}}";
    String CLOSE_TAG_LIST_ELEMENT = "{{/listElement}}";
    String OPEN_TAG_FOCUS = "{{#focus}}";
    String CLOSE_TAG_FOCUS = "{{/focus}}";
    String OPEN_TAG_BAD_CONFIG = "{{#badConfig}}";
    String CLOSE_TAG_BAD_CONFIG = "{{/badConfig}}";
    String OPEN_TAG_BLOCK = "{{#block}}";
    String CLOSE_TAG_BLOCK = "{{/block}}";
    String OPEN_TAG_BLOCK_QUOTE = "{{#blockQuote}}";
    String CLOSE_TAG_BLOCK_QUOTE = "{{/blockQuote}}";
    String OPEN_TAG_EMPHASIZE = "{{#emphasize}}";
    String CLOSE_TAG_EMPHASIZE = "{{/emphasize}}";
    String OPEN_TAG_EXAMPLE_TEXT = "{{#exampleText}}";
    String CLOSE_TAG_EXAMPLE_TEXT = "{{/exampleText}}";
    String OPEN_TAG_GOOD_CONFIG = "{{#goodConfig}}";
    String CLOSE_TAG_GOOD_CONFIG = "{{/goodConfig}}";
    String OPEN_TAG_ORDERED_LIST = "{{#orderedList}}";
    String CLOSE_TAG_ORDERED_LIST = "{{/orderedList}}";
    String OPEN_TAG_RISK_EVIDENCE = "{{#riskEvidence}}";
    String CLOSE_TAG_RISK_EVIDENCE = "{{/riskEvidence}}";
    String OPEN_TAG_TABLE = "{{#table}}";
    String CLOSE_TAG_TABLE = "{{/table}}";
    String OPEN_TAG_TABLE_BODY = "{{#tableBody}}";
    String CLOSE_TAG_TABLE_BODY = "{{/tableBody}}";
    String OPEN_TAG_TABLE_CELL = "{{#tableCell}}";
    String CLOSE_TAG_TABLE_CELL = "{{/tableCell}}";
    String OPEN_TAG_TABLE_CELL_ALT = "{{#tableCellAlt}}";
    String CLOSE_TAG_TABLE_CELL_ALT = "{{/tableCellAlt}}";
    String OPEN_TAG_TABLE_HEADER = "{{#tableHeader}}";
    String CLOSE_TAG_TABLE_HEADER = "{{/tableHeader}}";
    String OPEN_TAG_TABLE_HEADER_ROW = "{{#tableHeaderRow}}";
    String CLOSE_TAG_TABLE_HEADER_ROW = "{{/tableHeaderRow}}";
    String OPEN_TAG_TABLE_ROW = "{{#tableRow}}";
    String CLOSE_TAG_TABLE_ROW = "{{/tableRow}}";

    String[] MUSTACHE_CONSTANTS = {OPEN_TAG_CODE, CLOSE_TAG_CODE, OPEN_TAG_P, CLOSE_TAG_P, OPEN_TAG_PARAGRAPH,
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

    String ORGANIZATION_LIST = "organizationList";
    String DELIMITER = ";";

    // #0DA1A9
    Color LINK_COLOR = new Color(13, 161, 169);
    // #969494
    Color UNLICENSED_COLOR = new Color(150, 148, 148);
    // #bfbfbf
    Color RULE_COLOR = new Color(191, 191, 191);
    int REFRESH_DELAY = 5 * 60 * 1000; // 5 minutes
    // green - #aecd43 (r=174, g=205, b=67)
    // yellow - #f7b600 (r=247, g=182, b=0)
    // red - #e63025 (r=230, g=48, b=37)
    Color CREATION_COLOR = new Color(230, 48, 37);
    Color P20_COLOR = new Color(247, 182, 0);
    Color TAG_COLOR = new Color(174, 205, 67);
    Color GOOD_PARAM_COLOR = new Color(0, 128, 0);
    // #165BAD
    Color LINK_COLOR2 = new Color(22, 91, 173);
    // #999999
    Color CONTENT_COLOR = new Color(153, 153, 153);
    // #1b7eb1 27,126,177
    Color CODE_COLOR = new Color(27, 126, 177);
    // #e0f2ef
    Color ITEM_BACKGROUND_COLOR = new Color(224, 242, 239);

    Color EVENT_TYPE_ICON_COLOR_CREATION = new Color(247, 182, 0);
    Color EVENT_TYPE_ICON_COLOR_PROPAGATION = new Color(153, 153, 153);
    Color EVENT_TYPE_ICON_COLOR_TRIGGER = new Color(230, 48, 37);


    String TAINT = "{{#taint}}";
    String TAINT_CLOSED = "{{/taint}}";
    String SPAN_OPENED = "<span";
    String SPAN_CLOSED = "</span>";
    String ITALIC_OPENED = "<i>";
    String ITALIC_CLOSED = "</i>";
    String SPAN_CLASS_CODE_STRING = "<span class='code-string'>";
    String SPAN_CLASS_NORMAL_CODE = "<span class='normal-code'>";
    String SPAN_CLASS_TAINT = "<span class='taint'>";
    int MAX_WIDTH = 400;

    String SORT_BY_SEVERITY = "severity";
    String SORT_DESCENDING = "-";
    String SORT_BY_TITLE = "title";

    String SORT_BY_LAST_TIME_SEEN = "lastTimeSeen";
    String SORT_BY_STATUS = "status";
    String SORT_BY_APPLICATION_NAME = "application.name";

    String SEVERITY_LEVEL_NOTE = "Note";
    String SEVERITY_LEVEL_LOW = "Low";
    String SEVERITY_LEVEL_MEDIUM = "Medium";
    String SEVERITY_LEVEL_HIGH = "High";
    String SEVERITY_LEVEL_CRITICAL = "Critical";

    String VULNERABILITY_STATUS_AUTO_REMEDIATED = "Auto-Remediated";
    String VULNERABILITY_STATUS_CONFIRMED = "Confirmed";
    String VULNERABILITY_STATUS_SUSPICIOUS = "Suspicious";
    String VULNERABILITY_STATUS_NOT_A_PROBLEM = "Not+a+Problem";
    String VULNERABILITY_STATUS_REMEDIATED = "Remediated";
    String VULNERABILITY_STATUS_REPORTED = "Reported";
    String VULNERABILITY_STATUS_FIXED = "Fixed";
    String VULNERABILITY_STATUS_BEING_TRACKED = "Being+Tracked";
    String VULNERABILITY_STATUS_UNTRACKED = "Untracked";

    String LAST_DETECTED_ALL = "All";
    String LAST_DETECTED_HOUR = "Last Hour";
    String LAST_DETECTED_DAY = "Last Day";
    String LAST_DETECTED_WEEK = "Last Week";
    String LAST_DETECTED_MONTH = "Last Month";
    String LAST_DETECTED_YEAR = "Last Year";
    String LAST_DETECTED_CUSTOM = "Custom...";

    String TRACE_STORY_HEADER_CHAPTERS = "What happened?";
    String TRACE_STORY_HEADER_RISK = "What's the risk?";


    String CLASS_METHOD = "Class.Method: ";
    String OBJECT = "Object: ";
    String RETURN = "Return: ";
    String PARAMETERS = "Parameters: ";
    String STACK_TRACE = "Stack Trace: ";
    String UNLICENSED_DIALOG_MESSAGE = "The vulnerability is associated with an unlicensed application. Please have your Contrast administrator apply a license from the TeamServer web application in order to view the vulnerability finding.";
    String UNLICENSED_DIALOG_TITLE = "Unlicensed";

    String HTTP_REQUEST_TAB_TITLE = "HTTP Request";
    String EVENTS_TAB_TITLE = "Events";
    String LINK_DELIM = "$$LINK_DELIM$$";
    String FRAGMENT_TYPE_NORMAL_CODE = "NORMAL_CODE";
    String FRAGMENT_TYPE_CODE_STRING = "CODE_STRING";
    String FRAGMENT_TYPE_TEXT = "TEXT";
    String FRAGMENT_TYPE_TAINT_VALUE = "TAINT_VALUE";

}
