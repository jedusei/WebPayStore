package com.webpay.store;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

public class HomeFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        ListView productListView = root.findViewById(R.id.listView);
        final ProductListAdapter adapter  = new ProductListAdapter(getContext(),App.products);
        productListView.setAdapter(adapter);
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product product = App.products[i];
                if (!product.inCart) {
                    App.cart.add(product);
                    product.inCart = true;
                    Snackbar.make(view,product.name + " added to cart.",Snackbar.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                }
            }
        });
        return root;
    }
}