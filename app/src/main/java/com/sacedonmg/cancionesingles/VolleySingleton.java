package com.sacedonmg.cancionesingles;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {
    private static RequestQueue colaPeticiones;
    private static ImageLoader lectorImagenes;
    private static VolleySingleton ourInstance;
    private String LOG_TAG = "CI::VOLLEY_SINGLETON";

    public static VolleySingleton getInstance(Context mContext) {
        if (ourInstance == null) {
            ourInstance = new VolleySingleton(mContext);
        }

        return ourInstance;

    }

    private VolleySingleton(Context mContext) {
        colaPeticiones = Volley.newRequestQueue(mContext);
        lectorImagenes = new ImageLoader(colaPeticiones,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(10);

                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }

                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }
                });
    }

    public static RequestQueue getColaPeticiones() {
        return colaPeticiones;
    }

    public static ImageLoader getLectorImagenes() {
        return lectorImagenes;
    }
}
