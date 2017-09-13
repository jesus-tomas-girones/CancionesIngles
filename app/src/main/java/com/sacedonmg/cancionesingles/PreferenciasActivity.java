package com.sacedonmg.cancionesingles;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Userç on 07/09/2016.
 */
public class PreferenciasActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PreferenciasFragment()).commit();
    }
}
