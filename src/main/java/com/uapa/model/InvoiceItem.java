package com.uapa.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {
    private String product;
    private int quantity;
    private double price;
}
