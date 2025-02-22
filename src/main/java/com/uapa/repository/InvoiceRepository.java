package com.uapa.repository;

import java.util.List;
import java.util.Optional;

import com.uapa.model.Invoice;

public interface InvoiceRepository {
    void saveInvoice(Invoice invoice);

    Optional<Invoice> getInvoiceById(String id);

    List<Invoice> getAllInvoices();

}
