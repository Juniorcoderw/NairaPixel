package com.hjunior.nairapixel.client.dex.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NairaDexPokemonSummary {
    private final int numeroDex;
    private final int generacion;

    private final String pokemon;
    private final String forma;
    private final String region;
    private final String categoria;

    private final boolean formaBase;
    private final boolean temporal;
    private final boolean obtenido;

    private final int cantidad;
    private final int cantidadShiny;

    private final List<String> tipos;
    private final List<String> habilidades;
    private final List<String> habilidadesOcultas;

    private final int ps;
    private final int ataque;
    private final int defensa;
    private final int ataqueEspecial;
    private final int defensaEspecial;
    private final int velocidad;

    public NairaDexPokemonSummary(
            int numeroDex,
            int generacion,
            String pokemon,
            String forma,
            String region,
            String categoria,
            boolean formaBase,
            boolean temporal,
            boolean obtenido,
            int cantidad,
            int cantidadShiny,
            List<String> tipos,
            List<String> habilidades,
            List<String> habilidadesOcultas,
            int ps,
            int ataque,
            int defensa,
            int ataqueEspecial,
            int defensaEspecial,
            int velocidad
    ){
        this.numeroDex=numeroDex;
        this.generacion=generacion;

        this.pokemon=limpiar(pokemon);
        this.forma=limpiar(forma);
        this.region=limpiar(region);
        this.categoria=limpiar(categoria);

        this.formaBase=formaBase;
        this.temporal=temporal;
        this.obtenido=obtenido;

        this.cantidad=Math.max(0,cantidad);
        this.cantidadShiny=Math.max(0,cantidadShiny);

        this.tipos=copiar(tipos);
        this.habilidades=copiar(habilidades);
        this.habilidadesOcultas=copiar(habilidadesOcultas);

        this.ps=ps;
        this.ataque=ataque;
        this.defensa=defensa;
        this.ataqueEspecial=ataqueEspecial;
        this.defensaEspecial=defensaEspecial;
        this.velocidad=velocidad;
    }

    public int getNumeroDex(){
        return numeroDex;
    }

    public int getGeneracion(){
        return generacion;
    }

    public String getPokemon(){
        return pokemon;
    }

    public String getForma(){
        return forma;
    }

    public String getRegion(){
        return region;
    }

    public String getCategoria(){
        return categoria;
    }

    public boolean isFormaBase(){
        return formaBase;
    }

    public boolean isTemporal(){
        return temporal;
    }

    public boolean isObtenido(){
        return obtenido;
    }

    public int getCantidad(){
        return cantidad;
    }

    public int getCantidadShiny(){
        return cantidadShiny;
    }

    public List<String> getTipos(){
        return tipos;
    }

    public List<String> getHabilidades(){
        return habilidades;
    }

    public List<String> getHabilidadesOcultas(){
        return habilidadesOcultas;
    }

    public int getPS(){
        return ps;
    }

    public int getAtaque(){
        return ataque;
    }

    public int getDefensa(){
        return defensa;
    }

    public int getAtaqueEspecial(){
        return ataqueEspecial;
    }

    public int getDefensaEspecial(){
        return defensaEspecial;
    }

    public int getVelocidad(){
        return velocidad;
    }

    public int getBST(){
        return ps+
                ataque+
                defensa+
                ataqueEspecial+
                defensaEspecial+
                velocidad;
    }

    public boolean tieneHA(){
        return !habilidadesOcultas.isEmpty();
    }

    public boolean tieneForma(){
        return !forma.isEmpty();
    }

    public boolean tieneRegion(){
        return !region.isEmpty();
    }

    private static List<String> copiar(
            List<String> valores
    ){
        if(valores==null||valores.isEmpty()){
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(
                new ArrayList<>(valores)
        );
    }

    private static String limpiar(String texto){
        return texto==null
                ?""
                :texto.trim();
    }
}