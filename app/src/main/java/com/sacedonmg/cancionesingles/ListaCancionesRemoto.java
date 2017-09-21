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

import static com.sacedonmg.cancionesingles.MainActivity.ACTIVIDAD_VISTA_CANCION_REMOTA;
import static com.sacedonmg.cancionesingles.MainActivity.SECCION_DESCARGADAS;

public class ListaCancionesRemoto extends Fragment {

    private static final String LOG_TAG = "CI::ListaCanciones";

    private RecyclerView recyclerView;
    public static AdaptadorCancionesRemoto adaptador;
    private RecyclerView.LayoutManager layoutManager;
    private View rootView;

    public ListaCancionesRemoto() {
    }

    public static ListaCancionesRemoto newInstance() {
        ListaCancionesRemoto fragment = new ListaCancionesRemoto();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(LOG_TAG, "onCreateView");
        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.activity_main, container, false);
        inicializaVista();
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resulCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult -> " + requestCode);
        if (requestCode == ACTIVIDAD_VISTA_CANCION_REMOTA) {
            inicializaVista();
            // sincroListReproduccion();
            TabbedActivity.getViewPager().setCurrentItem(SECCION_DESCARGADAS);
        }
    }

    /***
     * Inicializa la vista recyclerView.
     */
    public void inicializaVista() {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        FirebaseSingleton firebaseSingleton = FirebaseSingleton.getInstance();
        adaptador = new AdaptadorCancionesRemoto(getContext(), firebaseSingleton.getCancionesReference());

        recyclerView.setAdapter(adaptador);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adaptador.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), VistaCancionActivity.class);
                i.putExtra("id", (long) recyclerView.getChildAdapterPosition(v));
                i.putExtra("source", ACTIVIDAD_VISTA_CANCION_REMOTA);
                startActivityForResult(i, ACTIVIDAD_VISTA_CANCION_REMOTA);
            }
        });
    }
}
