package com.uapa.config;

import java.sql.*;

public class H2DatabaseConnector implements DatabaseConnector {
    public H2DatabaseConnector() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 Driver not found", e);
        }
    }

    @Override
    public Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
            // Crear tabla si no existe (responsabilidad de configuración inicial)
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS invoices ("
                    + "id VARCHAR(255) PRIMARY KEY, "
                    + "date DATE, "
                    + "total DOUBLE)");
            // Crear tabla invoice_items si no existe
            stmt.execute("CREATE TABLE IF NOT EXISTS invoice_items ("
                    + "invoice_id VARCHAR(255), "
                    + "product VARCHAR(255), "
                    + "quantity INT, "
                    + "price DOUBLE)");
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to H2", e);
        }
    }
}