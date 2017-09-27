package com.sacedonmg.cancionesingles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import static com.sacedonmg.cancionesingles.UtilidadesCanciones.obtenerPortadaSD;

/**
 * Created by MGS on 03/09/2016.
 */
public class AdaptadorCancionesLocal extends RecyclerView.Adapter<ViewHolder> {

    protected Canciones canciones;
    protected LayoutInflater inflador;
    protected Context contexto;
    private String LOG_TAG = "CI::AdaptadorLocal";

    protected View.OnClickListener onClickListener;

    public void setOnItemClickListener(View.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    public AdaptadorCancionesLocal(Context contexto) {
        this.contexto = contexto;
        this.canciones = CancionesVector.getInstance();
        inflador = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    //Creamos el ViewHolder con la vista de un elemento sin personalizar
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflamos la vista desde el xml
        View v = inflador.inflate(R.layout.elemento_lista, parent, false);
        v.setOnClickListener(onClickListener);
        return new ViewHolder(v);
    }

    //Usando como base el ViewHolder y lo personalizamos
    @Override
    public void onBindViewHolder(ViewHolder holder, int posicion) {
        Cancion cancion = getItem(posicion);
        personalizaVistas(holder, cancion);
    }

    /***
     * Personalizamos un ViewHolder a partir de una cancion
     *
     * @param holder
     * @param cancion
     */
    @SuppressLint("NewApi")
    public void personalizaVistas(ViewHolder holder, Cancion cancion) {
        holder.titulo.setText(cancion.getTitulo());
        holder.autor.setText(cancion.getAutor());
        Bitmap image = obtenerPortadaSD(contexto, cancion.getNombreFichero());
        holder.portada.setImageBitmap(image);
        holder.portada.setScaleType(ImageView.ScaleType.FIT_END);
        holder.portada.setImageUrl(cancion.getImagen(), VolleySingleton.getInstance(contexto).getLectorImagenes());

        switch (cancion.getDificultad()) {
            case DIFICIL:
                holder.dificultad.setImageDrawable(contexto.getDrawable(R.drawable.dificultad_alta));
                break;
            case MEDIO:
                holder.dificultad.setImageDrawable(contexto.getDrawable(R.drawable.dificultad_media));
                break;
            case FACIL:
                holder.dificultad.setImageDrawable(contexto.getDrawable(R.drawable.dificultad_baja));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return canciones.tamanyo();
    }

    public Cancion getItem(int pos) {
        return canciones.elemento(pos);
    }

}
