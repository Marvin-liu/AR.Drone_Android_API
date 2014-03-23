package com.parrot.freeflight.activities.base;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;
import com.parrot.freeflight.R;
import com.parrot.freeflight.ui.StatusBar;

/**
 * Created by Yang Zhang on 2014/3/22.
 */
public class DashboardActivityBase extends BaseActivity implements View.OnClickListener {

    protected static final String TAG = "DashboardActivity";

    private StatusBar header = null;
    private AlertDialog alertDialog;

    private CheckedTextView btnFreeFlight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_screen);
        View headerView = findViewById(R.id.header_preferences);
        header = new StatusBar(this, headerView);

        initUI();
    }

    private void initUI() {
        btnFreeFlight = (CheckedTextView) findViewById(R.id.btnFreeFlight);
    }

    private void initListeners() {
        btnFreeFlight.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        super.onPause();
        header.stopUpdating();

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        header.startUpdating();
        requestUpdateButtonsState();

    }

    public void requestUpdateButtonsState() {
        if (Looper.myLooper() == null)
            throw new IllegalStateException("Should be called from UI thread");

        btnFreeFlight.setChecked(isFreeFlightEnabled());
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnFreeFlight:
                // Open freeflight
                if (!isFreeFlightEnabled() || !onStartFreeflight()) {
                    showErrorMessageForTime(v, getString(R.string.wifi_not_available_please_connect_device_to_drone), 2000);
                }

                break;
        }
    }

    protected boolean isFreeFlightEnabled() {
        return false;
    }

    protected boolean onStartFreeflight() {
        return false;
    }

    private void showErrorMessageForTime(View v, String string, int i) {
        final View oldView = v;
        final ViewGroup parent = (ViewGroup) v.getParent();
        final int index = parent.indexOfChild(v);

        TextView buttonNok = (TextView) v.getTag();

        if (buttonNok == null) {
            buttonNok = (TextView) inflateView(R.layout.dashboard_button_nok, parent, false);
            buttonNok.setLayoutParams(v.getLayoutParams());
            v.setTag(buttonNok);
        }

        buttonNok.setText(string);

        parent.removeView(v);
        parent.addView(buttonNok, index);

        Runnable runnable = new Runnable() {
            public void run() {
                parent.removeViewAt(index);
                parent.addView(oldView, index);
            }
        };

        parent.postDelayed(runnable, i);
    }


    protected void showAlertDialog(String title, String message, final Runnable actionOnDismiss) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialog = alertDialogBuilder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        if (actionOnDismiss != null) {
                            actionOnDismiss.run();
                        }
                    }
                }).create();

        alertDialog.show();
    }

}
