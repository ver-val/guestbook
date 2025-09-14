package com.example.guestbook.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {

  private static final String URL =
    "jdbc:h2:file:./data/guest;AUTO_SERVER=TRUE";

  static {
    try {
      Files.createDirectories(Path.of("data"));

      Class.forName("org.h2.Driver");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL);
  }
}
