package com.uapa.service;

import java.util.List;

import com.uapa.model.Invoice;
import com.uapa.observer.InvoiceObserver;

public interface InvoiceService {
    void createInvoice(Invoice invoice);

    List<Invoice> getAllInvoices();
}