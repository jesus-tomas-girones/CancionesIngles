package com.sacedonmg.cancionesingles;

/**
 * Created by MGS on 09/07/2016.
 */
public enum Genero {
    OTROS ("Otros"),
    ROCK ("Rock and Roll"),
    POP ("Pop"),
    RAP ("Rap"),
    BLUES ("Blues"),
    JAZZ ("Jazz"),
    ClASICA ("Clasica"),
    DISCO ("Disco"),
    SALSA ("Salsa");


    private final String textoGenero;

    Genero(String texto){
        this.textoGenero = texto;
    }

    public String getTextoGenero(){
        return textoGenero;
    }

    public static String[] getGeneros(){
        String[] resultado = new String[Genero.values().length];
        for (Genero genero : Genero.values()){
            resultado[genero.ordinal()] = genero.textoGenero;
        }
        return resultado;
    }


}
