package com.uapa.config;

import java.sql.Connection;

public interface DatabaseConnector {
    Connection getConnection();
}