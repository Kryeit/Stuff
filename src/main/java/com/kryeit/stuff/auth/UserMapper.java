package com.kryeit.stuff.auth;

import io.github.fabricators_of_create.porting_lib.event.client.InteractEvents;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UserMapper implements RowMapper<User> {
    @Override
    public User map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new User(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("username"),
                rs.getTimestamp("creation"),
                rs.getTimestamp("last_seen"),
                Arrays.asList(Arrays.stream(rs.getString("roles").split(",")).map(User.Role::valueOf).toArray(User.Role[]::new))
        );
    }
}