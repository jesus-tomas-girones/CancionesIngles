package com.sacedonmg.cancionesingles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.Vector;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.*;

public class MainActivity extends AppCompatActivity {

    public static Canciones vectorCanciones = new CancionesVector();
    private static final String LOG_TAG = "MainActivity";

    private RecyclerView recyclerView;
    public AdaptadorCanciones adaptador;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(android.R.drawable.ic_popup_sync);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sincroListReproduccion();
                inicializaVista();

            }
        });

        inicializaDatos();
        inicializaVista();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            lanzarPreferencias();
            return true;
        }
        if(id == R.id.nuevo){
            lanzarNuevo();
            return true;
        }
        if (id == R.id.acercaDe){
            lanzarAcercaDe();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /***
     * Mostrar preferencias
     */
    public void lanzarPreferencias(){
        Intent i = new Intent(this, PreferenciasActivity.class);
        startActivity(i);
    }

    /**
     * Lanzar actividad Acerca De ...
     */
    public void lanzarAcercaDe(){
        Intent i = new Intent(this, AcercaDeActivity.class);
        startActivity(i);
    }

    /***
     * Lanza la actividad que permite insertar nuevas canciones
     */
    public void lanzarNuevo(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_nuevo)
                .setMessage(R.string.mensaje_nuevo)
                .setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){

                        Intent i = new Intent (MainActivity.this, EdicionNuevaCancionActivity.class);
                        i.putExtra("editar",false);
                        startActivityForResult(i,5678);
                    }

                })
                .setNegativeButton(R.string.cancelar,null)
                .show();
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
                        copyFileFromAssets(this, rutaFicheroDemo, rutaFicheroSD);
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
    protected void onActivityResult(int requestCode, int resulCode, Intent data){
        if(requestCode == 4567 || requestCode == 5678) {
            inicializaVista();
        }
    }

    @Override
    public void onBackPressed(){
        finish();
    }


    /***
     * Inicializa la vista recyclerView.
     */
    public void inicializaVista(){
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adaptador = new AdaptadorCanciones(this, vectorCanciones);
        recyclerView.setAdapter(adaptador);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adaptador.setOnItemClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i = new Intent(MainActivity.this, VistaCancionActivity.class);
                i.putExtra("id",(long)recyclerView.getChildAdapterPosition(v));
                startActivityForResult(i,4567);

            }

        });
    }



}
