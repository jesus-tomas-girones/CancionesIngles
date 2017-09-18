package com.sacedonmg.cancionesingles;
/**
 * Created by MGS on 30/06/2016.
 */
public enum Dificultad {
    FACIL ("Facil"),
    MEDIO ("Medio"),
    DIFICIL ("Dificil");

    private String textoDificultad;

    Dificultad (String texto){
        this.textoDificultad = texto;
    }

    public  String getTextoDificultad () {
        return textoDificultad;
    }

    public static String[] getDificultades(){
        String[] resultado = new String[Dificultad.values().length];
        for (Dificultad dificultad : Dificultad.values()){
            resultado[dificultad.ordinal()] = dificultad.textoDificultad;
        }

        return resultado;
    }

    public static Dificultad getByKey(int key){
        switch (key){
            case 0: return FACIL;
            case 1: return MEDIO;
            case 2: return DIFICIL;
            default: return FACIL;
        }
    }
}