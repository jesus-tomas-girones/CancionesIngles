package com.sacedonmg.cancionesingles;


import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.sacedonmg.cancionesingles.MainActivity.READ_EXTERNAL_STORAGE_PERMISSION;
import static com.sacedonmg.cancionesingles.MainActivity.WRITE_EXTERNAL_STORAGE_PERMISSION;

/**
 * Created by MGS on 09/07/2016.
 */
public final class UtilidadesCanciones {
    private static final String LOG_TAG = "CI::UtilidadesCanciones";

    static final String EXTENSION_AUDIO = ".mp3";
    static final String EXTENSION_IMAGEN = ".jpg";
    static final String EXTENSION_TXTORIGINAL = "original.txt";
    static final String EXTENSION_TXTTRADUCIDO = "traducido.txt";
    static final String EXTENSION_XML = ".xml";

    static String nombreFicheroDemo1 = "yesterday";
    static String nombreFicheroDemo2 = "HeroOfWar";
    static String nombreFicheroDemo3 = "billiejean";

    static String rutaCarpeta = Environment.getExternalStorageDirectory()+"/cancionesingles/";
    private static Handler manejador = new Handler();

    // static boolean subiendoDatos = false;
    static boolean[] subiendoDatos = new boolean[5];
    static boolean[] errorSubiendoDatos = new boolean[5];

