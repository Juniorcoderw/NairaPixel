package com.hjunior.nairapixel.client.dex.state;

import java.util.Locale;

public final class NairaDexState {
    public enum Seccion {
        DEX,
        COLECCION,
        OBJETIVOS
    }

    public enum Pestana {
        GENERAL,
        SPAWN,
        MOVIMIENTOS,
        EVOLUCION,
        CRIANZA
    }

    public enum VistaCatalogo {
        LISTA,
        CUADRICULA
    }

    public enum FiltroColeccion {
        TODOS,
        OBTENIDOS,
        NO_OBTENIDOS
    }

    private static final NairaDexState INSTANCIA=
            new NairaDexState();

    private String pokemonSeleccionado="";
    private String formaSeleccionada="";
    private String busqueda="";

    private Seccion seccion=
            Seccion.DEX;

    private Pestana pestana=
            Pestana.GENERAL;

    private VistaCatalogo vistaCatalogo=
            VistaCatalogo.LISTA;

    private FiltroColeccion filtroColeccion=
            FiltroColeccion.TODOS;

    private NairaDexState(){}

    public static NairaDexState get(){
        return INSTANCIA;
    }

    public String getPokemonSeleccionado(){
        return pokemonSeleccionado;
    }

    public String getFormaSeleccionada(){
        return formaSeleccionada;
    }

    public String getBusqueda(){
        return busqueda;
    }

    public Seccion getSeccion(){
        return seccion;
    }

    public Pestana getPestana(){
        return pestana;
    }

    public VistaCatalogo getVistaCatalogo(){
        return vistaCatalogo;
    }

    public FiltroColeccion getFiltroColeccion(){
        return filtroColeccion;
    }

    public boolean tienePokemonSeleccionado(){
        return !pokemonSeleccionado.isEmpty();
    }

    public boolean tieneFormaSeleccionada(){
        return !formaSeleccionada.isEmpty();
    }

    public void seleccionarPokemon(String pokemon){
        String nuevo=
                limpiar(pokemon);

        if(mismoPokemon(
                pokemonSeleccionado,
                nuevo
        )){
            return;
        }

        pokemonSeleccionado=nuevo;
        formaSeleccionada="";
        pestana=Pestana.GENERAL;
    }

    public void seleccionarPokemon(
            String pokemon,
            String forma
    ){
        String nuevoPokemon=
                limpiar(pokemon);

        boolean cambioPokemon=
                !mismoPokemon(
                        pokemonSeleccionado,
                        nuevoPokemon
                );

        pokemonSeleccionado=nuevoPokemon;

        formaSeleccionada=
                normalizarForma(forma);

        if(cambioPokemon){
            pestana=Pestana.GENERAL;
        }
    }

    public void seleccionarForma(String forma){
        formaSeleccionada=
                normalizarForma(forma);
    }

    public void setBusqueda(String busqueda){
        this.busqueda=
                busqueda==null
                        ?""
                        :busqueda;
    }

    public void limpiarBusqueda(){
        busqueda="";
    }

    public void setSeccion(Seccion seccion){
        if(seccion==null)return;

        this.seccion=seccion;
    }

    public void setPestana(Pestana pestana){
        if(pestana==null)return;

        this.pestana=pestana;
    }

    public void setVistaCatalogo(
            VistaCatalogo vistaCatalogo
    ){
        if(vistaCatalogo==null)return;

        this.vistaCatalogo=vistaCatalogo;
    }

    public void setFiltroColeccion(
            FiltroColeccion filtroColeccion
    ){
        if(filtroColeccion==null)return;

        this.filtroColeccion=filtroColeccion;
    }

    public void alternarVistaCatalogo(){
        vistaCatalogo=
                vistaCatalogo==VistaCatalogo.LISTA
                        ?VistaCatalogo.CUADRICULA
                        :VistaCatalogo.LISTA;
    }

    public void limpiarSeleccion(){
        pokemonSeleccionado="";
        formaSeleccionada="";
        pestana=Pestana.GENERAL;
    }

    public void restablecer(){
        pokemonSeleccionado="";
        formaSeleccionada="";
        busqueda="";
        seccion=Seccion.DEX;
        pestana=Pestana.GENERAL;
        vistaCatalogo=VistaCatalogo.LISTA;
        filtroColeccion=FiltroColeccion.TODOS;
    }

    private static String normalizarForma(
            String forma
    ){
        String valor=
                limpiar(forma);

        if(valor.equalsIgnoreCase("base")){
            return "";
        }

        return valor;
    }

    private static boolean mismoPokemon(
            String a,
            String b
    ){
        return normalizar(a)
                .equals(normalizar(b));
    }

    private static String limpiar(String texto){
        return texto==null
                ?""
                :texto.trim();
    }

    private static String normalizar(String texto){
        if(texto==null)return "";

        String valor=
                texto.toLowerCase(Locale.ROOT);

        StringBuilder resultado=
                new StringBuilder();

        for(int i=0;i<valor.length();i++){
            char c=valor.charAt(i);

            if(Character.isLetterOrDigit(c)){
                resultado.append(c);
            }
        }

        return resultado.toString();
    }
}