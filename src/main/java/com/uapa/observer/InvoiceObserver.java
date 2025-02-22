package com.uapa.observer;

import com.uapa.model.Invoice;

public interface InvoiceObserver {
    void update(Invoice invoice);
}
