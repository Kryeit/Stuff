package com.kryeit.stuff;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream("stuff-config.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String ipqsKey = properties.getProperty("ipqsKey");
    public static final String clickHousePassword = properties.getProperty("clickhousePassword");
    public static final boolean dev = Boolean.parseBoolean(properties.getProperty("dev", "false"));
}
