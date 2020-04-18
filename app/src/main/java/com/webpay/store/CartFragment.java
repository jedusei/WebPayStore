package com.webpay.store;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

public class CartFragment extends Fragment {

    private View bottomLayout;
    private TextView txtTotal;
    private ProgressDialog progressDialog;
    private AlertDialog errorDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_cart, container, false);
        ListView listView = root.findViewById(R.id.listView);
        listView.setEmptyView(root.findViewById(R.id.emptyView));
        final CartAdapter adapter = new CartAdapter(getContext(), App.cart);
        final CoordinatorLayout coordinatorLayout = root.findViewById(R.id.coordinator);
        adapter.setOnChangeListener(new CartAdapter.OnChangeListener() {
            @Override
            public void onChange(Product product) {
                Snackbar.make(coordinatorLayout, product.name + " removed from cart.", Snackbar.LENGTH_SHORT).show();
                refreshBottomLayout();
            }
        });
        listView.setAdapter(adapter);

        bottomLayout = root.findViewById(R.id.bottomLayout);
        txtTotal = root.findViewById(R.id.txtTotal);
        refreshBottomLayout();

        Button checkoutBtn = root.findViewById(R.id.btn_checkout);
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkout();
            }
        });

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        errorDialog = new AlertDialog.Builder(getContext())
                .setTitle("Error")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton("OK",null)
                .create();

        return root;
    }

    private void refreshBottomLayout() {
        if (App.cart.size() == 0)
            bottomLayout.setVisibility(View.GONE);
        else {
            float totalPrice = getTotalPrice();
            txtTotal.setText(String.format("GH₵ %.2f", totalPrice));
        }
    }

    private float getTotalPrice() {
        float total = 0;
        for (int i = 0; i < App.cart.size(); i++) {
            total += App.cart.get(i).price;
        }
        return total;
    }

    private void checkout() {
        progressDialog.show();
        App.initPayment(new Consumer<String>() {
            @Override
            public void accept(String checkoutURL) {
                if (checkoutURL == null) {
                    progressDialog.hide();
                    errorDialog.show();
                    return;
                }

                try {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setShowTitle(true);
                    builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(getContext(), Uri.parse(checkoutURL));
                }
                catch(Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(checkoutURL)));
                }
                progressDialog.hide();
            }
        });
    }

}