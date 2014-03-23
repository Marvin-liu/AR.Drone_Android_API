package com.parrot.freeflight.activities.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Yang Zhang on 2014/3/22.
 */
@SuppressLint("Registered")
public class BaseActivity extends FragmentActivity {
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        View rootView = findViewById(android.R.id.content);
    }


    public View inflateView(int resource, ViewGroup root, boolean attachToRoot) {
        View result = inflater.inflate(resource, root, attachToRoot);

        /*if (result != null) {

        }*/

        return result;
    }
}
