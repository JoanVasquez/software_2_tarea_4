package com.uapa.event;

import com.uapa.model.Invoice;

public interface InvoiceEventPublisher {
    void publishInvoiceCreated(Invoice invoice);
}