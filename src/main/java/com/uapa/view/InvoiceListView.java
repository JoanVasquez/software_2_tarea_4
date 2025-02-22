package com.uapa.view;

import com.uapa.controller.InvoiceController;
import com.uapa.model.Invoice;
import com.uapa.model.InvoiceItem;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InvoiceListView extends JFrame {
    private final InvoiceController controller;
    private JTable table;
    private DefaultTableModel tableModel;

    public InvoiceListView(InvoiceController controller) {
        this.controller = controller;
        setTitle("Listado de Facturas");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Modelo de tabla con columnas en español: ID Factura, Fecha, Total, Producto,
        // Cantidad y Precio
        tableModel = new DefaultTableModel(
                new Object[] { "ID Factura", "Fecha", "Total", "Producto", "Cantidad", "Precio" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refrescar");
        refreshButton.addActionListener(e -> loadInvoices());
        add(refreshButton, BorderLayout.SOUTH);

        loadInvoices();
    }

    private void loadInvoices() {
        List<Invoice> invoices = controller.getAllInvoices();
        tableModel.setRowCount(0);
        for (Invoice invoice : invoices) {
            // Si la factura no tiene ítems, se agrega una fila con celdas vacías para los
            // detalles del ítem.
            if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
                tableModel.addRow(new Object[] {
                        invoice.getId(),
                        invoice.getDate().toString(),
                        invoice.getTotal(),
                        "", "", ""
                });
            } else {
                // Por cada ítem se agrega una fila con los datos de la factura y los datos del
                // ítem.
                for (InvoiceItem item : invoice.getItems()) {
                    tableModel.addRow(new Object[] {
                            invoice.getId(),
                            invoice.getDate().toString(),
                            Double.parseDouble(String.valueOf(item.getPrice())) * item.getQuantity(),
                            item.getProduct(),
                            item.getQuantity(),
                            item.getPrice()
                    });
                }
            }
        }
    }
}
