package com.sacedonmg.cancionesingles;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.sacedonmg.cancionesingles.UtilidadesCanciones.validarLeerSD;

/**
 * Created by MGS on 19/07/2016.
 */
public class VistaCancionActivity extends AppCompatActivity implements OnInitListener, OnPreparedListener, android.widget.MediaController.MediaPlayerControl {
    private static final String LOG_TAG = "CI::VistaCancionAct";
    private static final String MODO_NORMAL = "0";
    private static final String MODO_LECTURA_INICIAL = "1";
    private static final String MODO_REPETICION = "2";
    private static final String SUBTITULOS_AMBOS = "0";
    private static final String SUBTITULOS_ORIGINAL = "1";
    private static final String SUBTITULOS_TRADUCIDO = "2";
    private long id;
    private Cancion cancion;
    private MediaPlayer mediaPlayer;
    private MediaController mediaController;
    private TextView titulo;
    private NetworkImageView portada;
    private TextView fOriginal;
    private TextView fTraducida;
    private Button bNormal, bLectura, bRepetir;
    private ImageButton bMicro;
    private TextToSpeech tts; //Motor de voz
    private MiThread miThread;
    private boolean corriendo = false;
    private String modoRepro = MODO_NORMAL;
    private String subtitulos = SUBTITULOS_AMBOS;
    private boolean pulsadoMicro = false;
    private boolean pulsadoNormal = false;
    private TextView fauxiliar;
    private int contador = 0;
    private boolean estaHablando;
    private boolean versionLollipop;


