package com.sacedonmg.cancionesingles;

/**
 * Created by MGS on 09/07/2016.
 */
public class Frase {

    private int tiempoIni;
    private int tiempoFin;
    private String fraseOriginal;
    private String fraseTraducida;

    public Frase(int tiempoIni, int tiempoFin, String fraseOriginal, String fraseTraducida) {
        this.tiempoIni = tiempoIni;
        this.tiempoFin = tiempoFin;
        this.fraseOriginal = fraseOriginal;
        this.fraseTraducida = fraseTraducida;
    }

    public Frase() {
        this.tiempoIni = 0;
        this.tiempoFin = 0;
        this.fraseOriginal = "";
        this.fraseTraducida = "";

    }


    public int getTiempoIni() {
        return tiempoIni;
    }

    public void setTiempoIni(int tiempoIni) {
        this.tiempoIni = tiempoIni;
    }

    public int getTiempoFin() {
        return tiempoFin;
    }

    public void setTiempoFin(int tiempoFin) {
        this.tiempoFin = tiempoFin;
    }

    public String getFraseOriginal() {
        return fraseOriginal;
    }

    public void setFraseOriginal(String fraseOriginal) {
        this.fraseOriginal = fraseOriginal;
    }

    public String getFraseTraducida() {
        return fraseTraducida;
    }

    public void setFraseTraducida(String fraseTraducida) {
        this.fraseTraducida = fraseTraducida;

    }
}
