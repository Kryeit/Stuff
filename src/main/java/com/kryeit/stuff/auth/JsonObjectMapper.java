package com.kryeit.stuff.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JsonObjectMapper implements ColumnMapper<JsonObject> {
    @Override
    public JsonObject map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        String json = r.getString(columnNumber);
        return json == null ? new JsonObject() : JsonParser.parseString(json).getAsJsonObject();
    }
}
