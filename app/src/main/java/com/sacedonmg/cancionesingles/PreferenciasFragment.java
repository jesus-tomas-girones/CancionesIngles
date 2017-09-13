package com.sacedonmg.cancionesingles;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by MGS on 07/09/2016.
 */
public class PreferenciasFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencias);
    }
}
