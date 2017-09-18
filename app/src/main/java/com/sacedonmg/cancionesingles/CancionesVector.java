package com.sacedonmg.cancionesingles;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static com.sacedonmg.cancionesingles.UtilidadesCanciones.EXTENSION_XML;

/**
 * Created by MGS on 09/07/2016.
 */
public class CancionesVector implements Canciones {

    private static final String LOG_TAG = "CI::CancionesVector";
    private static CancionesVector instance;

    protected List<Cancion> vectorCanciones;

    private CancionesVector() {
        Log.d(LOG_TAG, "CancionesVector");
        vectorCanciones = new ArrayList<Cancion>();
    }

    public static CancionesVector getInstance() {
        if (instance == null) {
            instance = new CancionesVector();
        }

        return instance;
    }

    public Cancion elemento(int id) {
        return vectorCanciones.get(id);
    }

    public boolean exists(Cancion cancion) {
        for (Cancion c : vectorCanciones) {
            if (c.equals(cancion)) {
                return true;
            }
        }

        return false;
    }

    public void anyade(Cancion cancion) {
        if (!exists(cancion)) {
            Log.d(LOG_TAG, "Nueva canción: " + cancion.getTitulo());
            vectorCanciones.add(cancion);
        }
    }

    public int nuevo() {
        Cancion cancion = new Cancion();
        vectorCanciones.add(cancion);
        return vectorCanciones.size()-1;
    }

    public void borrar(int id) {
        Cancion cancion = elemento(id);
        if(cancion.borrarXML()){
            Log.d(LOG_TAG, "Fichero "+ cancion.getNombreFichero()+EXTENSION_XML + " borrado");
            vectorCanciones.remove(id);
        }

        else{
            Log.e(LOG_TAG, "Error borrando fichero: " + cancion.getNombreFichero()+EXTENSION_XML );
        }
    }

    public int tamanyo() {
        return vectorCanciones.size();
    }

    public void actualiza(int id, Cancion cancion) {
        vectorCanciones.set(id, cancion);
    }

}
