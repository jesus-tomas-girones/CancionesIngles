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

    public static Genero getByKey(int key){
        switch (key){
            case 0: return OTROS;
            case 1: return ROCK;
            case 2: return POP;
            case 3: return RAP;
            case 4: return BLUES;
            case 5: return JAZZ;
            case 6: return ClASICA;
            case 7: return DISCO;
            case 8: return SALSA;
            default: return OTROS;
        }
    }


}
