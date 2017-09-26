package com.sacedonmg.cancionesingles;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import static com.sacedonmg.cancionesingles.UtilidadesCanciones.EXTENSION_AUDIO;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.EXTENSION_IMAGEN;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.EXTENSION_TXTORIGINAL;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.EXTENSION_TXTTRADUCIDO;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.EXTENSION_XML;

/**
 * Created by Ana María Arrufat on 19/09/2017.
 */

class DownloadFile extends AsyncTask<Cancion, Integer, String> {
    private String LOG_TAG = "CI::DOWNLOAD_FILE";
    private String BASE_PATH = Environment.getExternalStorageDirectory().toString();
    private static ProgressDialog progressDialog;
    private Context mContext;
    private Resources resources;
    private String titulo;
    private boolean error = false;
    private Cancion cancion;

    public DownloadFile(Context mContext) {
        this.mContext = mContext;
        this.resources = mContext.getResources();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(6);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Descargando");
        if (!progressDialog.isShowing()) progressDialog.show();
    }

    public void download(String strUrl, String nombreFichero) throws IOException {
        URL url = new URL(strUrl);
        URLConnection conection = url.openConnection();
        conection.connect();

        InputStream input = new BufferedInputStream(url.openStream(), 8192);
        String filePath = BASE_PATH + "/cancionesingles/"+ nombreFichero;

        Log.d(LOG_TAG, "Downloading: " + cancion.getTitulo());

        OutputStream output = new FileOutputStream(filePath);

        int count;
        byte data[] = new byte[1024];
        while ((count = input.read(data)) != -1) output.write(data, 0, count);

        output.flush();
        output.close();
        input.close();
    }

    /**
     * Downloading file in background thread
     * */
    @Override
    protected String doInBackground(Cancion... params) {
        cancion = params[0];
        String nombreFichero = cancion.getTitulo().replace(" ", "").toLowerCase();
        try {
            download(cancion.getAudio(), nombreFichero + EXTENSION_AUDIO);
            publishProgress(1);
            download(cancion.getImagen(), nombreFichero + EXTENSION_IMAGEN);
            publishProgress(2);
            download(cancion.getXml(), nombreFichero + EXTENSION_XML);
            publishProgress(3);
            download(cancion.getTxt_original(), nombreFichero + EXTENSION_TXTORIGINAL);
            publishProgress(4);
            download(cancion.getTxt_traducido(), nombreFichero + EXTENSION_TXTTRADUCIDO);
            publishProgress(4);
        } catch (IOException e) {
            error = true;
            Log.e(LOG_TAG, e.getMessage());
        }

        // ...
        return null;
    }

    /**
     * Updating progress bar
     * */
    protected void onProgressUpdate(Integer... progress) {
        int count = progress[0];
        progressDialog.setMessage("Descargando fichero " + (count + 1) + "/5");
        progressDialog.setProgress(count);
    }

    /**
     * After completing background task
     * Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(String file_url) {
        progressDialog.dismiss();
        if (error) {
            return;
        }

        String nombreFichero = cancion.getTitulo().toLowerCase().replace(" ", "");
        cancion.setNombreFichero(nombreFichero);
        CancionesVector cancionesVector = CancionesVector.getInstance();
        cancionesVector.anyade(cancion);
    }

}