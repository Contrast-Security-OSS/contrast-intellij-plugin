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
import com.contrastsecurity.core.Util;
import com.contrastsecurity.core.cache.ContrastCache;
import com.contrastsecurity.core.cache.Key;
import com.contrastsecurity.core.internal.preferences.OrganizationConfig;
import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.RequestConstants;
import com.contrastsecurity.http.RuleSeverity;
import com.contrastsecurity.http.ServerFilterForm;
import com.contrastsecurity.http.TraceFilterForm;
import com.contrastsecurity.models.Application;
import com.contrastsecurity.models.EventSummaryResponse;
import com.contrastsecurity.models.HttpRequestResponse;
import com.contrastsecurity.models.RecommendationResponse;
import com.contrastsecurity.models.Server;
import com.contrastsecurity.models.StoryResponse;
import com.contrastsecurity.models.TagsResponse;
import com.contrastsecurity.models.Trace;
import com.contrastsecurity.models.Traces;
import com.contrastsecurity.sdk.ContrastSDK;
import com.contrastsecurity.sdk.UserAgentProduct;
import com.intellij.openapi.project.Project;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.proxy.CommonProxy;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ContrastUtil {

    public static final int PAGE_LIMIT = 20;
    public static final int SERVER_REQUEST_LIMIT = 100;

    public ContrastUtil() {
    }

    public static ContrastSDK getContrastSDK(Project project) {

        ContrastSDK sdk = null;
        OrganizationConfig organizationConfig = getSelectedOrganizationConfig(project);
        if (organizationConfig != null) {
            Proxy proxy = getIdeaDefinedProxy(organizationConfig.getTeamServerUrl()) != null
                    ? getIdeaDefinedProxy(organizationConfig.getTeamServerUrl()) : Proxy.NO_PROXY;

            InputStream ins = ContrastUtil.class.getClassLoader().getResourceAsStream("contrast.properties");
            Properties gradleProperty = new Properties();
            try {
                gradleProperty.load(ins);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            sdk = new ContrastSDK.Builder(organizationConfig.getUsername(), organizationConfig.getServiceKey(), organizationConfig.getApiKey())
                    .withApiUrl(organizationConfig.getTeamServerUrl())
                    .withProxy(proxy)
                    .withUserAgentProduct(UserAgentProduct.of("INTELLIJ_INTEGRATION", gradleProperty.getProperty("version")))
                    .build();

            sdk.setReadTimeout(5000);
        }
        return sdk;
    }

    @Nullable
    public static Proxy getIdeaDefinedProxy(@NotNull String url) {

        final List<Proxy> proxies = CommonProxy.getInstance().select(URI.create(url));
        if (proxies != null && !proxies.isEmpty()) {
            for (Proxy proxy : proxies) {
                if (HttpConfigurable.isRealProxy(proxy) && Proxy.Type.HTTP.equals(proxy.type())) {
                    return proxy;
                }
            }
        }
        return null;
    }

    public static OrganizationConfig getSelectedOrganizationConfig(Project project) {
        ContrastPersistentStateComponent contrastPersistentStateComponent = ContrastPersistentStateComponent.getInstance();
        Map<String, String> organizations = contrastPersistentStateComponent.getOrganizations();

        ContrastFilterPersistentStateComponent contrastFilterPersistentStateComponent = ContrastFilterPersistentStateComponent.getInstance(project);

        String selectedOrganizationName = contrastFilterPersistentStateComponent.getSelectedOrganizationName();
        if (selectedOrganizationName == null || selectedOrganizationName.isEmpty()) {
            selectedOrganizationName = contrastPersistentStateComponent.getSelectedOrganizationName();
            contrastFilterPersistentStateComponent.setSelectedOrganizationName(selectedOrganizationName);
        }

        return Util.getOrganizationConfigFromString(organizations.get(selectedOrganizationName), Constants.DELIMITER);
    }

    public static boolean isTraceLicensed(Trace trace) {
        boolean licensed = true;

        String title = trace.getTitle();
        int indexOfUnlicensed = title.indexOf(Constants.UNLICENSED);
        if (indexOfUnlicensed != -1) {
            licensed = false;
        }
        return licensed;
    }

    public static String filterHeaders(String data, String separator) {
        String[] lines = data.split(separator);
        String[] headers = {"authorization:", "intuit_tid:", ":"};

        String[] filtered = Arrays.stream(lines).filter(line -> !Arrays.stream(headers).anyMatch(header -> {
            if (line.toLowerCase().contains(header)) {
                if (!header.equals(":")) {
                    return true;
                } else {
                    if (line.split(":")[0].toLowerCase().contains("token")) {
                        return true;
                    }
                }
            }
            return false;
        })).toArray(String[]::new);

        return String.join(separator, filtered);
    }

    public static TraceFilterForm getTraceFilterFormFromContrastFilterPersistentStateComponent(Project project) {

        ContrastFilterPersistentStateComponent contrastFilterPersistentStateComponent = ContrastFilterPersistentStateComponent.getInstance(project);

        Long serverId = Constants.ALL_SERVERS;
        if (contrastFilterPersistentStateComponent.getSelectedServerUuid() != null) {
            serverId = contrastFilterPersistentStateComponent.getSelectedServerUuid();
        }

        int offset = contrastFilterPersistentStateComponent.getCurrentOffset();

        TraceFilterForm traceFilterForm = Util.getTraceFilterForm(offset, PAGE_LIMIT);
        if (serverId != Constants.ALL_SERVERS) {
            traceFilterForm = Util.getTraceFilterForm(serverId, offset, PAGE_LIMIT);
        }

        if (contrastFilterPersistentStateComponent.getSeverities() != null && !contrastFilterPersistentStateComponent.getSeverities().isEmpty()) {
            traceFilterForm.setSeverities(getRuleSeveritiesEnumFromList(contrastFilterPersistentStateComponent.getSeverities()));
        }

        if (contrastFilterPersistentStateComponent.getLastDetectedFrom() != null) {
            LocalDateTime localDateTimeFrom = getLocalDateTimeFromMillis(contrastFilterPersistentStateComponent.getLastDetectedFrom());
            traceFilterForm.setStartDate(getDateFromLocalDateTime(localDateTimeFrom));
        }

        if (contrastFilterPersistentStateComponent.getLastDetectedTo() != null) {
            LocalDateTime localDateTimeTo = getLocalDateTimeFromMillis(contrastFilterPersistentStateComponent.getLastDetectedTo());
            traceFilterForm.setEndDate(getDateFromLocalDateTime(localDateTimeTo));
        }
        if (contrastFilterPersistentStateComponent.getStatuses() != null && !contrastFilterPersistentStateComponent.getStatuses().isEmpty()) {
            traceFilterForm.setStatus(contrastFilterPersistentStateComponent.getStatuses());
        }

        traceFilterForm.setTracked(contrastFilterPersistentStateComponent.isBeingTracked());
        traceFilterForm.setUntracked(contrastFilterPersistentStateComponent.isUntracked());

        if (contrastFilterPersistentStateComponent.getCurrentOffset() != 0) {
            traceFilterForm.setOffset(contrastFilterPersistentStateComponent.getCurrentOffset());
        }

        if (contrastFilterPersistentStateComponent.getSort() != null) {
            traceFilterForm.setSort(contrastFilterPersistentStateComponent.getSort());
        } else {
            traceFilterForm.setSort(Constants.SORT_DESCENDING + Constants.SORT_BY_SEVERITY);
        }
        if (contrastFilterPersistentStateComponent.getAppVersionTag() != null && !contrastFilterPersistentStateComponent.getAppVersionTag().isEmpty()) {
            traceFilterForm.setAppVersionTags(Collections.singletonList(contrastFilterPersistentStateComponent.getAppVersionTag()));
        }

        traceFilterForm.setExpand(EnumSet.of(TraceFilterForm.TraceExpandValue.APPLICATION));

        return traceFilterForm;
    }

    private static EnumSet<RuleSeverity> getRuleSeveritiesEnumFromList(List<String> severities) {

        EnumSet<RuleSeverity> ruleSeverities = EnumSet.noneOf(RuleSeverity.class);
        if (!severities.isEmpty()) {
            for (String severity : severities) {
                if (severity.equals(RuleSeverity.NOTE.toString())) {
                    ruleSeverities.add(RuleSeverity.NOTE);
                } else if (severity.equals(RuleSeverity.LOW.toString())) {
                    ruleSeverities.add(RuleSeverity.LOW);
                } else if (severity.equals(RuleSeverity.MEDIUM.toString())) {
                    ruleSeverities.add(RuleSeverity.MEDIUM);
                } else if (severity.equals(RuleSeverity.HIGH.toString())) {
                    ruleSeverities.add(RuleSeverity.HIGH);
                } else if (severity.equals(RuleSeverity.CRITICAL.toString())) {
                    ruleSeverities.add(RuleSeverity.CRITICAL);
                }
            }
        }
        return ruleSeverities;
    }

    private static LocalDateTime getLocalDateTimeFromMillis(Long millis) {
        Date date = new Date(millis);
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private static Date getDateFromLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        } else {
            return null;
        }
    }

    public static int getNumOfPages(final int totalElements) {
        int numOfPages = 1;
        if (totalElements % ContrastUtil.PAGE_LIMIT > 0) {
            numOfPages = totalElements / ContrastUtil.PAGE_LIMIT + 1;
        } else {
            if (totalElements != 0) {
                numOfPages = totalElements / ContrastUtil.PAGE_LIMIT;
            }
        }
        return numOfPages;
    }

    public static List<Server> retrieveServers(ContrastSDK contrastSDK, String orgUuid) {
        List<Server> servers = new ArrayList<>();
        List<Server> serverSubList;

        ServerFilterForm serverFilter = new ServerFilterForm();
        serverFilter.setLimit(SERVER_REQUEST_LIMIT);
        serverFilter.setOffset(0);
        serverFilter.setExpand(EnumSet.of(ServerFilterForm.ServerExpandValue.APPLICATIONS));

        try {
            do{
                serverSubList = contrastSDK.getServersWithFilter(orgUuid, serverFilter).getServers();
                servers.addAll(serverSubList);
                serverFilter.setOffset(serverFilter.getOffset() + SERVER_REQUEST_LIMIT);
                Thread.sleep(50);
            } while(serverSubList.size() == SERVER_REQUEST_LIMIT);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return servers;
    }

    public static List<Application> retrieveApplications(ContrastSDK contrastSDK, String orgUuid) {
        List<Application> applications = new ArrayList<>();
        try {
           applications.addAll(contrastSDK.getApplicationsNames(orgUuid).getApplications());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return applications;
    }

    public static String formatLinks(String text) {

        String formattedText = text;
        String[] links = StringUtils.substringsBetween(formattedText, Constants.OPEN_TAG_LINK, Constants.CLOSE_TAG_LINK);
        if (links != null && links.length > 0) {
            for (String link : links) {
                int indexOfDelimiter = link.indexOf(Constants.LINK_DELIM);
                String formattedLink = link.substring(indexOfDelimiter + Constants.LINK_DELIM.length()) + " (" + link.substring(0, indexOfDelimiter) + ")";

                formattedText = formattedText.substring(0, formattedText.indexOf(link)) + formattedLink + formattedText.substring(formattedText.indexOf(link) + link.length());
            }
        }

        return formattedText;
    }


    public static String parseMustache(String text) {
        try {
            text = URLDecoder.decode(text, "UTF-8");
        } catch (Exception ignored) {
        }
        text = StringEscapeUtils.unescapeHtml4(text);

        for (String mustache : Constants.MUSTACHE_CONSTANTS) {
            text = text.replace(mustache, Constants.BLANK);
        }

        return text;
    }

    public static void updateOrganizationConfig() {
        ContrastPersistentStateComponent contrastPersistentStateComponent = ContrastPersistentStateComponent.getInstance();
        if (contrastPersistentStateComponent != null) {
            Map<String, String> organizationMap = contrastPersistentStateComponent.getOrganizations();
            for (Map.Entry<String, String> entry : organizationMap.entrySet()) {
                String organizationString = entry.getValue();

                String[] org = StringUtils.split(organizationString, Constants.DELIMITER);
                if (org.length == 2) {
                    String teamServerUrl = contrastPersistentStateComponent.getTeamServerUrl();
                    String username = contrastPersistentStateComponent.getUsername();
                    String serviceKey = contrastPersistentStateComponent.getServiceKey();

                    OrganizationConfig organizationConfig = new OrganizationConfig(teamServerUrl, username, serviceKey, org[0], org[1]);
                    String newOrganizationString = Util.getStringFromOrganizationConfig(organizationConfig, Constants.DELIMITER);

                    entry.setValue(newOrganizationString);
                }
            }
        }
    }

    public static Integer getLineNumber(String stacktrace) {
        int index = stacktrace.lastIndexOf(':');
        if (index >= 0) {
            String numText = stacktrace.substring(index + 1);
            index = numText.indexOf(')');
            if (index >= 0) {
                numText = numText.substring(0, index);
            }
            try {
                return Integer.parseInt(numText);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    public static String getTypeName(String stacktrace) {
        int start = stacktrace.lastIndexOf('(');
        int end = stacktrace.indexOf(':');
        if (start >= 0 && end > start) {
            String typeName = stacktrace.substring(start + 1, end);
            int indexOfExtension = typeName.indexOf(".java");
            if (indexOfExtension > 0) {
                typeName = typeName.substring(0, indexOfExtension);
                String qualifier = stacktrace.substring(0, start);
                start = qualifier.lastIndexOf('.');
                if (start >= 0) {
                    start = ((String) qualifier.subSequence(0, start)).lastIndexOf('.');
                    if (start == -1) {
                        start = 0;
                    }
                }
                if (start >= 0) {
                    qualifier = qualifier.substring(0, start);
                }
                if (qualifier.length() > 0) {
                    typeName = qualifier + "." + typeName;
                }
            }
            return typeName;
        } else if (start >= 0) {
            String qualifier = stacktrace.substring(0, start);
            start = qualifier.lastIndexOf('.');
            if (start >= 0) {
                qualifier = qualifier.substring(0, start);
            }
            if (qualifier.length() > 0) {
                return qualifier;
            }
        }
        return null;
    }

    @Nullable
    public static String getFilePath(String projectName, String typeName, String delimiter) {
        int start = typeName.indexOf(projectName) > -1 ? typeName.indexOf(projectName) + projectName.length() + delimiter.length() : 0;
        if (start >= 0) {
            String filePathString = typeName.substring(start);
            return filePathString;
        }
        return null;
    }

    public static Traces getTraces(ContrastSDK contrastSDK, String orgUuid, String appId, TraceFilterForm form)
            throws IOException, UnauthorizedException {

        Traces traces = null;
        if (contrastSDK != null) {
            if (Constants.ALL_APPLICATIONS.equals(appId)) {
                traces = contrastSDK.getTracesInOrg(orgUuid, form);
            } else if (!Constants.ALL_APPLICATIONS.equals(appId)) {
                traces = contrastSDK.getTraces(orgUuid, appId, form);
            }
        }
        return traces;
    }

    public static StoryResponse getStory(ContrastSDK contrastSDK, ContrastCache contrastCache, Key key) throws IOException, UnauthorizedException {
        StoryResponse story = contrastCache.getStoryResources().get(key);

        if (story == null) {
            story = contrastSDK.getStory(key.getOrgUuid(), key.getTraceId());
            contrastCache.getStoryResources().put(key, story);
        }

        return story;
    }

    public static EventSummaryResponse getEventSummary(ContrastSDK contrastSDK, ContrastCache contrastCache, Key key) throws IOException, UnauthorizedException {

        EventSummaryResponse eventSummaryResource = contrastCache.getEventSummaryResources().get(key);
        if (eventSummaryResource == null) {
            eventSummaryResource = contrastSDK.getEventSummary(key.getOrgUuid(), key.getTraceId());
            contrastCache.getEventSummaryResources().put(key, eventSummaryResource);
        }
        return eventSummaryResource;
    }

    public static TagsResponse getTags(ContrastSDK contrastSDK, ContrastCache contrastCache, Key key) throws IOException, UnauthorizedException {
        TagsResponse tagsResource = contrastCache.getTagsResources().get(key);

        if (tagsResource == null) {
            if (key.getTraceId() != null) {
                tagsResource = contrastSDK.getTagsByTrace(key.getOrgUuid(), key.getTraceId());
            } else {
                tagsResource = contrastSDK.getTraceTagsByOrganization(key.getOrgUuid());
            }
            contrastCache.getTagsResources().put(key, tagsResource);
        }
        return tagsResource;
    }

    public static HttpRequestResponse getHttpRequest(ContrastSDK contrastSDK, ContrastCache contrastCache, Key key) throws IOException, UnauthorizedException {

        HttpRequestResponse httpRequestResource = contrastCache.getHttpRequestResources().get(key);
        if (httpRequestResource == null) {
            httpRequestResource = contrastSDK.getHttpRequest(key.getOrgUuid(), key.getTraceId());
            contrastCache.getHttpRequestResources().put(key, httpRequestResource);
        }
        return httpRequestResource;
    }

    public static RecommendationResponse getRecommendationResource(ContrastSDK contrastSDK, ContrastCache contrastCache, Key key) throws IOException, UnauthorizedException {

        RecommendationResponse recommendationResource = contrastCache.getRecommendationResources().get(key);
        if (recommendationResource == null) {
            recommendationResource = contrastSDK.getRecommendation(key.getOrgUuid(), key.getTraceId());
            contrastCache.getRecommendationResources().put(key, recommendationResource);
        }
        return recommendationResource;
    }

    public static URL getOverviewUrl(String traceId, Project project) throws MalformedURLException {
        String teamServerUrl = ContrastUtil.getSelectedOrganizationConfig(project).getTeamServerUrl();
        teamServerUrl = teamServerUrl.trim();
        if (teamServerUrl.endsWith("/api")) {
            teamServerUrl = teamServerUrl.substring(0, teamServerUrl.length() - 4);
        }
        if (teamServerUrl.endsWith("/api/")) {
            teamServerUrl = teamServerUrl.substring(0, teamServerUrl.length() - 5);
        }
        String urlStr = teamServerUrl + "/static/ng/index.html#/" + ContrastUtil.getSelectedOrganizationConfig(project).getUuid() + "/vulns/" + traceId + "/overview";
        return new URL(urlStr);
    }
}
