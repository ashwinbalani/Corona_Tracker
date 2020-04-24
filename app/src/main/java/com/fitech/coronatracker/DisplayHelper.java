package com.fitech.coronatracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class DisplayHelper {
    public static void showSnackbar(String message, String dismissText, Context ctx, Snackbar snackbar, View v) {
        int snackbar_length = Snackbar.LENGTH_SHORT;
        //snackbar_length = Snackbar.LENGTH_LONG;

        if(snackbar != null) {
            if(snackbar.isShown() == true) {
                return;
            }
        }
        snackbar = Snackbar.make(v, message, snackbar_length)
                .setAction(dismissText, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.setVisibility(View.GONE);
                    }
                })
                .setActionTextColor(ctx.getResources().getColor(android.R.color.holo_blue_dark ));
        snackbar.show();
    }

    public static void showSnackbar(Activity activity, String message) {
        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        snackbar.setAction("Ok", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    public static void showSnackbarWithCustomDuration(Activity activity,String message, int SnackbarDuration) {
        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), message, SnackbarDuration);
        snackbar.setAction("Ok", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    // slide the view from below itself to the current position
    public static void slideUp(View view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public static void slideDown(View view){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }
    public static void showSoftInput(View view)
    {
        InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
    }

    public static void hideSoftInput(View view)
    {
        InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                enableDisableViewGroup((ViewGroup) view, enabled);
            }
        }
    }

    public static void openPermissionSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + activity.getPackageName()));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static void showToast(Context ctx,String Message, int Duration) {
        Toast.makeText(ctx,Message,Duration).show();
    }
    public static void showToastAtPosition(Context ctx,String Message, int Duration, float x, float y) {
        Toast t = Toast.makeText(ctx,Message,Duration);
        t.setGravity(Gravity.TOP,(int)x,(int)y);
        t.show();
    }
    public static void showToastAtPosition(Context ctx,String Message, int Duration, int gravity) {
        Toast t = Toast.makeText(ctx,Message,Duration);
        t.setGravity(gravity,0,0);
        t.show();
    }
}
