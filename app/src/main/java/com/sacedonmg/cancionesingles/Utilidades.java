package com.sacedonmg.cancionesingles;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import static com.sacedonmg.cancionesingles.MainActivity.READ_EXTERNAL_STORAGE_PERMISSION;
import static com.sacedonmg.cancionesingles.MainActivity.WRITE_EXTERNAL_STORAGE_PERMISSION;

/**
 * Created by Ana María Arrufat on 26/09/2017.
 */

public class Utilidades {

    private static int getCode(String permission) {
        switch (permission) {
            case android.Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return WRITE_EXTERNAL_STORAGE_PERMISSION;
            case android.Manifest.permission.READ_EXTERNAL_STORAGE:
                return READ_EXTERNAL_STORAGE_PERMISSION;
            default:
                return -1;
        }
    }

    private static String getDialogTitle(String permission) {
        switch (permission) {
            case android.Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "Es necesario acceder al almacenamiento externo para guardar las canciones descargadas";
            case android.Manifest.permission.READ_EXTERNAL_STORAGE:
                return "Es necesario acceder al almacenamiento externo para mostrar las canciones guardadas en el dispositivo";
            default:
                return "";
        }
    }

    public static void requestPermission(final String permission, final Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            new AlertDialog.Builder(activity)
                    .setTitle(null)
                    .setMessage(getDialogTitle(permission))
                    .setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int whichButton){
                            ActivityCompat.requestPermissions(activity, new String[] { permission}, getCode(permission));
                        }

                    })
                    .setNegativeButton(R.string.cancelar,null)
                    .show();

            return;
        }

        ActivityCompat.requestPermissions(activity, new String[] { permission}, getCode(permission));
    }

    public static boolean isPermissionGranted (String permission, Activity activity) {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
