package com.sacedonmg.cancionesingles;

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
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static com.sacedonmg.cancionesingles.UtilidadesCanciones.EXTENSION_AUDIO;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.EXTENSION_IMAGEN;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.EXTENSION_TXTORIGINAL;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.EXTENSION_TXTTRADUCIDO;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.EXTENSION_XML;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.rutaCarpeta;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.sincroListReproduccion;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.validarEscribirSD;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.validarLeerSD;

/**
 * Created by MGS on 09/07/2016.
 */
public class Cancion {
    private static final String LOG_TAG = "CI::Cancion";

    private String titulo;
    private String autor;
    private int genero;
    private int dificultad;
    private String nombreFichero;

    private String audio;           // URL en la que se encuentra el fichero de audio
    private String id;              // Identificador de la imagen
    private String imagen;          // URL en la que se encuentra la imagen de la portada
    private String txt_original;    // URL en la que se encuentra el fichero con la letra original
    private String txt_traducido;   // URL en la que se encuentra el fichero con la letra traducida
    private String xml;             // URL en la que se encuentra el fichero XML
    private String user;            // Usuario que creo/subió la canción

    private Boolean etiquetado;
    private List<Frase> letra;

    public Cancion ( String titulo, String autor, int genero, int dificultad, String nombreFichero, Boolean etiquetado, ArrayList<Frase> letra){
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
        this.genero = Genero.OTROS.ordinal();
        this.dificultad = Dificultad.FACIL.ordinal();
        this.nombreFichero = "";
        this.etiquetado = false;
        this.letra = new ArrayList<Frase>();
    }

    /** GETTER AND SETTER METHODS **/
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
        return Genero.getByKey(genero);
    }

    public void setGenero(int genero) {
        this.genero = genero;
    }

    public Dificultad getDificultad() {
        return Dificultad.getByKey(dificultad);
    }

    public void setDificultad(int dificultad) {
        this.dificultad = dificultad;
    }

    public String getNombreFichero() {
        return nombreFichero;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    /* Cuando se carga desde local */
    public void setNombreFichero(String nombreFichero) {
        this.nombreFichero = nombreFichero;
        setImagen(rutaCarpeta + nombreFichero + EXTENSION_IMAGEN);
        setAudio(rutaCarpeta + nombreFichero + EXTENSION_AUDIO);
        setXml(rutaCarpeta + nombreFichero + EXTENSION_XML);
        setTxt_original(rutaCarpeta + nombreFichero + EXTENSION_TXTORIGINAL);
        setTxt_traducido(rutaCarpeta + nombreFichero + EXTENSION_TXTTRADUCIDO);
    }

    public List<Frase> getLetra() {
        return letra;
    }

    public Boolean getEtiquetado() {
        return etiquetado;
    }

    public void setEtiquetado(Boolean etiquetado) {
        this.etiquetado = etiquetado;
    }

    public String getAudio() {
        return audio;
    }

    public String getId() {
        return id;
    }

    public String getImagen() {
        return imagen;
    }

    public String getTxt_original() {
        return txt_original;
    }

    public String getTxt_traducido() {
        return txt_traducido;
    }

    public String getXml() {
        return xml;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public void setTxt_original(String txt_original) {
        this.txt_original = txt_original;
    }

    public void setTxt_traducido(String txt_traducido) {
        this.txt_traducido = txt_traducido;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public void setLetra(List<Frase> letra) {
        this.letra = letra;
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
        this.genero = manejadorXML.getCancionXML().getGenero().ordinal();
        this.dificultad = manejadorXML.getCancionXML().getDificultad().ordinal();
        this.etiquetado =  manejadorXML.getCancionXML().getEtiquetado();
        this.letra = manejadorXML.getCancionXML().getLetra();
        this.user = "local";
    }

    /**
     * Leer un XML de una URL para generar un objeto canción
     */
    public void downloadXML() {
        try{
            URL url = new URL(getXml());
            URLConnection conn = url.openConnection();
            conn.connect();

            InputStream is = conn.getInputStream();
            InputSource inputSource = new InputSource(is);
            SAXParserFactory fabrica = SAXParserFactory.newInstance();
            SAXParser parser = fabrica.newSAXParser();
            XMLReader lector = parser.getXMLReader();
            ManejadorXML manejadorXML = new ManejadorXML();
            lector.setContentHandler(manejadorXML);
            lector.parse(inputSource);

            setEtiquetado(manejadorXML.getCancionXML().getEtiquetado());
            setLetra(manejadorXML.getCancionXML().getLetra());
        } catch (IOException e){
            Log.e(LOG_TAG, e.toString());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
            e.printStackTrace();
        }
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
                serializador.startTag("","user");
                serializador.text(this.user);
                serializador.endTag("","user");
                serializador.startTag("","autor");
                serializador.text(this.autor);
                serializador.endTag("","autor");
                serializador.startTag("","genero");
                serializador.text(String.valueOf(this.genero));
                serializador.endTag("","genero");
                serializador.startTag("","dificultad");
                serializador.text(String.valueOf(this.dificultad));
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

    @Override
    public String toString(){
        String mensaje =
                "Cancion: " + super.toString() + "\n" +
                "Título: " + this.titulo + "\n" +
                "Autor: " + this.autor + "\n" +
                "Dificultad: " + Dificultad.getByKey(this.dificultad).getTextoDificultad() + "\n" +
                "Genero: " + Genero.getByKey(this.genero).getTextoGenero() + "\n" +
                "Etiquetado: " + this.etiquetado.toString() + "\n"+
                "Nombre Fichero: " + this.nombreFichero + "\n" +
                "URL imagen: " + this.imagen + "\n" +
                "URL audio: " + this.audio + "\n" +
                "URL xml: " + this.xml + "\n" +
                "URL texto original: " + this.txt_original + "\n" +
                "URL texto traducido: " + this.txt_traducido + "\n" +
                "Usuario: " + this.user + "\n";

        return mensaje;
    }

    /**
     * Permite mostrar todos los atributos que componen el objeto canción
     */
    public void toStringCancion(){
        String mensaje = toString();
        /*for (Frase frase:this.letra){
            mensaje += "TiempoIni: " + String.valueOf(frase.getTiempoIni()) +"  "+
                    "TiempoFin: " + String.valueOf(frase.getTiempoFin()) +" "+
                    "FraseOriginal: " + frase.getFraseOriginal()+" "+
                    "FraseTraducida: " + frase.getFraseTraducida()+"\n";
        }*/

        Log.d(LOG_TAG, mensaje);
    }

    @Override
    public boolean equals(Object object) {
        Cancion cancion = (Cancion) object;
        boolean equalsTo = false;
        equalsTo = equalsTo || getTitulo().compareTo(cancion.getTitulo()) == 0;
        equalsTo = equalsTo || getAutor().compareTo(cancion.getAutor()) == 0;
        equalsTo = equalsTo || getNombreFichero().compareTo(cancion.getNombreFichero()) == 0;
        equalsTo = equalsTo || getXml().compareTo(cancion.getXml()) == 0;
        return equalsTo;
    }
}
