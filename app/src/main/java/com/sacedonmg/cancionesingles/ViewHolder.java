package com.sacedonmg.cancionesingles;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

/**
 * Created by Ana María Arrufat on 20/09/2017.
 */

public class ViewHolder extends RecyclerView.ViewHolder {
    public TextView titulo;
    public TextView autor;
    public NetworkImageView portada;
    public ImageView dificultad;

    public ViewHolder(View itemView) {
        super(itemView);
        titulo = (TextView) itemView.findViewById(R.id.nombreCancion);
        autor = (TextView) itemView.findViewById(R.id.autorCancion);
        portada = (NetworkImageView) itemView.findViewById(R.id.imagenPortada);
        dificultad = (ImageView) itemView.findViewById(R.id.iv_dificultad);
    }
}