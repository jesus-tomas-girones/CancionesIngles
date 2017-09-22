package com.sacedonmg.cancionesingles;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by MGS on 21/07/2016.
 */
public class EdicionNuevaCancionActivity extends AppCompatActivity {

    private static final String LOG_TAG = "CI::EdiNuevaCancion";
    private long id;
    private Cancion cancion;
    private EditText titulo;
    private EditText autor;
    private Spinner genero;
    private Spinner dificultad;
    private EditText nombreFichero;
    private TextView textoNombreFichero;
    private TextView nombreVista;
    private boolean editarCancion = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edicion_cancion);

        Bundle extras = getIntent().getExtras();

        editarCancion = extras.getBoolean("editar");

        inicializaDatosCancion();

        if(editarCancion) { //Editamos una canción existente
            id = extras.getLong("id", -1);
            mostrarDatosCancion((int) id);
        }

    }


    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.menu_editar_cancion,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.accion_guardar:
                guardarCambios();
                finish();
                return true;

            case R.id.accion_cancelar:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void inicializaDatosCancion(){
        cancion = new Cancion();

        FirebaseUser currentUser = FirebaseSingleton.getInstance().getCurrentUser();
        String userUid = currentUser != null ? currentUser.getUid() : "local";
        cancion.setUser(userUid);

        nombreVista = (TextView) findViewById(R.id.nombreVista);

        titulo = (EditText)findViewById(R.id.titulo);
        autor = (EditText)findViewById(R.id.autor);

        genero = (Spinner)findViewById(R.id.genero);
        ArrayAdapter<String> adaptadorG = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,Genero.getGeneros());
        adaptadorG.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genero.setAdapter(adaptadorG);

        dificultad = (Spinner)findViewById(R.id.dificultad);
        ArrayAdapter<String> adaptadorD = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,Dificultad.getDificultades());
        adaptadorD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dificultad.setAdapter(adaptadorD);

        textoNombreFichero = (TextView) findViewById(R.id.t_nombreFichero);
        nombreFichero = (EditText)findViewById(R.id.nombreFichero);

        if(editarCancion){
            nombreFichero.setVisibility(View.INVISIBLE);
            textoNombreFichero.setVisibility(View.INVISIBLE);
            nombreVista.setText(R.string.editar_cancion);
        }
        else{
            nombreFichero.setVisibility(View.VISIBLE);
            textoNombreFichero.setVisibility(View.VISIBLE);
            nombreVista.setText(R.string.nueva_cancion);
        }
    }

    /**
     * Muestra los datos de la canción en la vista de edición
     * @param id identificador del objeto canción dentro del vector canciones
     */
    public void mostrarDatosCancion(int id){
        cancion = ListaCanciones.vectorCanciones.elemento(id);
        titulo.setText(cancion.getTitulo());
        autor.setText(cancion.getAutor());
        genero.setSelection(cancion.getGenero().ordinal());
        dificultad.setSelection(cancion.getDificultad().ordinal());
    }

    /**
     * Guarda los cambios realizados en la vista de edición/nuevo
     */
    public void guardarCambios(){
        boolean borrado = false;
        boolean noInsertar = false;

        cancion.setTitulo(titulo.getText().toString());
        cancion.setAutor(autor.getText().toString());
        cancion.setGenero(genero.getSelectedItemPosition());
        cancion.setDificultad(dificultad.getSelectedItemPosition());

        if(!editarCancion){
            cancion.setEtiquetado(false);

            if(nombreFichero.getText().toString().isEmpty()) {
                Log.e(LOG_TAG, "Error Nombre fichero: "+nombreFichero.getText().toString());
                noInsertar = true;
            }else {
                cancion.setNombreFichero(nombreFichero.getText().toString());
                cancion.leerTXT();

            }
        }
        if(!noInsertar) {

            borrado = cancion.borrarXML();

            if (!borrado && editarCancion) {
                Log.e(LOG_TAG, "Error borrando fichero: Imposible guarda cambios en el xml");
            } else {
                cancion.escribirXML();
            }
        }

    }

}
