package com.sacedonmg.cancionesingles;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import static com.sacedonmg.cancionesingles.UtilidadesCanciones.obtenerValorDificultad;

/**
 * Created by Ana María Arrufat on 18/09/2017.
 */
public class AdaptadorCancionesRemoto extends FirebaseRecyclerAdapter<Cancion,AdaptadorCancionesRemoto.ViewHolder> implements ChildEventListener {

    protected LayoutInflater inflador;
    protected Context contexto;
    protected View.OnClickListener onClickListener;

    public void setOnItemClickListener(View.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    // Firebase
    protected DatabaseReference songsReference;
    private ArrayList<DataSnapshot> items;
    private ArrayList<String> keys;

    public AdaptadorCancionesRemoto(Context contexto, DatabaseReference songsReference) {

        super(Cancion.class, R.layout.elemento_lista, AdaptadorCancionesRemoto.ViewHolder.class, songsReference);

        items = new ArrayList<DataSnapshot>();
        keys = new ArrayList<String>();
        inflador = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.contexto = contexto;
        this.songsReference = songsReference;
        this.songsReference.addChildEventListener(this);
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
    public void populateViewHolder(final ViewHolder holder, Cancion cancion, int posicion) {
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
        holder.portada.setImageUrl(cancion.getImagen(), VolleySingleton.getInstance(contexto).getLectorImagenes());
        Float valorRating = obtenerValorDificultad(cancion.getDificultad().getTextoDificultad());
        holder.dificultad.setRating(valorRating);
        holder.dificultad.isIndicator();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

     public Cancion getItem(int pos) {
        DataSnapshot item = items.get(pos);
        Cancion cancion = item.getValue(Cancion.class);
        return cancion;
    }

    // ChildEventListenermethods
    @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        items.add(dataSnapshot);
        keys.add(dataSnapshot.getKey());
        notifyItemInserted(getItemCount() - 1);
    }

    @Override public void onChildRemoved(DataSnapshot dataSnapshot) {
        String key = dataSnapshot.getKey();
        int index = keys.indexOf(key);
        if (index != -1) {
            keys.remove(index);
            items.remove(index);
            notifyItemRemoved(index);
        }
    }

    @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

    @Override public void onCancelled(DatabaseError databaseError) {}

    @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        String key = dataSnapshot.getKey();
        int index = keys.indexOf(key);
        if (index != -1) {
            items.set(index, dataSnapshot);
            notifyItemChanged(index, dataSnapshot.getValue(Cancion.class));
        }
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
}
