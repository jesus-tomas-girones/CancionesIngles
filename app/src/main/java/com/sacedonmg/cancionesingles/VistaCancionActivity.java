package com.sacedonmg.cancionesingles;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
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
import android.support.v7.widget.MenuItemHoverListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.sacedonmg.cancionesingles.MainActivity.ACTIVIDAD_EDICION;
import static com.sacedonmg.cancionesingles.MainActivity.ACTIVIDAD_ETIQUETAR;
import static com.sacedonmg.cancionesingles.MainActivity.ACTIVIDAD_LOGIN;
import static com.sacedonmg.cancionesingles.MainActivity.ACTIVIDAD_VISTA_CANCION_LOCAL;
import static com.sacedonmg.cancionesingles.MainActivity.ACTIVIDAD_VISTA_CANCION_REMOTA;
import static com.sacedonmg.cancionesingles.MainActivity.CANCION_DESCARGADA;
import static com.sacedonmg.cancionesingles.MainActivity.READ_EXTERNAL_STORAGE_PERMISSION;
import static com.sacedonmg.cancionesingles.MainActivity.WRITE_EXTERNAL_STORAGE_PERMISSION;
import static com.sacedonmg.cancionesingles.Utilidades.isPermissionGranted;
import static com.sacedonmg.cancionesingles.Utilidades.requestPermission;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.borrandoDatos;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.mostrarMensaje;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.obtenerPortadaSD;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.validarLeerSD;

/**
 * Created by MGS on 19/07/2016.
 */
public class VistaCancionActivity extends AppCompatActivity implements OnInitListener, OnPreparedListener, android.widget.MediaController.MediaPlayerControl {
    private static final String LOG_TAG = "CI::VistaCancionAct";
    private static final String MODO_NORMAL = "0";
    private static final String MODO_LECTURA_INICIAL = "1";
    private static final String MODO_REPETICION = "2";
    private static final String MODO_RELLENAR = "3";
    private static final String SUBTITULOS_AMBOS = "0";
    private static final String SUBTITULOS_ORIGINAL = "1";
    private static final String SUBTITULOS_TRADUCIDO = "2";
    private long id;
    private Cancion cancion;
    private MediaPlayer mediaPlayer;
    private MediaController mediaController;
    private TextView titulo, tv_palabraOculta, fOriginal, fTraducida, fauxiliar;
    private EditText et_palabraOculta;
    private NetworkImageView portada;
    private Button bNormal, bLectura, bRepetir, bRellenar, bComprobar, bSeguir, bDescubrir, bRepetirFrase;
    private ImageButton bMicro;
    private TextToSpeech tts; //Motor de voz
    private MiThread miThread;
    private String modoRepro = MODO_NORMAL;
    private String subtitulos = SUBTITULOS_AMBOS;
    private boolean corriendo = false;
    private boolean pulsadoMicro = false;
    private boolean pulsadoNormal = false;
    private int contador = 0;
    private boolean estaHablando, versionLollipop;
    private int posicion;

