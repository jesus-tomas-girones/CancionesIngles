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

import static com.sacedonmg.cancionesingles.MainActivity.ACTIVIDAD_CREAR;
import static com.sacedonmg.cancionesingles.MainActivity.ACTIVIDAD_VISTA_CANCION_LOCAL;
import static com.sacedonmg.cancionesingles.Utilidades.isPermissionGranted;
import static com.sacedonmg.cancionesingles.Utilidades.requestPermission;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.crearArchivosEjemplo;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.rutaCarpeta;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.sincroListReproduccion;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.validarEscribirSD;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.validarLeerSD;

public class ListaCanciones extends Fragment {
    private static final String LOG_TAG = "CI::ListaCanciones";

    private RecyclerView recyclerView;
    public static AdaptadorCancionesLocal adaptador;
    private RecyclerView.LayoutManager layoutManager;
    private View rootView;

    public ListaCanciones() {}

    public static ListaCanciones newInstance() {
        ListaCanciones fragment = new ListaCanciones();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(LOG_TAG, "onCreateView");
        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.activity_main, container, false);
        inicializaDatos();
        inicializaVista();
        return rootView;
    }

    /**
     * Inicializar datos de la Aplicación:
     * Si es la primera vez (instala la aplicación), guarda en la SD los ficheros para una canción DEMO.
     * Carga la lista de reproducción en función de los xml que encuentra en la carpeta SD "cancionesingles"
     */
    public void inicializaDatos() {
        Log.d(LOG_TAG, "inicializaDatos");
        boolean sincronizar = false;
        if (validarLeerSD()) {
            File carpeta = new File(rutaCarpeta);
            if (!carpeta.exists()) {  //Es la primera vez que se instala la aplicación.
                sincronizar = crearCarpeta(carpeta);
            } else { //ya existia la carpeta
                sincronizar = true;
            }
        }
        if (sincronizar) {
            sincroListReproduccion();
        }
    }

    /**
     * Crea la carpeta cancionesingles en la SD cuando se instala por primera vez la aplicación
     * y guarda los ficheros Demo de Assets en la SD
     *
     * @param carpeta carpeta cancionesingles a crear en la SD
     * @return
     */
    public boolean crearCarpeta(File carpeta) {
        Log.d(LOG_TAG, "crear carpeta cancionesingles");
        if (!validarEscribirSD()) {
            return false;
        }

        if (isPermissionGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, getActivity())) {
            carpeta.mkdirs();
            return crearArchivosEjemplo(getContext());
        }  else {
            requestPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, getActivity());
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resulCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult");
        if (requestCode == ACTIVIDAD_VISTA_CANCION_LOCAL || requestCode == ACTIVIDAD_CREAR) {
            inicializaVista();
        }
    }

    /***
     * Inicializa la vista recyclerView.
     */
    public void inicializaVista() {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adaptador = new AdaptadorCancionesLocal(getContext());
        recyclerView.setAdapter(adaptador);

        adaptador.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), VistaCancionActivity.class);
                i.putExtra("id", (long) recyclerView.getChildAdapterPosition(v));
                i.putExtra("source", ACTIVIDAD_VISTA_CANCION_LOCAL);
                startActivityForResult(i, ACTIVIDAD_VISTA_CANCION_LOCAL);
            }
        });
    }
}
