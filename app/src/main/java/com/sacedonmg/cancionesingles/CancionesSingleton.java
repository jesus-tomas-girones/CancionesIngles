package com.sacedonmg.cancionesingles;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CancionesSingleton  {
    private AdaptadorCancionesRemoto adaptador;
    private static CancionesSingleton ourInstance;
    private DatabaseReference cancionesRef;
    private final static String SONGS_CHILD = "canciones";


    private CancionesSingleton(Context mContext) {
        Log.e("CancionesSingleton", "constructor");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        cancionesRef = database.getReference().child(SONGS_CHILD);
    }

    public static CancionesSingleton getInstance(Context mContext) {
        if (ourInstance == null) {
            ourInstance = new CancionesSingleton(mContext);
        }

        return ourInstance;
    }

    public DatabaseReference getCancionesReference() {
        return cancionesRef;
    }
}
