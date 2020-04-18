package com.webpay.store;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class CartAdapter extends ArrayAdapter {
    ArrayList<Product> products;
    OnChangeListener listener;

    public CartAdapter(Context context, ArrayList<Product> products) {
        super(context, 0);
        this.products = products;
    }

    @Override
    public int getCount() {
        return (products != null ? products.size() : 0);
    }

    public void setOnChangeListener(OnChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = View.inflate(super.getContext(), R.layout.list_item_cart, null);
        ImageView imageView = convertView.findViewById(R.id.imageView);
        TextView txtName = convertView.findViewById(R.id.txtName);
        TextView txtPrice = convertView.findViewById(R.id.txtPrice);
        ImageButton deleteBtn = convertView.findViewById(R.id.btn_delete);

        final Product product = products.get(position);
        imageView.setImageResource(product.imgResource);
        txtName.setText(product.name);
        txtPrice.setText(String.format("GH₵ %.2f", product.price));
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                product.inCart = false;
                products.remove(position);
                notifyDataSetChanged();
                if (listener != null)
                    listener.onChange(product);
            }
        });

        return convertView;
    }

    public interface OnChangeListener {
        void onChange(Product removedProduct);
    }
}
