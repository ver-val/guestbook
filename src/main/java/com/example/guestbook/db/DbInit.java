package com.example.guestbook.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DbInit {

    public static void init() throws SQLException {
        try (Connection con = Db.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate("""
                create table if not exists comments (
                  id bigint generated always as identity primary key,
                  author varchar(64) not null,
                  text varchar(1000) not null,
                  created_at timestamp with time zone default current_timestamp
                )
            """);
        }
    }
}
