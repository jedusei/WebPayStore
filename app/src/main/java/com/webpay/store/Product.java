package com.webpay.store;

import androidx.annotation.DrawableRes;

public class Product {
    public int id;
    public String name;
    public float price;
    @DrawableRes
    public int imgResource;
    public boolean inCart = false;

    public Product(int id, String name, float price, @DrawableRes int imgResource) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imgResource = imgResource;
    }
}
