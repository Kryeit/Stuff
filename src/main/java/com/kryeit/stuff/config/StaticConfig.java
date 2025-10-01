package com.kryeit.stuff.config;


public class StaticConfig {
    public static final boolean enableAnalytics = Boolean.parseBoolean(System.getenv("ENABLE_ANALYTICS"));
}