    private int ACTIVIDAD_VISTA_CANCION_LOCAL = 4567;
    private int ACTIVIDAD_VISTA_CANCION_REMOTA = 4568;
    private int source;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vista_cancion);

        Bundle extras = getIntent().getExtras();
        id = extras.getLong("id", -1);
        source = extras.getInt("source", -1);
        tts = new TextToSpeech( this, this );
        ponInfoCancion((int) id);
        mContext = this;
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.menu_vista_cancion_remota, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.accion_editar:
                lanzarEdicionCancion();
                return true;
            case R.id.accion_etiquetar:
                lanzarEtiquetarCancion();
                return true;
            case R.id.accion_borrar:
                borrarCancion((int)id);
                return true;
            case R.id.accion_descargar:
                descargarCancion();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void descargarCancion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Descargar cancion");
        builder.setMessage("¿Quiere descargar la canción? Si lo hace la tendrá disponible cuando no tenga conexión a Internet.");

        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final DownloadFile task = new DownloadFile(mContext);
                task.execute(cancion.getAudio(), cancion.getImagen(), cancion.getXml(), cancion.getTxt_original(), cancion.getTxt_traducido());
                final Thread waitingThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while(task.getStatus() != AsyncTask.Status.FINISHED) {
                                TimeUnit.SECONDS.sleep(1);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    CancionesVector cancionesVector = CancionesVector.getInstance();
                                    cancionesVector.anyade(cancion);
                                    ListaCanciones.adaptador.notifyItemInserted(cancionesVector.tamanyo() - 1);
                                    finish();
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });

                waitingThread.start();
            }
        });
        builder.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    /***
     * Elimina la cancion seleccionada
     * @param id
     */
    public void borrarCancion(final int id){
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_borrar)
                .setMessage(R.string.mensaje_borrar)
                .setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){
                        ListaCanciones.vectorCanciones.borrar((int)id);
                        Intent i = new Intent (VistaCancionActivity.this, ListaCanciones.class);
                        startActivity(i);
                    }

                })
                .setNegativeButton(R.string.cancelar,null)
                .show();
    }

    /**
     * Lanza la vista Edición Canción
     */
    public void lanzarEdicionCancion(){
        Intent i = new Intent (this, EdicionNuevaCancionActivity.class);
        i.putExtra("id",id);
        i.putExtra("editar",true);
        startActivityForResult(i,1234);
    }


    /***
     * lanza la Vista Etiquetar Cancion
     */
    public void lanzarEtiquetarCancion(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_etiquetar)
                .setMessage(R.string.mensaje_etiquetar)
                .setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){
                        Intent i = new Intent (VistaCancionActivity.this, EtiquetarCancionActivity.class);
                        i.putExtra("id",id);
                        startActivityForResult(i,2345);
                    }

                })
                .setNegativeButton(R.string.cancelar,null)
                .show();


    }

    /***
     * Refrescamos la vista canción cuando volvemos de Etiquetar/Editar
     * @param requestCode
     * @param resulCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resulCode, Intent data){
        if(requestCode == 1234 || requestCode ==2345){
            ponInfoCancion((int)id);
            findViewById(R.id.vista_cancion).invalidate();
        }

        if (requestCode == 4578 && resulCode== Activity.RESULT_OK && data!=null) {
            pulsadoMicro = true;
            ArrayList<String> text=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            fTraducida.setText(text.get(0));
        }
    }

    public void bindViews() {
        titulo = (TextView) findViewById(R.id.tituloCancion);
        portada = (NetworkImageView) findViewById(R.id.portada);
        fOriginal = (TextView) findViewById(R.id.f_original);
        fTraducida = (TextView) findViewById(R.id.f_traducida);
        fauxiliar = (TextView) findViewById(R.id.f_aux);
        bMicro = (ImageButton)findViewById(R.id.bMicro);
        bNormal = (Button)findViewById(R.id.bnormal);
        bLectura = (Button)findViewById(R.id.blectura);
        bRepetir = (Button)findViewById(R.id.brepetir);
    }

    private Cancion getCancionById(int id) {
        if (source == ACTIVIDAD_VISTA_CANCION_LOCAL) {
            return CancionesVector.getInstance().elemento(id);
        }

        return ListaCancionesRemoto.adaptador.getItem(id);
    }

    private void getEtiquetado(final Cancion cancion) {
        if (source == ACTIVIDAD_VISTA_CANCION_LOCAL) {
            if(!cancion.getEtiquetado()){
                lanzarAlertaEtiquetado();
            }
        }


        final Thread waitingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                cancion.downloadXML();
                if (!cancion.getEtiquetado()){
                    lanzarAlertaEtiquetado();
                }
            }

        });

        waitingThread.start();
    }

    /**
     * Asigna los datos del elemento cancion seleccionado a la Vista_Cancion
     * @param id posición del objeto canción en el vector canciones.
     */
    public void ponInfoCancion(int id) {
        cancion = getCancionById(id);
        Log.e(LOG_TAG, cancion.toString());

        getEtiquetado(cancion);
        bindViews();
        titulo.setText(cancion.getTitulo());
        portada.setImageUrl(cancion.getImagen(), VolleySingleton.getInstance(getApplicationContext()).getLectorImagenes());

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            titulo.setVisibility(View.INVISIBLE);
            portada.setVisibility(View.INVISIBLE);
        }

        fauxiliar.setVisibility(View.INVISIBLE);
        bMicro.setVisibility(View.INVISIBLE);

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);

        mediaController = new MediaController(this);
        mediaController.setVisibility(View.VISIBLE);
        try {
            Uri audio = Uri.parse(cancion.getAudio());
            if (validarLeerSD()) {
                mediaPlayer.setDataSource(this, audio);
                mediaPlayer.prepare();
            }

        } catch (IOException e) {
            Log.d(LOG_TAG, "No se puede reproducir el audio: " + cancion.getNombreFichero(), e);
        }

        inicializaVistas();
    }

    /***
     * Lanza el AlertDialog si una canción no está etiquetada para su reproducción
     */
    public void lanzarAlertaEtiquetado(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_alerta_etiquetado)
                .setMessage(R.string.mensaje_alerta_etiquetado)
                .setPositiveButton(R.string.etiquetar_cancion, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){
                        Intent i = new Intent (VistaCancionActivity.this, EtiquetarCancionActivity.class);
                        i.putExtra("id",id);
                        startActivityForResult(i,2345);
                    }

                })
                .setNegativeButton(R.string.cancelar,null)
                .show();
    }

    /***
     * inicializa las Vistas a su estado original teniendo en cuenta si la canción está etiquetada o no
     */
    public void inicializaVistas(){
        fTraducida.setText("");
        fOriginal.setText("");
        fauxiliar.setText("");

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            titulo.setVisibility(View.VISIBLE);
            portada.setVisibility(View.VISIBLE);
        }else {
            titulo.setVisibility(View.INVISIBLE);
            portada.setVisibility(View.INVISIBLE);
        }


        bNormal.setVisibility(View.VISIBLE);
        fauxiliar.setVisibility(View.INVISIBLE);
        bMicro.setVisibility(View.INVISIBLE);

        if (!cancion.getEtiquetado()) {
            bRepetir.setVisibility(View.INVISIBLE);
            bLectura.setVisibility(View.INVISIBLE);
            fOriginal.setVisibility(View.INVISIBLE);
            fTraducida.setVisibility(View.INVISIBLE);
        } else {
            bRepetir.setVisibility(View.VISIBLE);
            bLectura.setVisibility(View.VISIBLE);
            fOriginal.setVisibility(View.VISIBLE);
            fTraducida.setVisibility(View.VISIBLE);
        }

        pulsadoNormal = false;
    }

    /***
     * Se activa la repodrucción Modo Normal
     * @param view
     */
    public void sePulsaNormal(View view){

        descativarBotones();
        contador=0;
        pulsadoNormal=true;
        modoRepro = MODO_NORMAL;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        subtitulos = pref.getString("subtitulos","?");
        switch (subtitulos) {
            case SUBTITULOS_AMBOS:
                fOriginal.setVisibility(View.VISIBLE);
                fTraducida.setVisibility(View.VISIBLE);
                break;
            case SUBTITULOS_ORIGINAL:
                fTraducida.setVisibility(View.INVISIBLE);
                break;
            case SUBTITULOS_TRADUCIDO:
                fOriginal.setVisibility(View.INVISIBLE);
                break;
        }
        mediaPlayer.start();
        activaReproduccion();
    }

    /***
     * Se activa la repodrucción Modo Lectura
     * @param view
     */
    public void sePulsaLectura(View view){
        pulsadoNormal=false;
        contador=0;
        descativarBotones();
        modoRepro = MODO_LECTURA_INICIAL;
        activaReproduccion();

    }
    /***
     * Se activa la repodrucción Modo Repetir
     * @param view
     */
    public void sePulsaRepetir(View view){
        pulsadoNormal=false;
        contador=0;
        descativarBotones();
        modoRepro = MODO_REPETICION;
        titulo.setVisibility(View.INVISIBLE);
        portada.setVisibility(View.INVISIBLE);
        fOriginal.setVisibility(View.INVISIBLE);
        fauxiliar.setVisibility(View.VISIBLE);
        activaReproduccion();
    }

    /****
     * Activamos la ejecución del thread que gestiona los modos de reproducción
     */
    public void activaReproduccion(){
        corriendo = true;
        miThread = new MiThread();
        miThread.start();
    }

    /***
     * Desactiva todos los botones
     */
    public void descativarBotones(){
        bNormal.setVisibility(View.INVISIBLE);
        bRepetir.setVisibility(View.INVISIBLE);
        bLectura.setVisibility(View.INVISIBLE);
    }

    /***
     * Activa pulsar el micro para el reconociemiento de voz
      * @param view
     */
    public void sePulsaMicro(View view){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        try {
            startActivityForResult(intent, 4578);
        } catch (ActivityNotFoundException e) {
            Log.e(LOG_TAG,"El dispositivo no permite reconocimiento de voz");
            bMicro.setVisibility(View.INVISIBLE);

        }

    }


    /***
     * Hilo que gestiona los modos de reproducción.
     * modoNormal, modoLecturaInicial, modoRepetición
     */
    class MiThread extends Thread{

        int result;
        Frase frase;
        boolean repetir = true;



        @Override public void run(){
            while(contador <cancion.getLetra().size() && corriendo) {
                switch (modoRepro) {
                    case MODO_NORMAL:
                        modoNormal();
                        break;
                    case MODO_LECTURA_INICIAL:
                        modoLecturaInicial();
                        break;
                    case MODO_REPETICION:
                        modoRepeticion();
                        break;
                }

            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    inicializaVistas();
                    corriendo = false;
                    contador = 0;

                }
            });

        }


        /***
         * Gestiona el modo de reproducción Normal:
         * Reproduce el audio y los subtitulos seleccionados.
         */
        public void modoNormal(){
            //frase = cancion.getLetra().get(contador);
            //publicado = false;

            try {
                //while(mediaPlayer.getCurrentPosition()<frase.getTiempoIni() && corriendo){

                while( mediaPlayer.getCurrentPosition()<cancion.getLetra().get(contador).getTiempoIni() && corriendo){

                    sleep(1);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error ModoNormal sleep1:" + e);
            }

            try {
                //while (mediaPlayer.getCurrentPosition() <= frase.getTiempoFin() && corriendo) {
                while (mediaPlayer.getCurrentPosition() <= cancion.getLetra().get(contador).getTiempoFin() && corriendo) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(contador < cancion.getLetra().size()) {
                                int cont = contador;
                                fOriginal.setText(cancion.getLetra().get(cont).getFraseOriginal());
                                fTraducida.setText(cancion.getLetra().get(cont).getFraseTraducida());
                                //publicado = false;
                            }


                        }
                    });
                }
            }catch (Exception e){
                Log.e(LOG_TAG, "Error ModoNormal bloque2:" + e);
            }

            contador++;
        }

        /***
         * Gestión del modo Lectura Inicial
         * Para cada frase: la lee en inglés, la lee en castellano y la reproduce en el audio.
         */
        public void modoLecturaInicial(){
            frase = cancion.getLetra().get(contador);
            versionLollipop = false;
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String s) {
                }

                @Override
                public void onDone(String utteranceId) {
                    estaHablando = false;
                }

                @Override
                public void onError(String s) {
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                versionLollipop = true;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fOriginal.setText(frase.getFraseOriginal());
                    fTraducida.setText("");
                }
            });

            result = tts.setLanguage(Locale.ENGLISH);
            if (result != TextToSpeech.LANG_MISSING_DATA || result != TextToSpeech.LANG_NOT_SUPPORTED) {
                if(corriendo) {
                    if(versionLollipop){
                        Log.d(LOG_TAG, "VersionLollipop");
                        tts.speak(fOriginal.getText().toString(), TextToSpeech.QUEUE_FLUSH, null,"id");
                    }else {
                        tts.speak(fOriginal.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
            if(versionLollipop){
                estaHablando = true;
                while(estaHablando) {
                    try {
                        sleep(100);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error LecturaInicial sleep1:" + e);
                    }
                }
            }
            else{
                while(tts.isSpeaking()) {
                    try {
                        sleep(1500);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error LecturaInicial sleep1:" + e);
                    }
                }
            }
            try {
                sleep(2000);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error LecturaInicial sleep2:" + e);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fTraducida.setText(frase.getFraseTraducida());
                }

            });


            result = tts.setLanguage(Locale.getDefault());
            if (result != TextToSpeech.LANG_MISSING_DATA || result != TextToSpeech.LANG_NOT_SUPPORTED) {
                if(corriendo) {
                    if(versionLollipop){
                        tts.speak(fTraducida.getText().toString(), TextToSpeech.QUEUE_FLUSH, null,"id");
                    }else {
                        tts.speak(fTraducida.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
            if(versionLollipop){
                estaHablando = true;
                while(estaHablando) {
                    try {
                        sleep(100);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error LecturaInicial sleep3:" + e);
                    }
                }
            }
            else{
                while(tts.isSpeaking()) {
                    try {
                        sleep(1500);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error LecturaInicial sleep3:" + e);
                    }
                }
            }
            try {
                sleep(2000);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error LecturaInicial sleep4:" + e);
            }
            try {
                mediaPlayer.seekTo(frase.getTiempoIni());
                mediaPlayer.start();
                while (mediaPlayer.getCurrentPosition() <= frase.getTiempoFin() && corriendo) {
                    try {
                        if(versionLollipop) {
                            sleep(500);
                        }else{
                            sleep(1500);
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error LecturaInicial sleep5:" + e);
                    }
                }
                mediaPlayer.pause();
            }catch (Exception e){
                Log.d(LOG_TAG, "Error ThreadLecturaInicial: mediaplayer");
            }

            contador++;
            try {
                sleep(1500);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error sleep6:" + e);
            }
        }


        /***
         * Gestión del modo Reproducción
         * Para cada frase: la lee en inglés, espera a que la repitas (reconocimiento voz) y la reproduce en el audio.
         */
        public void modoRepeticion(){
            frase = cancion.getLetra().get(contador);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fauxiliar.setText(frase.getFraseOriginal());
                }
            });
            result = tts.setLanguage(Locale.ENGLISH);
            if (result != TextToSpeech.LANG_MISSING_DATA || result != TextToSpeech.LANG_NOT_SUPPORTED) {
                if(corriendo) {
                    tts.speak(fauxiliar.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                }
            }
            while(tts.isSpeaking()) {
                try {
                    sleep(1500);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error ModoRepetición sleep1:" + e);
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fTraducida.setText("");
                    bMicro.setVisibility(View.VISIBLE);

                }
            });

            while(!pulsadoMicro){
                try {
                    sleep(2000);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error ModoRepetición sleep2:" + e);
                }

            }
            pulsadoMicro=false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    bMicro.setVisibility(View.INVISIBLE);

                }
            });


            try {
                mediaPlayer.seekTo(frase.getTiempoIni());
                mediaPlayer.start();
                while (mediaPlayer.getCurrentPosition() <= frase.getTiempoFin() && corriendo) {
                    try {
                        sleep(1000);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error LecturaInicial sleep5:" + e);
                    }
                }
                mediaPlayer.pause();
            }catch (Exception e){
                Log.d(LOG_TAG, "Error ThreadLecturaInicial: mediaplayer");
            }

            contador++;
            try {
                sleep(2000);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error sleep6:" + e);
            }
        }

    }


    @Override
    public void onPrepared(MediaPlayer mediaplayer) {
        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(findViewById(R.id.vista_cancion));
        mediaController.setEnabled(true);
        if(pulsadoNormal) {
            mediaController.show();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent evento) {
        if (pulsadoNormal) {
            mediaController.show();
        }
        return false;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        int pos = 0;
        try {
            pos = mediaPlayer.getCurrentPosition();

        }catch (Exception e){
            Log.e(LOG_TAG, "Error getCurrentPosition");
        }
        return pos;
    }

    @Override
    public int getDuration() {
        int pos = 0;
        try{
            pos =  mediaPlayer.getDuration();
        }catch (Exception e){
            Log.e(LOG_TAG, "Error getDuration");
        }
        return pos;
    }

    @Override
    public boolean isPlaying() {
        boolean play = false;
        try{
            play = mediaPlayer.isPlaying();
        }catch (Exception e){
            Log.e(LOG_TAG, "Error isPlaying");
        }
        return play;
    }

    @Override
    public void pause() {
        try {
            mediaPlayer.pause();
        }catch (Exception e){
            Log.e(LOG_TAG, "Error pause");
        }
    }

    @Override
    public void seekTo(int pos) {
        int cont = 0;
        boolean noEncontrado = false;

        while (cont < cancion.getLetra().size() && !noEncontrado){
            if(pulsadoNormal) {
                if (pos > cancion.getLetra().get(cont).getTiempoIni() && pos < cancion.getLetra().get(cont).getTiempoFin()) {
                    contador = cont;
                    descativarBotones();
                    fOriginal.setText(cancion.getLetra().get(contador).getFraseOriginal());
                    fTraducida.setText(cancion.getLetra().get(contador).getFraseTraducida());
                    mediaPlayer.seekTo(pos);
                    noEncontrado = true;
                } else {
                    cont++;
                }
            }
        }

    }

    @Override
    public void start() {
        mediaPlayer.start();
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }





    /***
     * Inicializa el TextToSpeech
     * @param status
     */
    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            //coloca lenguaje por defecto  en nuestro caso el lenguaje es aspañol ;)

            int result = tts.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(LOG_TAG, "TTS: Idioma no sportado");
            }
        } else {
            Log.e(LOG_TAG, "TTS: Fallo Inicialización!");
        }

    }


    /***
     * Destruimos el TTS cuando cerramos la aplicación
     * Paramos y liberamos el Mediaplayer
     * Paramos los threads de los modos.
     */
    @Override
    public void onDestroy() {

        corriendo = false;
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onResume(){

        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        corriendo = true;


    }

    @Override
    public void onStop() {

        corriendo = false;
        try {
            if (mediaPlayer !=null) {

                    mediaPlayer.stop();
                    mediaPlayer.release();

            }
        }catch (Exception e){
            Log.e(LOG_TAG, "onStop:MediaPlayer");
        }
        if (tts != null) {
            tts.stop();
        }
        //inicializaVistas();

        super.onStop();
    }
    /***
     * Cuando salimos de la vista con el botón de retorno paramos la música y liberamos el MediaPlayer
     */
    @Override
    public void onBackPressed(){
        corriendo = false;
        finish();
    }


    /*//Guardar el estado de la Actividad VistaCancion
    @Override
    public void onSaveInstanceState(Bundle estadoGuardado){
        super.onSaveInstanceState(estadoGuardado);
        if(mediaPlayer != null){
            int pos = mediaPlayer.getCurrentPosition();
            estadoGuardado.putInt("posicion", pos);

        }
        int cont = contador;
        estadoGuardado.putInt("contador", cont);
        estadoGuardado.putBoolean("pulsadoNormal",pulsadoNormal);
        estadoGuardado.putString("modoRepro",modoRepro);
        estadoGuardado.putBoolean("iniciadoVistas",iniciadoVistas);
        estadoGuardado.putBoolean("reinicio",true);

    }


    @Override
    public void onRestoreInstanceState(Bundle estadoGuardado){
        super.onRestoreInstanceState(estadoGuardado);
        if(estadoGuardado != null){
            contador = estadoGuardado.getInt("contador");
            pulsadoNormal = estadoGuardado.getBoolean("pulsadoNormal");
            modoRepro = estadoGuardado.getString("modoRepro");
            iniciadoVistas = estadoGuardado.getBoolean("iniciadoVistas");
            reinicio = estadoGuardado.getBoolean("reinicio");
            if(mediaPlayer!= null) {
                int pos = estadoGuardado.getInt("posicion");
                mediaPlayer.seekTo(pos);
            }
        }

    }*/

}


