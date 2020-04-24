package com.fitech.coronatracker.web;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class App extends Application
{
    public static final String TAG = App.class.getSimpleName();
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized App getInstance() {
        return mInstance;
    }

    public boolean isConnected() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

    }

    public RequestQueue getRequestQueue() {

        if (mRequestQueue == null) {

            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {

        getRequestQueue();

        if (mImageLoader == null) {

            mImageLoader = new ImageLoader(this.mRequestQueue, new LruBitmapCache());
        }

        return this.mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {

        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        req.setRetryPolicy(new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(Constants.DEFAULT_VOLLEY_TIMEOUT),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {

        if (mRequestQueue != null) {

            mRequestQueue.cancelAll(tag);
        }
    }

    public void setFcmToken(final String fcmToken)
    {
        //Store this token internally as well
        //LoginManager.setFCMToken(getApplicationContext(),fcmToken);

        Log.d("FCM_TOKEN",fcmToken);

        sendFCMTokenOnServer(fcmToken);
    }
    public void sendFCMTokenOnServer(final String fcmToken)
    {
        //Send this token online
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, Constants.SERVER_OPERATIONS, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("APP_FCM_TOKEN", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                //params.put("operation", Constants.METHOD_UPDATE_FCM_TOKEN_ANDROID);
                params.put("fcm_token",fcmToken + "");
                //params.put("user_id",LoginManager.getUserAccountId(getApplicationContext()));

                return params;
            }
        };
        App.getInstance().addToRequestQueue(jsonReq);
    }
}
