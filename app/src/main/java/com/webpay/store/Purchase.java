package com.webpay.store;

import java.util.Date;

public class Purchase {
    public Date date;
    public Product[] products;
    public float amount;
    public String transactionId;
    public boolean refundRequested;
}
