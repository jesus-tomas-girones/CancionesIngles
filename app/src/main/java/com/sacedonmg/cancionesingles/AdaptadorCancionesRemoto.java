package com.sacedonmg.cancionesingles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by Ana María Arrufat on 18/09/2017.
 */
public class AdaptadorCancionesRemoto extends FirebaseRecyclerAdapter<Cancion, ViewHolder> implements ChildEventListener {

    protected LayoutInflater inflador;
    protected Context contexto;
    protected View.OnClickListener onClickListener;

    public void setOnItemClickListener(View.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    // Firebase variables
    protected DatabaseReference songsReference;
    private ArrayList<DataSnapshot> items;
    private ArrayList<String> keys;

    public AdaptadorCancionesRemoto(Context contexto, DatabaseReference songsReference) {
        super(Cancion.class, R.layout.elemento_lista, ViewHolder.class, songsReference);
        this.contexto = contexto;

        FirebaseSingleton firebaseSingleton = FirebaseSingleton.getInstance();
        this.songsReference = firebaseSingleton.getCancionesReference();
        this.songsReference.addChildEventListener(this);

        items = new ArrayList<DataSnapshot>();
        keys = new ArrayList<String>();

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
    public void populateViewHolder(final ViewHolder holder, Cancion cancion, int posicion) {
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
}
