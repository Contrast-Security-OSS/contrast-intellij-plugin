package com.contrastsecurity.core;

public final class UrlConstants {

    public final static String EVENT_DETAILS = "/ng/%s/traces/%s/events/%s/details?expand=skip_links";
    public final static String EVENT_SUMMARY = "/ng/%s/traces/%s/events/summary?expand=skip_links";
    public final static String HTTP_REQUEST = "/ng/%s/traces/%s/httprequest?expand=skip_links";
    public final static String RECOMMENDATION = "/ng/%s/traces/%s/recommendation";
    public final static String TRACE = "/ng/%s/traces/%s/story?expand=skip_links";
    public final static String TRACE_TAGS = "/ng/%s/tags/traces/trace/%s";
    public final static String ORG_TAGS = "/ng/%s/tags/traces";
    public final static String TRACE_TAGS_DELETE = "/ng/%s/tags/trace/%s";
    public final static String STATUS = "/ng/%s/orgtraces/mark";

}
