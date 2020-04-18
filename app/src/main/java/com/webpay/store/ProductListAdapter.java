package com.webpay.store;

import android.content.Context;
import android.media.Image;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class ProductListAdapter extends ArrayAdapter<Product> {
    Product[] products;

    public ProductListAdapter(Context context, Product[] products) {
        super(context, 0);
        this.products = products;
    }

    @Override
    public int getCount() {
        return (products != null ? products.length : 0);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = View.inflate(super.getContext(), R.layout.list_item_product, null);

        ImageView imageView = convertView.findViewById(R.id.imageView);
        TextView txtName = convertView.findViewById(R.id.txtName);
        TextView txtPrice = convertView.findViewById(R.id.txtPrice);
        final ImageView imgAdd = convertView.findViewById(R.id.imgAdd);
        final ImageView imgCart = convertView.findViewById(R.id.imgCart);

        final Product product = products[position];
        imageView.setImageResource(product.imgResource);
        txtName.setText(product.name);
        txtPrice.setText(String.format("GH₵ %.2f", product.price));
        imgAdd.setVisibility(product.inCart ? View.GONE : View.VISIBLE);
        imgCart.setVisibility(product.inCart ? View.VISIBLE : View.GONE);

        return convertView;
    }
}