    private int source;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vista_cancion);

        mContext = this;
        Bundle extras = getIntent().getExtras();
        id = extras.getLong("id", -1);
        source = extras.getInt("source", -1);
        tts = new TextToSpeech( this, this );
        ponInfoCancion((int) id, true);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        if (source == ACTIVIDAD_VISTA_CANCION_LOCAL) {
            getMenuInflater().inflate(R.menu.menu_vista_cancion, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_vista_cancion_remota, menu);
            FirebaseUser currentUser = FirebaseSingleton.getInstance().getCurrentUser();
            if (currentUser == null || cancion.getUser().compareTo(currentUser.getUid()) != 0) {
                menu.findItem(R.id.accion_borrar).setVisible(false);
            }
        }

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
                return true;
            case R.id.accion_subir:
                subirCancionAFireBase();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void descargarCancion() {
        if (!isPermissionGranted(WRITE_EXTERNAL_STORAGE, this)) {
            requestPermission(WRITE_EXTERNAL_STORAGE, this);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Descargar cancion");
        builder.setMessage("¿Quiere descargar la canción? Si lo hace la tendrá disponible cuando no tenga conexión a Internet.");

        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final DownloadFile task = new DownloadFile(mContext);
                task.execute(cancion);
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
                                    ListaCanciones.adaptador.notifyItemInserted(cancionesVector.tamanyo() - 1);
                                    setResult(CANCION_DESCARGADA);
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

    public ProgressDialog setUpProgressDialog(Cancion cancion, Context mContext) {
        ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Subiendo " + cancion.getTitulo());
        return progressDialog;
    }

    public void subirCancionAFireBase() {

        FirebaseSingleton firebaseSingleton = FirebaseSingleton.getInstance();
        FirebaseUser currentUser = firebaseSingleton.getCurrentUser();
        if (currentUser == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Es necesario estar loggeado para compartir una canción")
                    .setMessage("¿Desea identificarse ahora?")
                    .setPositiveButton("Inciciar sesión", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int whichButton){
                            Intent i = new Intent (VistaCancionActivity.this, MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            i.putExtra("SCREEN", ACTIVIDAD_LOGIN);
                            startActivity(i);
                        }

                    })
                    .setNegativeButton(R.string.cancelar, null)
                    .show();
        } else {
            ProgressDialog progressDialog = setUpProgressDialog(cancion, mContext);
            UtilidadesCanciones.subirCancionAFireBase(cancion, progressDialog, mContext);
        }
    }

    private void borrarCancionLocal(int id) {
        CancionesVector cancionesVector = CancionesVector.getInstance();
        cancionesVector.borrar(id);
        ListaCanciones.adaptador.notifyItemRemoved(id);
        finish();
    }

    private void borrarCancionRemota(final int id) {
        final Cancion cancion = getCancionById(id);

        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Borrando " + cancion.getTitulo());
        progressDialog.show();

        UtilidadesCanciones.borrarCancionRemota(cancion);

        final Thread waitingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int count = 5;
                    while(count > 0) {
                        count = 0;
                        for (int i = 0; i < borrandoDatos.length; i++)
                            count = borrandoDatos[i] ? count + 1 : count;

                        final int countToShow = count;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run () {
                                progressDialog.setMessage("Quedan " + countToShow + "/5 archivos");
                            }
                        });

                        TimeUnit.SECONDS.sleep(1);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            progressDialog.dismiss();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String nombre = cancion.getTitulo().replace(" ", "").toLowerCase();
                DatabaseReference cancionesRef = FirebaseSingleton.getInstance().getCancionesReference();
                cancionesRef.child(nombre).removeValue();
                finish();
            }
        });

        waitingThread.start();
    }

    /***
     * Elimina la cancion seleccionada
     * @param id
     */
    public void borrarCancion(final int id){
        int mensaje = R.string.mensaje_borrar_local;
        if (source == ACTIVIDAD_VISTA_CANCION_REMOTA) {
            mensaje = R.string.mensaje_borrar_remota;
            FirebaseUser currentUser = FirebaseSingleton.getInstance().getCurrentUser();
            if (currentUser == null || cancion.getUser() == null || cancion.getUser().compareTo(currentUser.getUid()) != 0) {
                mostrarMensaje(this, mContext.getResources().getString(R.string.no_permiso_borrar));
                return;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_borrar)
                .setMessage(R.string.mensaje_borrar)
                .setMessage(mensaje)
                .setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){
                        if (source == ACTIVIDAD_VISTA_CANCION_REMOTA) {
                            borrarCancionRemota(id);
                        } else {
                            borrarCancionLocal(id);
                        }
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
        startActivityForResult(i, ACTIVIDAD_EDICION);
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
                        startActivityForResult(i, ACTIVIDAD_ETIQUETAR);
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
        if(requestCode == ACTIVIDAD_EDICION || requestCode == ACTIVIDAD_ETIQUETAR){
            ponInfoCancion((int)id, true);
            findViewById(R.id.vista_cancion).invalidate();
        }

        if (requestCode == 4578 && resulCode== Activity.RESULT_OK && data!=null) {
            pulsadoMicro = true;
            ArrayList<String> text=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            fTraducida.setText(text.get(0));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == READ_EXTERNAL_STORAGE_PERMISSION || requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && validarLeerSD()) {
                descargarCancion();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void bindViews() {
        titulo = (TextView) findViewById(R.id.tituloCancion);
        portada = (NetworkImageView) findViewById(R.id.portada);
        fOriginal = (TextView) findViewById(R.id.f_original);
        fTraducida = (TextView) findViewById(R.id.f_traducida);
        fauxiliar = (TextView) findViewById(R.id.f_aux);
        tv_palabraOculta = (TextView) findViewById(R.id.tv_palabraOculta);
        et_palabraOculta = (EditText) findViewById(R.id.et_palabraOculta);
        bMicro = (ImageButton)findViewById(R.id.bMicro);
        bNormal = (Button)findViewById(R.id.bnormal);
        bLectura = (Button)findViewById(R.id.blectura);
        bRepetir = (Button)findViewById(R.id.brepetir);
        bRellenar = (Button)findViewById(R.id.brellenar);
        bComprobar = (Button)findViewById(R.id.bcomprobar);
        bSeguir = (Button)findViewById(R.id.bseguir);
        bDescubrir = (Button)findViewById(R.id.bdescubrir);
        bRepetirFrase = (Button)findViewById(R.id.brepetirfrase);
    }

    private Cancion getCancionById(int id) {
        if (source == ACTIVIDAD_VISTA_CANCION_LOCAL) {
            Log.d(LOG_TAG, "ACTIVIDAD_VISTA_CANCION_LOCAL");
            return CancionesVector.getInstance().elemento(id);
        } else {
            Log.d(LOG_TAG, "ACTIVIDAD_VISTA_CANCION_REMOTA");
        }

        return ListaCancionesRemoto.adaptador.getItem(id);
    }

    private void getEtiquetado(final Cancion cancion, final boolean lanzarAlerta) {
        if (source == ACTIVIDAD_VISTA_CANCION_LOCAL) {
            if(!cancion.getEtiquetado() && lanzarAlerta){
                lanzarAlertaEtiquetado();
            }

            return;
        }

        final Thread waitingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                cancion.downloadXML();
            }

        });

        waitingThread.start();
    }

    /**
     * Asigna los datos del elemento cancion seleccionado a la Vista_Cancion
     * @param id posición del objeto canción en el vector canciones.
     */
    public void ponInfoCancion(int id, boolean lanzarAlerta) {
        cancion = getCancionById(id);
        Log.d(LOG_TAG, cancion.toString());
        getEtiquetado(cancion, lanzarAlerta);
        bindViews();
        titulo.setText(cancion.getTitulo());
        if (source == ACTIVIDAD_VISTA_CANCION_LOCAL) {
            Bitmap image = obtenerPortadaSD(mContext, cancion.getNombreFichero());
            portada.setImageBitmap(image);
        }

        portada.setImageUrl(cancion.getImagen(), VolleySingleton.getInstance(mContext).getLectorImagenes());

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
            if (cancion.getAudio().compareTo("") != 0) {
                Uri audio = Uri.parse(cancion.getAudio());
                if ((source == ACTIVIDAD_VISTA_CANCION_LOCAL && validarLeerSD()) || source == ACTIVIDAD_VISTA_CANCION_REMOTA) {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(this, audio);
                    mediaPlayer.prepare();
                }
            }

        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, "FileNotFoundException " + cancion.getNombreFichero(), e);
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
                        startActivityForResult(i,ACTIVIDAD_ETIQUETAR);
                    }

                })
                .setNegativeButton(R.string.cancelar,null)
                .show();
    }

    /***
     * inicializa las Vistas a su estado original teniendo en cuenta si la canción está etiquetada o no
     */
    public void inicializaVistas(){
        Log.d(LOG_TAG, "inicializaVistas");

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
            bRellenar.setVisibility(View.INVISIBLE);
            bLectura.setVisibility(View.INVISIBLE);
            fOriginal.setVisibility(View.INVISIBLE);
            fTraducida.setVisibility(View.INVISIBLE);
        } else {
            bRepetir.setVisibility(View.VISIBLE);
            bRellenar.setVisibility(View.VISIBLE);
            bLectura.setVisibility(View.VISIBLE);
            fOriginal.setVisibility(View.VISIBLE);
            fTraducida.setVisibility(View.VISIBLE);
        }

        et_palabraOculta.setVisibility(View.GONE);
        tv_palabraOculta.setVisibility(View.GONE);
        bComprobar.setVisibility(View.GONE);
        bSeguir.setVisibility(View.GONE);
        bDescubrir.setVisibility(View.GONE);
        bRepetirFrase.setVisibility(View.GONE);

        fTraducida.setText("");
        fOriginal.setText("");
        fauxiliar.setText("");
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

        activaReproduccion();
        mediaPlayer.start();
    }

    /***
     * Se activa la repodrucción Modo Lectura
     * @param view
     */
    public void sePulsaLectura(View view){
        pulsadoNormal = false;
        contador = 0;
        descativarBotones();
        modoRepro = MODO_LECTURA_INICIAL;
        activaReproduccion();
    }

    /***
     * Se activa la repodrucción Modo Repetir
     * @param view
     */
    public void sePulsaRepetir(View view){
        pulsadoNormal = false;
        contador = 0;
        descativarBotones();
        modoRepro = MODO_REPETICION;
        titulo.setVisibility(View.INVISIBLE);
        portada.setVisibility(View.INVISIBLE);
        fOriginal.setVisibility(View.INVISIBLE);
        fauxiliar.setVisibility(View.VISIBLE);
        activaReproduccion();
    }

    /***
     * Se activa la repodrucción Modo Repetir
     * @param view
     */
    public void sePulsaRellenar(View view){
        descativarBotones();
        contador = 0;
        pulsadoNormal = false;
        modoRepro = MODO_RELLENAR;
        titulo.setVisibility(View.INVISIBLE);
        portada.setVisibility(View.GONE);
        fOriginal.setVisibility(View.VISIBLE);
        fTraducida.setVisibility(View.GONE);
        et_palabraOculta.setVisibility(View.VISIBLE);
        tv_palabraOculta.setVisibility(View.VISIBLE);
        bComprobar.setVisibility(View.VISIBLE);
        bSeguir.setVisibility(View.VISIBLE);
        bDescubrir.setVisibility(View.VISIBLE);
        bRepetirFrase.setVisibility(View.VISIBLE);
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
        bRellenar.setVisibility(View.INVISIBLE);
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
    class MiThread extends Thread {
        int result;
        Frase frase;
        boolean repetir = true;

        // Modo rellenar
        private String[] palabrasOcultas;
        private int fraseActual;
        private String[] frasesARellenar;
        private boolean goOnClicked;
        private boolean repetirFrase;

        private void onPreExecuteModoRellenar() {
            frasesARellenar = new String[cancion.getLetra().size()];
            palabrasOcultas = new String[cancion.getLetra().size()];

            for (int i = 0; i < frasesARellenar.length; i++) {
                List<Frase> letra = cancion.getLetra();
                String fraseOriginal = letra.get(i).getFraseOriginal();
                String[] palabras = fraseOriginal.split(" ");

                int numRandom =  (int )(Math.random() * palabras.length);
                palabrasOcultas[i] = palabras[numRandom].trim().toLowerCase();
                frasesARellenar[i] = fraseOriginal.replaceAll(palabras[numRandom], " _________ ");
            }

            bComprobar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String[] caracteresEspeciales = {"?", "¿", ",", ";", ".", ":", "!", "¡", "\""};
                    String palabraOculta = palabrasOcultas[fraseActual];
                    String input = et_palabraOculta.getText().toString().toLowerCase().trim();

                    for (String c : caracteresEspeciales) {
                        palabraOculta = palabraOculta.replace(c, "");
                    }

                    if (input.compareTo(palabraOculta) == 0) {
                        Log.d(LOG_TAG, "PALABRA CORRECTA");
                        mostrarMensaje(VistaCancionActivity.this, "¡Palabra correcta!");
                        bSeguir.setEnabled(true); bSeguir.getBackground().setAlpha(255);
                        fOriginal.setText(cancion.getLetra().get(fraseActual).getFraseOriginal());
                    } else {
                        mostrarMensaje(VistaCancionActivity.this, "¡No es correcto!");
                    }
                }
            });

            et_palabraOculta.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    Log.d(LOG_TAG, "onKey");
                    if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                        bComprobar.callOnClick();
                        return true;
                    }
                    return false;
                }
            });

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.bseguir:
                            goOnClicked = true;
                            et_palabraOculta.setText("");
                            break;
                        case R.id.bdescubrir:
                            fOriginal.setText(cancion.getLetra().get(fraseActual).getFraseOriginal());
                            bSeguir.setEnabled(true); bSeguir.getBackground().setAlpha(255);
                            break;
                        case R.id.brepetirfrase:
                            if (!repetirFrase) repetirFrase = true;
                            break;
                    }
                }
            };

            bSeguir.setOnClickListener(onClickListener);
            bDescubrir.setOnClickListener(onClickListener);
            bRepetirFrase.setOnClickListener(onClickListener);
            mediaPlayer.start();
        }

        public MiThread() {
            if (modoRepro == MODO_RELLENAR) {
                onPreExecuteModoRellenar();
            }
        }

        @Override public void run() {
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
                    case MODO_RELLENAR:
                        modoRellenar();
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

                while(mediaPlayer.getCurrentPosition()<cancion.getLetra().get(contador).getTiempoIni() && corriendo ){
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
         * Gestión del modo Rellenar
         * Para cada frase traducida: la reproduce en el audio, oculta una palabra del texto y espera a que la rellenes.
         */
        public void modoRellenar() {
            frase = cancion.getLetra().get(contador);
            goOnClicked = false;
            repetirFrase = false;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bSeguir.setEnabled(false); bSeguir.getBackground().setAlpha(128);
                    bComprobar.setEnabled(false); bComprobar.getBackground().setAlpha(128);
                    bDescubrir.setEnabled(false); bDescubrir.getBackground().setAlpha(128);
                    bRepetirFrase.setEnabled(false); bRepetirFrase.getBackground().setAlpha(128);
                }
            });

            try {
                while(corriendo && mediaPlayer.getCurrentPosition() < cancion.getLetra().get(contador).getTiempoIni() ){
                    sleep(1);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error ModoRellenar sleep1:" + e);
            }


            while (corriendo && mediaPlayer.getCurrentPosition() <= frase.getTiempoFin()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (contador < cancion.getLetra().size() && contador >= 0) {
                            fraseActual = contador;
                            fOriginal.setText(frasesARellenar[contador]);
                        }
                    }
                });
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bComprobar.setEnabled(true); bComprobar.getBackground().setAlpha(255);
                    bDescubrir.setEnabled(true); bDescubrir.getBackground().setAlpha(255);
                    bRepetirFrase.setEnabled(true); bRepetirFrase.getBackground().setAlpha(255);
                }
            });

            if (corriendo) mediaPlayer.pause();

            try {
                while (!goOnClicked) {
                    sleep(1000);
                    if (repetirFrase) {
                        mediaPlayer.seekTo(frase.getTiempoIni());
                        goOnClicked = true;
                        contador--;
                    }
                }
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Error ModoRellenar sleep2:" + e);
            }


            if (corriendo) mediaPlayer.start();
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
                public void onStart(String s) {}

                @Override
                public void onDone(String utteranceId) {
                    estaHablando = false;
                }

                @Override
                public void onError(String s) {}
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
        Log.d(LOG_TAG, "onResume");
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onStop() {
        corriendo = false;
        try {
            if (mediaPlayer != null) {
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
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed " + corriendo);
        if (!corriendo) {
            finish();
            return;
        }

        corriendo = false;
        mostrarMensaje(mContext, "Pulse otra vez para volver a la lista");

        if (tts != null) tts.stop();

        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            if (miThread != null && miThread.isAlive()) {

                miThread.getThreadGroup().interrupt();
                miThread = null;
            }

        } catch (UnsupportedOperationException e) {
            Log.e(LOG_TAG, "onStop:MediaPlayer", e);
        } catch (Exception e){
            Log.e(LOG_TAG, "onStop:MediaPlayer", e);
        }

        ponInfoCancion((int)id, false);
    }


    //Guardar el estado de la Actividad VistaCancion
    @Override
    public void onSaveInstanceState(Bundle estadoGuardado){
        super.onSaveInstanceState(estadoGuardado);
        if(mediaPlayer != null){
            int pos = mediaPlayer.getCurrentPosition();
            estadoGuardado.putInt("posicion", pos);
        }

        int cont = contador;
        estadoGuardado.putInt("contador", cont);
        estadoGuardado.putBoolean("pulsadoNormal", pulsadoNormal);
        estadoGuardado.putString("modoRepro", modoRepro);
        estadoGuardado.putBoolean("reinicio",true);
        estadoGuardado.putBoolean("corriendo",corriendo);
    }


    @Override
    public void onRestoreInstanceState(Bundle estadoGuardado){
        Log.d(LOG_TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(estadoGuardado);
        if (estadoGuardado != null){
            contador = estadoGuardado.getInt("contador");
            pulsadoNormal = estadoGuardado.getBoolean("pulsadoNormal");
            modoRepro = estadoGuardado.getString("modoRepro");
            corriendo = estadoGuardado.getBoolean("corriendo");
            posicion = estadoGuardado.getInt("posicion");
        }
    }
}


