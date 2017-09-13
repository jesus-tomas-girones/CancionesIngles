package com.sacedonmg.cancionesingles;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static com.sacedonmg.cancionesingles.UtilidadesCanciones.*;

/**
 * Created by MGS on 09/07/2016.
 */
public class Cancion {
    private static final String LOG_TAG = "Cancion";
    private String titulo;
    private String autor;
    private Genero genero;
    private Dificultad dificultad;
    private String nombreFichero;


    private Boolean etiquetado;
    private List<Frase> letra;

    public Cancion ( String titulo, String autor, Genero genero, Dificultad dificultad, String nombreFichero, Boolean etiquetado, ArrayList<Frase> letra){
        this.titulo = titulo;
        this.autor = autor;
        this.genero = genero;
        this.dificultad = dificultad;
        this.nombreFichero = nombreFichero;
        this.etiquetado = etiquetado;
        this.letra = letra;
    }
    public Cancion (){
        this.titulo = "";
        this.autor = "";
        this.genero = Genero.OTROS;
        this.dificultad = Dificultad.FACIL;
        this.nombreFichero = "";
        this.etiquetado = false;
        this.letra = new ArrayList<Frase>();
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    public Dificultad getDificultad() {
        return dificultad;
    }

    public void setDificultad(Dificultad dificultad) {
        this.dificultad = dificultad;
    }

    public String getNombreFichero() {
        return nombreFichero;
    }

    public void setNombreFichero(String nombreFichero) {
        this.nombreFichero = nombreFichero;
    }

    public List<Frase> getLetra() {
        return letra;
    }

    public void setLetra(ArrayList<Frase> letra) {
        this.letra = letra;
    }
    public Boolean getEtiquetado() {
        return etiquetado;
    }

    public void setEtiquetado(Boolean etiquetado) {
        this.etiquetado = etiquetado;
    }


    /**
     * Leer un XML para generar un objeto canción
     * @param path ruta al fichero XML
     * @throws Exception
     */
    public void leerXML (String path) throws Exception{

        File xmlFile = new File(path);
        FileInputStream xmlFileInputStream = new FileInputStream (xmlFile);
        InputSource inputSource = new InputSource(xmlFileInputStream);
        SAXParserFactory fabrica = SAXParserFactory.newInstance();
        SAXParser parser = fabrica.newSAXParser();
        XMLReader lector = parser.getXMLReader();
        ManejadorXML manejadorXML = new ManejadorXML();
        lector.setContentHandler(manejadorXML);
        lector.parse(inputSource);

        this.titulo =  manejadorXML.getCancionXML().getTitulo();
        this.autor =  manejadorXML.getCancionXML().getAutor();
        this.genero = manejadorXML.getCancionXML().getGenero();
        this.dificultad = manejadorXML.getCancionXML().getDificultad();
        this.etiquetado =  manejadorXML.getCancionXML().getEtiquetado();
        this.letra = manejadorXML.getCancionXML().getLetra();


    }

    /***
     * Genera un xml con los datos de la Canción
     */

    public void escribirXML(){
        if (validarEscribirSD()){
            String path =rutaCarpeta+this.nombreFichero+EXTENSION_XML;


            try{
                FileOutputStream xmlFile = new FileOutputStream(path,true);
                XmlSerializer serializador = Xml.newSerializer();
                serializador.setOutput(xmlFile,"UTF-8");
                serializador.startDocument("UTF-8",true);
                serializador.startTag("", "cancion");
                serializador.startTag("","titulo");
                serializador.text(this.titulo);
                serializador.endTag("","titulo");
                serializador.startTag("","autor");
                serializador.text(this.autor);
                serializador.endTag("","autor");
                serializador.startTag("","genero");
                serializador.text(String.valueOf(this.genero.ordinal()));
                serializador.endTag("","genero");
                serializador.startTag("","dificultad");
                serializador.text(String.valueOf(this.dificultad.ordinal()));
                serializador.endTag("","dificultad");
                serializador.startTag("","etiquetado");
                serializador.text(String.valueOf(this.etiquetado));
                serializador.endTag("","etiquetado");
                serializador.startTag("","letra");
                for(Frase frase:this.letra){
                    serializador.startTag("","frase");
                    serializador.startTag("","tiempoIni");
                    serializador.text(String.valueOf(frase.getTiempoIni()));
                    serializador.endTag("","tiempoIni");
                    serializador.startTag("","tiempoFin");
                    serializador.text(String.valueOf(frase.getTiempoFin()));
                    serializador.endTag("","tiempoFin");
                    serializador.startTag("","fraseOriginal");
                    serializador.text(frase.getFraseOriginal());
                    serializador.endTag("","fraseOriginal");
                    serializador.startTag("","fraseTraducida");
                    serializador.text(frase.getFraseTraducida());
                    serializador.endTag("","fraseTraducida");
                    serializador.endTag("","frase");
                }
                serializador.endTag("","letra");
                serializador.endTag("","cancion");
                serializador.endDocument();

                sincroListReproduccion();

            }catch (Exception e){
                Log.e(LOG_TAG, e.getMessage(), e);
            }

        }

    }


    /***
     * Lee los txt original y traducido generando el objeto letra.
     *
     */
    public void leerTXT(){
        if(validarLeerSD()){
            String txtOriginal = rutaCarpeta + this.nombreFichero + EXTENSION_TXTORIGINAL;
            String txtTraducido = rutaCarpeta + this.nombreFichero + EXTENSION_TXTTRADUCIDO;

            try{
                File original = new File(txtOriginal);
                File traducido = new File(txtTraducido);

                FileReader frOriginal = new FileReader(original);
                FileReader frTraducido = new FileReader(traducido);

                BufferedReader brOriginal = new BufferedReader(frOriginal);
                BufferedReader brTraducido = new BufferedReader(frTraducido);

                String fraseOriginal;
                String fraseTraducida;
                do{
                    Frase frase = new Frase();
                    fraseOriginal = brOriginal.readLine();
                    fraseTraducida = brTraducido.readLine();

                    if(fraseOriginal != null && fraseTraducida !=null){
                        frase.setFraseOriginal(fraseOriginal);
                        frase.setFraseTraducida(fraseTraducida);
                        letra.add(frase);
                    }

                }while(fraseOriginal!=null && fraseTraducida !=null);
                frOriginal.close();
                frTraducido.close();
            }
            catch(Exception e){
                Log.e(LOG_TAG, e.getMessage(),e);

            }

        }
    }

    /**
     * Permite borrar el fichero Xml asociado a la canción
     * @return true si borrado
     */
    public boolean borrarXML(){
        String rutaXML = rutaCarpeta+this.nombreFichero+EXTENSION_XML;
        boolean borrado = false;
        if(validarEscribirSD()){
            File fileXML = new File(rutaXML);
            borrado = fileXML.delete();
        }
        return borrado;
    }


    /**
     * Permite mostrar todos los atributos que componen el objeto canción
     */
    public void toStringCancion(){
        String mensaje= "TÍtulo: " + this.titulo +"\n" +
                "Autor: " + this.autor +"\n" +
                "Dificultad: " + this.dificultad.getTextoDificultad() +"\n"+
                "Genero: " + this.genero.getTextoGenero() +"\n"+
                "Etiquetado: " + this.etiquetado.toString() +"\n"+
                "Nombre Fichero: "+this.nombreFichero +"\n";
        for (Frase frase:this.letra){
            mensaje += "TiempoIni: " + String.valueOf(frase.getTiempoIni()) +"  "+
                    "TiempoFin: " + String.valueOf(frase.getTiempoFin()) +" "+
                    "FraseOriginal: " + frase.getFraseOriginal()+" "+
                    "FraseTraducida: " + frase.getFraseTraducida()+"\n";
        }
        Log.d(LOG_TAG, mensaje);

    }
}
