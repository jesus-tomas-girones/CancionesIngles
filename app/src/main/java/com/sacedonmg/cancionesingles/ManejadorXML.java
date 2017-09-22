package com.sacedonmg.cancionesingles;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by MGS on 09/07/2016.
 */
public class ManejadorXML extends DefaultHandler{
    private Cancion cancionXML;
    private Frase frase;
    private String tmpValue;

    public Cancion getCancionXML(){
        return this.cancionXML;
    }

    @Override
    public void startDocument() throws SAXException {
        cancionXML = new Cancion();
    }

    @Override
    public void startElement(String uri, String nombreLocal, String nombreCualif, Attributes atr) throws SAXException {

        if(nombreCualif.equalsIgnoreCase("frase")){
            frase = new Frase();
        }
    }

    @Override
    public void characters(char [] c, int comienzo, int lon){
        tmpValue = new String(c, comienzo, lon);
    }

    @Override
    public void endElement(String uri, String nombreLocal, String nombreCualif) throws SAXException{
        if(nombreCualif.equalsIgnoreCase("titulo")){
            cancionXML.setTitulo(tmpValue);
        }
        if(nombreCualif.equalsIgnoreCase("autor")){
            cancionXML.setAutor(tmpValue);
        }
        if(nombreCualif.equalsIgnoreCase("genero")){
            cancionXML.setGenero(Integer.parseInt(tmpValue));
        }
        if(nombreCualif.equalsIgnoreCase("dificultad")){
            cancionXML.setDificultad(Integer.parseInt(tmpValue));
        }
        if (nombreCualif.equalsIgnoreCase("etiquetado")){
            Log.e("ManejadorXML", ""+Boolean.parseBoolean(tmpValue));

            cancionXML.setEtiquetado(Boolean.parseBoolean(tmpValue));
        }

        if (nombreCualif.equalsIgnoreCase("tiempoIni")){
            frase.setTiempoIni(Integer.parseInt(tmpValue));
        }
        if (nombreCualif.equalsIgnoreCase("tiempoFin")){
            frase.setTiempoFin(Integer.parseInt(tmpValue));
        }
        if (nombreCualif.equalsIgnoreCase("fraseOriginal")){
            frase.setFraseOriginal(tmpValue);
        }
        if (nombreCualif.equalsIgnoreCase("fraseTraducida")){
            frase.setFraseTraducida(tmpValue);
        }
        if (nombreCualif.equalsIgnoreCase("frase")){
            cancionXML.getLetra().add(frase);
        }
    }
}
