package com.sacedonmg.cancionesingles;

/**
 * Created by MGS on 09/07/2016.
 */
public interface Canciones {
    Cancion elemento(int id); //Devuelve el elemento dado su id
    void anyade(Cancion cancion); //Añade el elemento indicado
    int nuevo(); //Añade un elemento en blanco y devuelve su id
    void borrar(int id); //Elimina el elemento con el id indicado
    int tamanyo(); //Devuelve el número de elementos
    void actualiza(int id, Cancion cancion); //Reemplaza un elemento
}
