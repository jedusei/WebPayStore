package com.webpay.store;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArrayMap;
import android.widget.Toast;

import androidx.core.util.Consumer;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public final class App extends Application {
    public static Product[] products = {
            new Product(1, "iPhone X", 2300, R.drawable.iphone),
            new Product(2, "PS4 DualShock Controller", 120, R.drawable.ps4_controller),
            new Product(3, "HP Inspiron 3573", 2500, R.drawable.hp_inspiron_3573),
            new Product(4, "Nintendo Switch", 1200, R.drawable.nintendo_switch),
            new Product(5, "Sneakers", 100, R.drawable.shoe),
            new Product(6, "DELL Vostro 3480", 2700, R.drawable.dell_vostro_3480),
            new Product(7, "Men's Slim-Fit Trousers", 120, R.drawable.trousers),
            new Product(8, "Women's High Heel Sandals", 60, R.drawable.heel_sandals),
            new Product(9, "5-Piece Handbag Set", 250, R.drawable.handbag),
            new Product(10, "Sleeveless Dress", 85, R.drawable.dress)
    };
    public static ArrayList<Product> cart = new ArrayList<>();
    public static ArrayList<Purchase> purchases;

    private SharedPreferences preferences;
    private static RequestQueue volleyRequestQueue;
    private static String BASE_URL = "https://webpaygh.herokuapp.com/api";
    private static String CLIENT_ID = "5e19600ddb1417811b87ef16";
    private static String CLIENT_SECRET = "cixyv6DAQisu1nWxins8";
    private static String accessToken;
    private static Date tokenExpiryDate;
    private static String currentTransactionId;

    @Override
    public void onCreate() {
        super.onCreate();
        volleyRequestQueue = Volley.newRequestQueue(this);
        // Load data
        preferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        for (int i = 0; i < products.length; i++) {
            Product product = products[i];
            product.inCart = preferences.getBoolean("product_" + product.id + "_in_cart", false);
            if (product.inCart)
                cart.add(product);
        }
        int purchaseCount = preferences.getInt("purchase_count", 0);
        purchases = new ArrayList<>(purchaseCount);
        for (int i = 0; i < purchaseCount; i++) {
            String prefix = "purchase_" + i;
            Purchase purchase = new Purchase();
            purchase.date = new Date(preferences.getLong(prefix + "_date", 0));
            purchase.transactionId = preferences.getString(prefix + "_transactionId", null);
            purchase.amount = preferences.getFloat(prefix + "_amount", 0);
            int count = preferences.getInt(prefix + "_count", 0);
            purchase.products = new Product[count];
            for (int j = 0; j < count; j++) {
                int productId = preferences.getInt(prefix + "_" + j + "_id", 0);
                for (int k = 0; k < products.length; k++) {
                    Product p = products[k];
                    if (p.id == productId) {
                        purchase.products[j] = p;
                        break;
                    }
                }
            }
            purchases.add(purchase);
        }

        accessToken = preferences.getString("access_token", null);
        tokenExpiryDate = new Date(preferences.getLong("token_expiry_date", 0));
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            SharedPreferences.Editor editor = preferences.edit();
            for (int i = 0; i < products.length; i++) {
                editor.putBoolean("product_" + i + "_in_cart", products[i].inCart);
            }
            editor.putInt("purchase_count", purchases.size());
            for (int j = 0; j < purchases.size(); j++) {
                String prefix = "purchase_" + j;
                Purchase p = purchases.get(j);
                editor.putLong(prefix + "_date", p.date.getTime());
                editor.putString(prefix + "_transactionId", p.transactionId);
                editor.putFloat(prefix + "_amount", p.amount);
                editor.putInt(prefix + "_count", p.products.length);
                for (int k = 0; k < p.products.length; k++) {
                    editor.putInt(prefix + "_" + k + "_id", p.products[k].id);
                }
            }
            editor.putString("access_token", accessToken);
            editor.putLong("token_expiry_date", tokenExpiryDate.getTime());
            editor.apply();
        }
    }

    private static void refreshToken(final Consumer<Boolean> callback) {
        if (tokenExpiryDate.after(new Date())) {
            callback.accept(true);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, BASE_URL + "/token", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            accessToken = response.getString("token");
                            tokenExpiryDate = new Date(response.getLong("bestBefore"));
                            callback.accept(true);
                        } catch (Exception e) {
                            callback.accept(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.accept(false);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new ArrayMap<>();
                headers.put("WP-Client-Id", CLIENT_ID);
                headers.put("WP-Client-Secret", CLIENT_SECRET);
                return headers;
            }
        };
        volleyRequestQueue.add(request);
    }

    private static JSONObject getCartInfo() {
        float totalPrice = 0;
        for (int i = 0; i < cart.size(); i++) {
            totalPrice += cart.get(i).price;
        }
        try {
            JSONObject result = new JSONObject();
            result.put("amount", totalPrice);
            JSONArray jsonCart = new JSONArray();
            for (int i = 0; i < cart.size(); i++) {
                Product item = cart.get(i);
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("name", item.name);
                jsonItem.put("price", item.price);
                jsonItem.put("quantity", 1);
                jsonCart.put(jsonItem);
            }
            result.put("cart", jsonCart);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static void initPayment(final Consumer<String> callback) {
        refreshToken(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean b) {
                if (!b) {
                    callback.accept(null);
                } else {
                    JsonObjectRequest request = new JsonObjectRequest(
                            Request.Method.POST, BASE_URL + "/start-payment", getCartInfo(),
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        currentTransactionId = response.getString("transactionId");
                                        callback.accept(response.getString("checkoutURL"));
                                    } catch (Exception e) {
                                        callback.accept(null);
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    callback.accept(null);
                                }
                            }
                    ) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new ArrayMap<>();
                            headers.put("Authorization", "Bearer " + accessToken);
                            return headers;
                        }
                    };
                    volleyRequestQueue.add(request);
                }
            }
        });
    }

    public static void endPayment(Boolean accepted) {
        if (!accepted) {
            currentTransactionId = null;
            return;
        }
        // Clear cart and create new purchase info
        Purchase purchase = new Purchase();
        purchase.date = new Date();
        purchase.transactionId = currentTransactionId;
        currentTransactionId = null;
        purchase.amount = 0;
        purchase.products = new Product[cart.size()];
        for (int i = 0; i < purchase.products.length; i++) {
            Product p = cart.get(i);
            purchase.amount += p.price;
            purchase.products[i] = p;
            p.inCart = false;
        }
        purchase.products = cart.toArray(new Product[0]);
        purchases.add(purchase);
        cart.clear();
    }

    public static void refundSale(final Purchase purchase, String reason, final Consumer<Boolean> callback) {
        try {
            final JSONObject data = new JSONObject()
                    .put("transactionId", purchase.transactionId)
                    .put("reason", reason);
            refreshToken(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean b) {
                    if (!b) {
                        callback.accept(false);
                    } else {
                        volleyRequestQueue.add(
                                new JsonObjectRequest(
                                        Request.Method.POST, BASE_URL + "/refund", data,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                purchase.refundRequested=true;
                                                callback.accept(true);
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                callback.accept(false);
                                            }
                                        }) {
                                    @Override
                                    public Map<String, String> getHeaders() {
                                        Map<String, String> headers = new ArrayMap<>();
                                        headers.put("Authorization", "Bearer " + accessToken);
                                        return headers;
                                    }

                                    @Override
                                    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                                        return Response.success(null, null);
                                    }
                                }
                        );
                    }
                }
            });
        } catch (Exception e) {
            callback.accept(false);
        }
    }
}
