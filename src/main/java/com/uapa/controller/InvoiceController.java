package com.uapa.controller;

import java.util.List;
import com.uapa.factory.InvoiceFactory;
import com.uapa.model.Invoice;
import com.uapa.model.InvoiceItem;
import com.uapa.service.InvoiceService;

public class InvoiceController {
    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    public void generateInvoice() {
        Invoice invoice = InvoiceFactory.createDummyInvoice();
        System.out.println("[Controller] Generando factura: " + invoice.getId());
        service.createInvoice(invoice);
    }

    public void generateRealInvoice(List<InvoiceItem> items) {
        Invoice invoice = InvoiceFactory.createInvoice(items);
        System.out.println("[Controller] Generando factura real: " + invoice.getId());
        service.createInvoice(invoice);
    }

    public List<Invoice> getAllInvoices() {
        return service.getAllInvoices();
    }
}
