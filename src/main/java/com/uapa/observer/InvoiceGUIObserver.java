package com.uapa.observer;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import com.uapa.model.Invoice;

public class InvoiceGUIObserver implements InvoiceObserver {
    private final JTextArea logArea;

    public InvoiceGUIObserver(JTextArea logArea) {
        this.logArea = logArea;
    }

    @Override
    public void update(Invoice invoice) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[Observer] Factura generada: " + invoice.getId()
                    + " | Total: " + invoice.getTotal() + "\n");
        });
    }
}
