package com.uapa.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class H2DatabaseConnectorTest {

    @Test
    public void testGetConnection_NotNull() {
        H2DatabaseConnector connector = new H2DatabaseConnector();
        Connection connection = connector.getConnection();
        assertNotNull(connection, "La conexión no debería ser nula");
    }

    @Test
    public void testTablesExist() {
        H2DatabaseConnector connector = new H2DatabaseConnector();
        try (Connection connection = connector.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            // Verificar la existencia de la tabla INVOICES
            try (ResultSet rs = metaData.getTables(null, null, "INVOICES", null)) {
                assertTrue(rs.next(), "La tabla INVOICES debería existir");
            }

            // Verificar la existencia de la tabla INVOICE_ITEMS
            try (ResultSet rs = metaData.getTables(null, null, "INVOICE_ITEMS", null)) {
                assertTrue(rs.next(), "La tabla INVOICE_ITEMS debería existir");
            }

        } catch (SQLException e) {
            fail("Se produjo una SQLException: " + e.getMessage());
        }
    }
}
