package com.sacedonmg.cancionesingles;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.sacedonmg.cancionesingles.ListaCanciones.vectorCanciones;

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


    /**
     * Metodo para mostrar mensajes (Toast)
     * @param context contexto de la aplicación
     * @param mensaje el mensaje a mostrar
     */
    static void mostrarMensaje (final Context context, final String mensaje){
        manejador.post(new Runnable(){
            public void run(){
                Toast.makeText(context,mensaje,Toast.LENGTH_LONG).show();
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
        vectorCanciones = CancionesVector.getInstance();
        for(String nombreXML: listaFicherosXML){
            cancion = new Cancion ();
            String nombreFichero = nombreXML.substring(0, nombreXML.lastIndexOf("."));
            cancion.setNombreFichero(nombreFichero);
            try {
                cancion.leerXML(rutaCarpeta+nombreXML);
            }catch (Exception e){
                Log.e(LOG_TAG, e.getMessage(), e);
            }

            cancion.toStringCancion();
            vectorCanciones.anyade(cancion);
            Log.d(LOG_TAG, nombreFichero);
        }

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


    /***
     * Obtenemos el valor de la dificultad a través de su texto
     * @param dificultad
     * @return
     */
    static Float obtenerValorDificultad(String dificultad){

        float valor = 1;
        switch (dificultad){

            case "Medio":
                valor = 2;
                break;
            case "Dificil":
                valor = 3;
                break;
            case "Facil":
            default:
                valor=1;
                break;
        }
        return valor;
    }

}
