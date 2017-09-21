package com.sacedonmg.cancionesingles;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Ana María Arrufat on 20/09/2017.
 */

public class FirebaseSingleton {
    private static FirebaseSingleton instance;
    private FirebaseAuth auth;

    private final static String USERS_CHILD = "usuarios";
    private final static String SONGS_CHILD = "canciones";
    private DatabaseReference cancionesRef;
    private DatabaseReference usersReference;
    private FirebaseDatabase database;

    private FirebaseSingleton() {
        database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        usersReference = database.getReference().child(USERS_CHILD);
        cancionesRef = database.getReference().child(SONGS_CHILD);
        auth = FirebaseAuth.getInstance();
    }

    public static FirebaseSingleton getInstance() {
        if (instance == null) {
            instance = new FirebaseSingleton();
        }

        return instance;
    }

    public DatabaseReference getUsersReference() {
        return usersReference;
    }

    public DatabaseReference getCancionesReference() {
        return cancionesRef;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseDatabase getDataBase() {
        return database;
    }
}
