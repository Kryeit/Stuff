package com.kryeit.stuff.config;


import static com.kryeit.stuff.config.ConfigReader.*;

public class StaticConfig {
    public static final boolean production = true;

    public static final String dbUrl = production
            ? DB_URL
            : "jdbc:postgresql://localhost:5432/postgres";

    public static final String dbUser = production
            ? DB_USER
            : "postgres";
    public static final String dbPassword = production
            ? DB_PASSWORD
            : "lel";
}
