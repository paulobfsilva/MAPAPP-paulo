package com.azgo.mapapp.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.azgo.mapapp.R;

/**
 * Created by Utilizador on 13-12-2016.
 */

public class HistoryActivity extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.e("DEBUG", "IM HERE");
        return inflater.inflate(R.layout.history,container, false);
    }
}