    /**
     * Metodo para mostrar mensajes (Toast)
     * @param context contexto de la aplicación
     * @param mensaje el mensaje a mostrar
     */
    static void mostrarMensaje (final Context context, final String mensaje){
        manejador.post(new Runnable(){
            public void run(){
                Toast toast = Toast.makeText(context,mensaje,Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }


    // Generar todos los ficheros (audio, imagen, txtOriignal y traducido y Xml) a partir del nombreFichero

    /**
     * Genera todos los ficheros añadiendo al nombre del fichero las extensiones: .mp3, .jpg, original.txt, traducido.txt, .xml
     *
     * @return  Vector con los nombres de los ficheros mas las extensiones
     */
    static String[] generarFicheros() {

        String [] nombreFicheros = new String[] {
                nombreFicheroDemo1+ EXTENSION_AUDIO,
                nombreFicheroDemo1+ EXTENSION_IMAGEN,
                nombreFicheroDemo1+ EXTENSION_TXTORIGINAL,
                nombreFicheroDemo1+ EXTENSION_TXTTRADUCIDO,
                nombreFicheroDemo1+ EXTENSION_XML,
                nombreFicheroDemo2+ EXTENSION_AUDIO,
                nombreFicheroDemo2+ EXTENSION_IMAGEN,
                nombreFicheroDemo2+ EXTENSION_TXTORIGINAL,
                nombreFicheroDemo2+ EXTENSION_TXTTRADUCIDO,
                nombreFicheroDemo2+ EXTENSION_XML,
                nombreFicheroDemo3+ EXTENSION_AUDIO,
                nombreFicheroDemo3+ EXTENSION_IMAGEN,
                nombreFicheroDemo3+ EXTENSION_TXTORIGINAL,
                nombreFicheroDemo3+ EXTENSION_TXTTRADUCIDO,
                nombreFicheroDemo3+ EXTENSION_XML};

        return nombreFicheros;
    }

    /**
     * Comprueba si se puede leer de la SD
     * @return  true si es posible.
     */
    static Boolean validarLeerSD(){
        String stadoSD = Environment.getExternalStorageState();
        if(!stadoSD.equals(Environment.MEDIA_MOUNTED) && (!stadoSD.equals(Environment.MEDIA_MOUNTED_READ_ONLY))){
            Log.v(LOG_TAG, "Imposible leer memoria externa");
            return false;
        }
        return true;
    }

    /**
     * Comprueba si se puede escribir en la SD
     * @return true si es posible
     */
    static Boolean validarEscribirSD(){
        String stadoSD = Environment.getExternalStorageState();
        if(!stadoSD.equals(Environment.MEDIA_MOUNTED)){
            Log.v(LOG_TAG, "Imposible escribir en la memoria externa");
            return false;
        }
        return true;
    }

    public static boolean crearArchivosEjemplo(Context context) {
        boolean resultado;
        File carpeta = new File(rutaCarpeta);

        if (carpeta.exists()) {
            Log.v(LOG_TAG, "Carpeta cancionesingles creada en la SD");
            String[] ficherosDemo = generarFicheros();
            for (String rutaFicheroDemo : ficherosDemo) {    ///Copiamos todos los ficherosDemo de Assets a la SD
                try {
                    String rutaFicheroSD = rutaCarpeta + rutaFicheroDemo;
                    copyFileFromAssets(context, rutaFicheroDemo, rutaFicheroSD);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            resultado = true;
        } else {
            Log.v(LOG_TAG, "ERROR: Carpeta cancionesingles no creada");
            resultado = false;
        }

        return resultado;
    }

    /**
     * Copia los ficheros Demo de la carpeta Assets a la SD
     * @param context  el contexto de la aplicación
     * @param file  nombre del fichero de la carpeta Assets a copiar
     * @param dest ruta a la SD
     * @throws Exception
     */
    static  void copyFileFromAssets(final Context context, final String file, final String dest) throws Exception
    {
        InputStream in = null;
        OutputStream fout = null;
        int count = 0;

        try
        {
            in = context.getAssets().open(file);
            fout = new FileOutputStream(new File(dest));

            byte data[] = new byte[1024];
            while ((count = in.read(data, 0, 1024)) != -1)
            {
                fout.write(data, 0, count);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (in != null)
            {
                try {
                    in.close();
                } catch (IOException e)
                {
                }
            }
            if (fout != null)
            {
                try {
                    fout.close();
                } catch (IOException e) {
                }
            }
        }
    }


    /**
     * Otener todos los ficheros .xml existentes en la SD.
     * @param path    ruta donde buscar los ficheros xml
     * @return
     */
    static List<String> getListOfFilesXML(final String path) {

        File files = new File(path);
        final List<String> list = new ArrayList<String>();
        final File [] filesFound = files.listFiles();

        if (filesFound != null && filesFound.length > 0) {
            for (File file : filesFound) {
                if(file.getName().contains(".xml")){
                    list.add(file.getName());
                    Log.v(LOG_TAG, file.getName());
                }

            }
        }
        return list;
    }


    /**
     * Sincroinizar Lista Reproducción: Genera un objeto canción por cada xml de la SD y lo añade al vectorCanciones
     */
    static void sincroListReproduccion() {
        List<String> listaFicherosXML = getListOfFilesXML(rutaCarpeta);
        Cancion cancion;
        CancionesVector vectorCanciones = CancionesVector.getInstance();
        for(String nombreXML: listaFicherosXML){
            cancion = new Cancion ();
            String nombreFichero = nombreXML.substring(0, nombreXML.lastIndexOf("."));
            cancion.setNombreFichero(nombreFichero);
            try {
                cancion.leerXML(rutaCarpeta+nombreXML);
            }catch (Exception e) {
                Log.d(LOG_TAG, e.getMessage(), e);
            }

            cancion.toStringCancion();
            vectorCanciones.anyade(cancion);
            Log.d(LOG_TAG, nombreFichero);
        }

        ListaCanciones.adaptador.notifyDataSetChanged();
    }

    /**
     * Obtenemos la imagen de portada de la SD y la visualizamos en el layout.
     * En caso de no existir la imagen de portada o no poder leer de la SD
     * se establece imagen por defecto.
     * @param nombreFichero nombre de la imagen
     * @return Bitmap
     */
    static  Bitmap obtenerPortadaSD( Context context, String nombreFichero){
        Bitmap image = null;
        boolean cargadaImagen = false;

        if (validarLeerSD()){
            String rutaImagen = rutaCarpeta+nombreFichero+EXTENSION_IMAGEN;
            File file = new File(rutaImagen);
            if(file.exists()){
                //Tenemos la foto guardada en la SD, asi que la cargamos
                image = BitmapFactory.decodeFile(rutaImagen);
                cargadaImagen=true;

            }

        }
        if(!cargadaImagen){
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.cancion);
        }
        return image;
    }


    /**
     * Obtenemos la ruta del fichero de audio
     * @param nombreFichero
     * @return
     */
    static String rutaAudio(String nombreFichero){
        return(rutaCarpeta+nombreFichero+EXTENSION_AUDIO);
    }


    /*
     *  Subir fichero a Firebase
     */
    private static void updateProgressDialog(ProgressDialog progressDialog, Context mContext) {
        int count = 0;
        int errores = 0;
        for (int i = 0; i < subiendoDatos.length; i++) {
            if (subiendoDatos[i]) count++;
            if (errorSubiendoDatos[i]) errores++;
        }

        progressDialog.setMessage("Quedan " + count + "/5 ficheros");
        if (count == 0) {
            progressDialog.dismiss();
            String msg = errores > 0 ? "Ha ocurrido algún error al subir los archivos. Inténtelo de nuevo" : "¡Canción subida con éxito!";
            mostrarMensaje(mContext, msg);
        }
    }

    private static String getNodoByFilename(String fileName) {
        String format = fileName.split("\\.")[1];
        switch (format) {
            case "mp3":
                return "audio";
            case "jpg":
                return "imagen";
            case "png":
                return "imagen";
            case "txt":
                if (fileName.contains("original")) return "txt_original";
                if (fileName.contains("traducido")) return "txt_traducido";
                break;
            case "xml":
                return "xml";
        }

        return "";
    }

    private static void subirFichero(String localPath, final DatabaseReference fileDataBaseRef, StorageReference cancionStorageRef, final ProgressDialog progressDialog, final int index, final Context mContext) {
        Log.d(LOG_TAG, "subiendo fichero " + localPath);

        Uri file = Uri.fromFile(new File(localPath));
        final String fileName = file.getLastPathSegment().toLowerCase();

        subiendoDatos[index] = true;
        StorageReference fileStorageRef = cancionStorageRef.child(fileName);
        UploadTask uploadTask = fileStorageRef.putFile(file);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(LOG_TAG, "onFailure " + fileName, exception);
                subiendoDatos[index] = false;
                errorSubiendoDatos[index] = true;
                updateProgressDialog(progressDialog, mContext);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(LOG_TAG, "onSuccess " + fileName);
                subiendoDatos[index] = false;
                errorSubiendoDatos[index] = false;

                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                String nodo = getNodoByFilename(fileName); Log.d(LOG_TAG, "getNodoByFileName -> " + nodo);

                if (nodo.compareTo("") != 0) {
                    DatabaseReference nodoRef = fileDataBaseRef.child(nodo);
                    nodoRef.setValue(downloadUrl.toString());
                }

                updateProgressDialog(progressDialog, mContext);
            }
        });
    }

    private static void subirFicheros(final Cancion cancion, ProgressDialog progressDialog, Context mContext) {
        String titulo = cancion.getNombreFichero().toLowerCase().replace(" ", "") + "/";
        FirebaseSingleton firebaseSingleton = FirebaseSingleton.getInstance();

        // DataBase reference
        DatabaseReference databaseReference = firebaseSingleton.getCancionesReference();
        final DatabaseReference cancionDatabaseRef = databaseReference.child(titulo);

        StorageReference storageReference = firebaseSingleton.getStorageReference();
        StorageReference cancionStorageRef = storageReference.child(titulo);

        progressDialog.setMessage("Quedan 5/5 ficheros");

        // Subir audio
        String localPath = cancion.getAudio();
         subirFichero(localPath, cancionDatabaseRef, cancionStorageRef, progressDialog, 0, mContext);

        // Subir imagen
        localPath = cancion.getImagen();
        subirFichero(localPath, cancionDatabaseRef, cancionStorageRef, progressDialog, 1, mContext);

        // Subir xml
        localPath = cancion.getXml();
        subirFichero(localPath, cancionDatabaseRef, cancionStorageRef, progressDialog, 2, mContext);

        // Subir letra original
        localPath = cancion.getTxt_original();
        subirFichero(localPath, cancionDatabaseRef, cancionStorageRef, progressDialog, 3, mContext);

        // Subir letra traducida
        localPath = cancion.getTxt_traducido();
        subirFichero(localPath, cancionDatabaseRef, cancionStorageRef, progressDialog, 4, mContext);

        final Thread waitingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int countSubiendo = 5;

                    while(countSubiendo > 0) {
                        countSubiendo = 0;
                        for (int i = 0; i < subiendoDatos.length; i++) {
                            countSubiendo = subiendoDatos[i] ? countSubiendo + 1 : countSubiendo;
                            // countError = countError[i] ? countError + 1 : countError;
                        }
                        TimeUnit.SECONDS.sleep(1);
                    }

                    cancionDatabaseRef.child("titulo").setValue(cancion.getTitulo());
                    cancionDatabaseRef.child("autor").setValue(cancion.getAutor());
                    cancionDatabaseRef.child("dificultad").setValue(cancion.getDificultad().ordinal());
                    cancionDatabaseRef.child("genero").setValue(cancion.getGenero().ordinal());
                    cancionDatabaseRef.child("id").setValue(cancion.getId());
                    String uid = FirebaseSingleton.getInstance().getCurrentUser().getUid();
                    cancionDatabaseRef.child("user").setValue(uid);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        waitingThread.start();
    }

    private static Task<Uri> checkIfFileExists(String titulo, String localPath) {
        FirebaseSingleton firebaseSingleton = FirebaseSingleton.getInstance();
        StorageReference storageReference = firebaseSingleton.getStorageReference();

        Uri file = Uri.fromFile(new File(localPath));
        StorageReference fileRef = storageReference.child(titulo + file.getLastPathSegment());
        return fileRef.getDownloadUrl();
    }

    public static void subirCancionAFireBase (final Cancion cancion, final ProgressDialog progressDialog, final Context mContext) {
        final String titulo = cancion.getNombreFichero().toLowerCase().replace(" ", "") + "/";

        progressDialog.setMessage("Comprobando si ya existe en el servidor...");
        progressDialog.show();

        FirebaseSingleton firebaseSingleton = FirebaseSingleton.getInstance();
        StorageReference storageReference = firebaseSingleton.getStorageReference();

        String localPath = cancion.getAudio();
        Uri file = Uri.fromFile(new File(localPath));
        String nodo = titulo + file.getLastPathSegment();
        StorageReference fileRef = storageReference.child(nodo);
        Task<Uri> task = fileRef.getDownloadUrl();
        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                progressDialog.dismiss();
                mostrarMensaje(mContext, "¡El archivo ya existe en el servidor!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                subirFicheros(cancion, progressDialog, mContext);
            }
        });
    }


    /*
     *  Eliminar canción de Firebase
     */
    static boolean[] borrandoDatos = {false, false, false, false, false};
    static boolean[] errorBorrandoDatos = {false, false, false, false, false};


    private static void resetVariablesBorrado () {
        for (int i = 0; i < borrandoDatos.length; i++) {
            borrandoDatos[i] = false;
            errorBorrandoDatos[i] = false;
        }
    }

    private static void borrarArchivoRemoto(StorageReference cancionRef, final String path, final int i) {
        Uri uri = Uri.parse(path);
        final String nodo = uri.getLastPathSegment();
        Log.d(LOG_TAG, "borrando fichero " + nodo);

        borrandoDatos[i] = true;
        StorageReference archivoRef = cancionRef.child(nodo);
        archivoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(LOG_TAG, "onSuccess: "+ nodo);
                borrandoDatos[i] = false;
                errorBorrandoDatos[i] = true;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(LOG_TAG, "onFailure: " + nodo, exception);
                borrandoDatos[i] = false;
                errorBorrandoDatos[i] = true;
            }
        });
    }

    public static void borrarCancionRemota(Cancion cancion) {
        StorageReference cancionStorageRef = FirebaseSingleton.getInstance().getStorageReference();
        borrarArchivoRemoto(cancionStorageRef, cancion.getAudio() , 0);
        borrarArchivoRemoto(cancionStorageRef, cancion.getXml(), 1);
        borrarArchivoRemoto(cancionStorageRef, cancion.getTxt_original(), 2);
        borrarArchivoRemoto(cancionStorageRef, cancion.getTxt_traducido(), 3);
        borrarArchivoRemoto(cancionStorageRef, cancion.getImagen(), 4);
        resetVariablesBorrado();
    }

}
