package com.sacedonmg.cancionesingles;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import static com.sacedonmg.cancionesingles.UtilidadesCanciones.obtenerPortadaSD;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.obtenerValorDificultad;

/**
 * Created by MGS on 03/09/2016.
 */
public class AdaptadorCancionesLocal extends RecyclerView.Adapter<AdaptadorCancionesLocal.ViewHolder> {

    protected Canciones canciones;
    protected LayoutInflater inflador;
    protected Context contexto;

    protected View.OnClickListener onClickListener;

    public void setOnItemClickListener(View.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    public AdaptadorCancionesLocal(Context contexto) {
        this.contexto = contexto;
        this.canciones = CancionesVector.getInstance();
        inflador = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //Creamos nuestro viewHolder, con los tipos de elementos a modificar
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titulo;
        public TextView autor;
        public NetworkImageView portada;
        public RatingBar dificultad;

        public ViewHolder(View itemView) {
            super(itemView);
            titulo = (TextView) itemView.findViewById(R.id.nombreCancion);
            autor = (TextView) itemView.findViewById(R.id.autorCancion);
            portada = (NetworkImageView) itemView.findViewById(R.id.imagenPortada);
            dificultad = (RatingBar) itemView.findViewById(R.id.ratDificultad);
        }
    }

    //Creamos el ViewHolder con la vista de un elemento sin personalizar
    @Override
    public AdaptadorCancionesLocal.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflamos la vista desde el xml
        View v = inflador.inflate(R.layout.elemento_lista, parent, false);
        v.setOnClickListener(onClickListener);
        return new AdaptadorCancionesLocal.ViewHolder(v);
    }

    //Usando como base el ViewHolder y lo personalizamos
    @Override
    public void onBindViewHolder(ViewHolder holder, int posicion) {
        Cancion cancion = canciones.elemento(posicion);
        personalizaVistas(holder, cancion);
    }

    /***
     * Personalizamos un ViewHolder a partir de una cancion
     *
     * @param holder
     * @param cancion
     */
    public void personalizaVistas(ViewHolder holder, Cancion cancion) {
        holder.titulo.setText(cancion.getTitulo());
        holder.autor.setText(cancion.getAutor());
        Bitmap image = obtenerPortadaSD(contexto, cancion.getNombreFichero());
        holder.portada.setImageBitmap(image);
        holder.portada.setScaleType(ImageView.ScaleType.FIT_END);
        holder.portada.setImageUrl(cancion.getImagen(), VolleySingleton.getInstance(contexto).getLectorImagenes());
        Float valorRating = obtenerValorDificultad(cancion.getDificultad().getTextoDificultad());
        holder.dificultad.setRating(valorRating);
        holder.dificultad.isIndicator();
    }

    @Override
    public int getItemCount() {
        return canciones.tamanyo();
    }

    public Cancion getItem(int pos) {
        return canciones.elemento(pos);
    }

}
