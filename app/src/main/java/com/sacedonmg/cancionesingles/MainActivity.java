package com.sacedonmg.cancionesingles;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import static com.sacedonmg.cancionesingles.UtilidadesCanciones.copyFileFromAssets;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.generarFicheros;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.rutaCarpeta;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.sincroListReproduccion;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.validarEscribirSD;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.validarLeerSD;

public class MainActivity extends Fragment {

    public static Canciones vectorCanciones = new CancionesVector();
    private static final String LOG_TAG = "MainActivity";

    private RecyclerView recyclerView;
    public AdaptadorCancionesLocal adaptador;
    private RecyclerView.LayoutManager layoutManager;
    private View rootView;

    private int ACTIVIDAD_VISTA_CANCION = 4567;
    private int ACTIVIDAD_EDICION = 5678;
    
    public MainActivity() {}

    public static MainActivity newInstance() {
        MainActivity fragment = new MainActivity();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.activity_main, container, false);
        inicializaDatos();
        inicializaVista();
        return rootView;
    }


     /** Inicializar datos de la Aplicación:
     *   Si es la primera vez (instala la aplicación), guarda en la SD los ficheros para una canción DEMO.
     *   Carga la lista de reproducción en función de los xml que encuentra en la carpeta SD "cancionesingles"
     */
    public void inicializaDatos (){
        boolean sincronizar = false;
        if(validarLeerSD()){

            File carpeta = new File(rutaCarpeta);

            if(!carpeta.exists()) {  //Es la primera vez que se instala la aplicación.
                sincronizar = crearCarpeta(carpeta);
            }
            else{ //ya existia la carpeta
                sincronizar = true;
            }
        }
        if(sincronizar){
            sincroListReproduccion();

        }
    }

    /**
     * Crea la carpeta cancionesingles en la SD cuando se instala por primera vez la aplicación
     * y guarda los ficheros Demo de Assets en la SD
     * @param carpeta carpeta cancionesingles a crear en la SD
     * @return
     */
    public boolean crearCarpeta(File carpeta){
        boolean resultado = false;


        if (validarEscribirSD()){
            carpeta.mkdirs();
            Log.v(LOG_TAG, "Carpeta cancionesingles creada en la SD");

            if(carpeta.exists()) {
                String[] ficherosDemo = generarFicheros();

                for (String rutaFicheroDemo : ficherosDemo) {    ///Copiamos todos los ficherosDemo de Assets a la SD
                    try {
                        String rutaFicheroSD = rutaCarpeta + rutaFicheroDemo;
                        copyFileFromAssets(getContext(), rutaFicheroDemo, rutaFicheroSD);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                resultado = true;
            }
            else{
                Log.v(LOG_TAG,"ERROR: Carpeta cancionesingles no creada");
                resultado = false;
            }
        }
        return resultado;
    }

    @Override
    public void onActivityResult(int requestCode, int resulCode, Intent data){
        if(requestCode == ACTIVIDAD_VISTA_CANCION || requestCode == ACTIVIDAD_EDICION) {
            inicializaVista();
        }
    }

    /*@Override
    public void onBackPressed(){
        finish();
    }*/


    /***
     * Inicializa la vista recyclerView.
     */
    public void inicializaVista() {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        adaptador = new AdaptadorCancionesLocal(getContext(), vectorCanciones);
        recyclerView.setAdapter(adaptador);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adaptador.setOnItemClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i = new Intent(getContext(), VistaCancionActivity.class);
                i.putExtra("id",(long)recyclerView.getChildAdapterPosition(v));
                startActivityForResult(i, ACTIVIDAD_VISTA_CANCION);
            }
        });
    }



}
