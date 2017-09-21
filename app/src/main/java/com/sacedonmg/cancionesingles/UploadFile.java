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

/**
 * Created by Ana María Arrufat on 19/09/2017.
 */

class UploadFile extends AsyncTask<String, Integer, String> {
    private String LOG_TAG = "CI::UPLOAD_FILE";
    private String BASE_PATH = Environment.getExternalStorageDirectory().toString();
    private static ProgressDialog progressDialog;
    private Context mContext;
    private Resources resources;
    private String titulo;
    private boolean error = false;

    public UploadFile(Context mContext) {
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

    public void download(String strUrl) throws IOException {
        String fileName = strUrl.split("http://mmoviles.upv.es/canciones_ingles/")[1];
        URL url = new URL(strUrl);
        URLConnection conection = url.openConnection();
        conection.connect();

        InputStream input = new BufferedInputStream(url.openStream(), 8192);
        String filePath = BASE_PATH + "/cancionesingles/"+ fileName;

        Log.d(LOG_TAG, "Downloading: " + strUrl);

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
    protected String doInBackground(String... params) {
        this.titulo = params[0].split("http://mmoviles.upv.es/canciones_ingles/")[1];

        try {
            for (int i = 0; i < params.length; i++) {
                publishProgress(i);
                download(params[i]);
            }
        } catch (IOException e) {
            error = true;
            Log.e(LOG_TAG, e.getMessage());
        }

        return null;
    }

    /**
     * Updating progress bar
     * */
    protected void onProgressUpdate(Integer... progress) {
        int count = progress[0];
        progressDialog.setMessage("Descargando fichero " + (count + 1) + "/6");
        progressDialog.setProgress(count);
    }

    /**
     * After completing background task
     * Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(String file_url) {
        progressDialog.dismiss();
    }

}