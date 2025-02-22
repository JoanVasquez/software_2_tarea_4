package com.uapa.repository;

import com.uapa.config.DatabaseConnector;
import com.uapa.model.Invoice;
import com.uapa.model.InvoiceItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvoiceRepositoryImpl implements InvoiceRepository {
    private final DatabaseConnector connector;

    public InvoiceRepositoryImpl(DatabaseConnector connector) {
        this.connector = connector;
    }

    @Override
    public void saveInvoice(Invoice invoice) {
        try (Connection connection = connector.getConnection()) {
            // Desactivar el auto-commit para asegurar la transacción
            connection.setAutoCommit(false);

            // Insertar la factura
            PreparedStatement psInvoice = connection.prepareStatement(
                    "INSERT INTO invoices (id, date, total) VALUES (?, ?, ?)");
            psInvoice.setString(1, invoice.getId());
            psInvoice.setDate(2, java.sql.Date.valueOf(invoice.getDate()));
            psInvoice.setDouble(3, invoice.getTotal());
            psInvoice.executeUpdate();

            // Insertar cada ítem asociado a la factura (si existen)
            if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
                PreparedStatement psItem = connection.prepareStatement(
                        "INSERT INTO invoice_items (invoice_id, product, quantity, price) VALUES (?, ?, ?, ?)");
                for (InvoiceItem item : invoice.getItems()) {
                    psItem.setString(1, invoice.getId());
                    psItem.setString(2, item.getProduct());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setDouble(4, item.getPrice());
                    psItem.addBatch();
                }
                psItem.executeBatch();
                psItem.close();
            }

            // Confirmar la transacción
            connection.commit();
            System.out.println("[Repository] Factura guardada: " + invoice.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Invoice> getInvoiceById(String id) {
        try (Connection connection = connector.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM invoices WHERE id = ?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Invoice invoice = Invoice.builder()
                        .id(rs.getString("id"))
                        .date(rs.getDate("date").toLocalDate())
                        .total(rs.getDouble("total"))
                        // Se omite la lista de items para simplificar.
                        .build();
                return Optional.of(invoice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Invoice> getAllInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        String query = "SELECT i.id, i.date, i.total, " +
                "ii.product, ii.quantity, ii.price " +
                "FROM invoices i " +
                "LEFT JOIN invoice_items ii ON i.id = ii.invoice_id";
        try (Connection connection = connector.getConnection();
                PreparedStatement ps = connection.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {

            // Usamos un LinkedHashMap para agrupar los ítems por factura y mantener el
            // orden
            java.util.Map<String, Invoice> invoiceMap = new java.util.LinkedHashMap<>();

            while (rs.next()) {
                String id = rs.getString("id");
                Invoice invoice = invoiceMap.get(id);
                if (invoice == null) {
                    invoice = Invoice.builder()
                            .id(id)
                            .date(rs.getDate("date").toLocalDate())
                            .total(rs.getDouble("total"))
                            // Inicializamos la lista de ítems (vacía si no hay registros en invoice_items)
                            .items(new ArrayList<>())
                            .build();
                    invoiceMap.put(id, invoice);
                }
                // Si existen datos de ítem, se agregan a la factura
                String product = rs.getString("product");
                if (product != null) {
                    InvoiceItem item = InvoiceItem.builder()
                            .product(product)
                            .quantity(rs.getInt("quantity"))
                            .price(rs.getDouble("price"))
                            .build();
                    invoice.getItems().add(item);
                }
            }
            invoices.addAll(invoiceMap.values());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

}
