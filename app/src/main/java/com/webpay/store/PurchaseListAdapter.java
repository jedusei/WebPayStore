package com.webpay.store;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.List;

public class PurchaseListAdapter extends ArrayAdapter<Purchase> {
    List<Purchase> purchases;
    SimpleDateFormat sdf = new SimpleDateFormat("h:mm a, MMM dd yyyy");
    RefundClickListener listener;

    public PurchaseListAdapter(Context context, List<Purchase> purchases) {
        super(context, 0);
        this.purchases = purchases;
    }

    @Override
    public int getCount() {
        return (purchases != null ? purchases.size() : 0);
    }

    public void setOnRefundClickListener(RefundClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = View.inflate(getContext(), R.layout.list_item_purchase, null);
        TextView txtTitle = convertView.findViewById(R.id.txtTitle);
        TextView txtPrice = convertView.findViewById(R.id.txtPrice);
        TextView txtDate = convertView.findViewById(R.id.txtDate);
        Button refundBtn = convertView.findViewById(R.id.btn);

        final Purchase purchase = purchases.get(position);
        txtPrice.setText(String.format("GH₵ %.2f", purchase.amount));
        txtDate.setText(sdf.format(purchase.date));
        if (purchase.refundRequested) {
            refundBtn.setEnabled(false);
        }
        else {
            refundBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(position);
                }
            });
        }

        StringBuilder itemListBuilder = new StringBuilder();
        for (int i = 0; i < purchase.products.length; i++) {
            if (i != purchase.products.length - 1)
                itemListBuilder.append(purchase.products[i].name + ", ");
            else
                itemListBuilder.append(purchase.products[i].name);
        }
        txtTitle.setText(itemListBuilder.toString());

        return convertView;
    }

    interface RefundClickListener {
        void onClick(int position);
    }
}
