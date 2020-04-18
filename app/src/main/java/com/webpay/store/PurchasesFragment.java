package com.webpay.store;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

public class PurchasesFragment extends Fragment {

    ProgressDialog progressDialog;
    AlertDialog errorDialog;
    AlertDialog refundReasonDialog;
    EditText txtRefundReason;
    Purchase curPurchase;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_purchases, container, false);
        ListView listView = root.findViewById(R.id.listView);
        listView.setEmptyView(root.findViewById(R.id.emptyView));
        final PurchaseListAdapter adapter = new PurchaseListAdapter(getContext(), App.purchases);
        adapter.setOnRefundClickListener(new PurchaseListAdapter.RefundClickListener() {
            @Override
            public void onClick(int position) {
                curPurchase = App.purchases.get(position);
                refundReasonDialog.show();
            }
        });
        listView.setAdapter(adapter);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        errorDialog = new AlertDialog.Builder(getContext())
                .setTitle("Error")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton("OK", null)
                .create();

        View refundDialogView = inflater.inflate(R.layout.dialog_refund, null);
        txtRefundReason = refundDialogView.findViewById(R.id.txtReason);
        refundReasonDialog = new AlertDialog.Builder(getContext())
                .setTitle("Request a Refund")
                .setView(refundDialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final Purchase p = curPurchase;
                        progressDialog.show();
                        App.refundSale(p, txtRefundReason.getText().toString(), new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean success) {
                                progressDialog.hide();
                                if (!success)
                                    errorDialog.show();
                                else {
                                    adapter.notifyDataSetChanged();
                                    Snackbar.make(root, "Your request is being processed.", Snackbar.LENGTH_SHORT).show();
                                    Handler h = new Handler(getContext().getMainLooper());
                                    h.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            App.purchases.remove(p);
                                            adapter.notifyDataSetChanged();
                                            Snackbar.make(root, "Your purchase has been refunded.", Snackbar.LENGTH_SHORT).show();
                                        }
                                    }, 7000);
                                }
                            }
                        });
                    }
                })
                .create();

        return root;
    }

    private void doRefund(int purchaseIndex) {

    }

}