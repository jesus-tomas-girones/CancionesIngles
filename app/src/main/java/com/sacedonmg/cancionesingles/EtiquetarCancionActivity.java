package com.sacedonmg.cancionesingles;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;

import static com.sacedonmg.cancionesingles.UtilidadesCanciones.rutaAudio;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.validarLeerSD;

/**
 * Created by MGS on 06/09/2016.
 */
public class EtiquetarCancionActivity extends AppCompatActivity {
    private static final String LOG_TAG = "EtiquetaCancionActivity";
    private long id;
    private Cancion cancion;
    private TextView tFraseOriginal;
    private TextView tFraseTraducida;
    private Button bIni, bFin;
    private ImageButton bStart;
    //private ImageView portada;
    private MediaPlayer mediaPlayer;
    private int contador= 0;
    private int timeActualMP =0;
    private int timeAnteriorFin = 0;
    private boolean pulsadoIni = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.etiquetar_cancion);

        Bundle extras = getIntent().getExtras();
        id = extras.getLong("id", -1);

        inicializaDatosEtiquetar();

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
                guardarEtiquetar();
                finish();
                return true;

            case R.id.accion_cancelar:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /***
     * Guardamos las modificaciones realizadas por el proceso de etiquetar
     * Activamos etiquetado a true, borramos el xml y lo volvemos a escribir con los datos de la canción.
     */
    public void guardarEtiquetar(){
        cancion.setEtiquetado(true);

        if(cancion.borrarXML()){
            cancion.escribirXML();
        }
        else{
            Log.e(LOG_TAG, "Error borrando fichero: Imposible guardar etiquetado en el xml");
        }
    }



    /***
     * Inicializa todos los componentes que necesita la vista Etiquetar así como el MediaPlayer
     */
    public void inicializaDatosEtiquetar(){
        cancion = ListaCanciones.vectorCanciones.elemento((int)id);

        tFraseOriginal = (TextView)findViewById(R.id.fraseOriginal);
        tFraseTraducida = (TextView)findViewById(R.id.fraseTraducida);

        bIni = (Button) findViewById(R.id.buttonIni);
        bStart = (ImageButton)findViewById(R.id.buttonStart);
        bFin = (Button) findViewById(R.id.buttonFin);

        bIni.setVisibility(View.INVISIBLE);
        bFin.setVisibility(View.INVISIBLE);


        /*portada = (ImageView) findViewById(R.id.portadaEtiquetar);
        Bitmap image = obtenerPortadaSD(this, cancion.getNombreFichero());
        if (image != null) {
            portada.setImageBitmap(image);
        }*/

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        try {
            if (validarLeerSD()) {
                mediaPlayer.setDataSource(rutaAudio(cancion.getNombreFichero()));
                mediaPlayer.prepare();

            }
        } catch (IOException e) {
            Log.d(LOG_TAG, "No se puede reproducir el audio: " + cancion.getNombreFichero(), e);
        }

        /*
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                bIni.setVisibility(View.INVISIBLE);
                bFin.setVisibility(View.INVISIBLE);
                bStart.setVisibility(View.VISIBLE);
                bStart.setText(R.string.tiempo_repetir);

            }
        });*/
    }


    @Override
    public void onStop() {
        super.onStop();
        try {

            mediaPlayer.stop();
            mediaPlayer.release();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error en mediaPlayer.stop()");
        }
    }


    /****
     * Tras pulsar el botón start, pasa a modo invisible, visualizando los botones ini y fin.
     * Visualizamos la primera frase y comienza el audio.
     * @param view
     */
    public void start(View view){
        bStart.setVisibility(View.INVISIBLE);
        bIni.setVisibility(View.VISIBLE);
        bFin.setVisibility(View.VISIBLE);
        visualizarFrase();
        mediaPlayer.start();
    }

    /***
     * Al pulsar el botón Ini se guarda el tiempo inicial de la frase
     * @param view
     */

    public void ini(View view){
        if(mediaPlayer.isPlaying() && contador < cancion.getLetra().size()) {
            timeActualMP = mediaPlayer.getCurrentPosition()-100;
            cancion.getLetra().get(contador).setTiempoIni(timeActualMP);
            if(!pulsadoIni){
                pulsadoIni = true;
            }
        }
    }


    /***
     * Pulsando el botón fin obtenemos el tiempo final de la frase y visualiza la siguiente frase
     * Comprueba si se ha pulsado Ini, en caso contrario actualiza con el valor del tiempo fin anterior +25ms de ajuste
     * @param view
     */
    public void fin(View view){
        if(mediaPlayer.isPlaying() && contador < cancion.getLetra().size()) {
            timeActualMP = mediaPlayer.getCurrentPosition()+100;
            cancion.getLetra().get(contador).setTiempoFin(timeActualMP);

            if(!pulsadoIni && cancion.getLetra().get(contador).getTiempoIni() == 0) {
                cancion.getLetra().get(contador).setTiempoIni(timeAnteriorFin+25);
            }
            timeAnteriorFin = timeActualMP;
            pulsadoIni=false;
            contador++;
            visualizarFrase();
        }
        if(!mediaPlayer.isPlaying() || contador >= cancion.getLetra().size()){
            contador = 0;
            bIni.setVisibility(View.INVISIBLE);
            bFin.setVisibility(View.INVISIBLE);
            bStart.setVisibility(View.VISIBLE);

        }

    }



    /***
     * Visualiza cada frase de la canción
     */
    public void visualizarFrase(){
        if(contador < cancion.getLetra().size()){
            tFraseOriginal.setText(cancion.getLetra().get(contador).getFraseOriginal());
            tFraseTraducida.setText(cancion.getLetra().get(contador).getFraseTraducida());
        }
    }




}
