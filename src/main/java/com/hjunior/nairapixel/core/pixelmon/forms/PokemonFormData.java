package com.hjunior.nairapixel.core.pixelmon.forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PokemonFormData {
    private final String pokemon;
    private final String forma;
    private final String region;
    private final boolean formaBase;
    private final boolean temporal;
    private final List<String> tipos;
    private final List<String> habilidades;
    private final List<String> habilidadesOcultas;
    private final int ps;
    private final int ataque;
    private final int defensa;
    private final int ataqueEspecial;
    private final int defensaEspecial;
    private final int velocidad;

    public PokemonFormData(
            String pokemon,
            String forma,
            String region,
            boolean formaBase,
            boolean temporal,
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
        this.pokemon=limpiar(pokemon);
        this.forma=limpiar(forma);
        this.region=limpiar(region);
        this.formaBase=formaBase;
        this.temporal=temporal;
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

    public String getPokemon(){return pokemon;}
    public String getForma(){return forma;}
    public String getRegion(){return region;}
    public boolean isFormaBase(){return formaBase;}
    public boolean isTemporal(){return temporal;}
    public List<String> getTipos(){return tipos;}
    public List<String> getHabilidades(){return habilidades;}
    public List<String> getHabilidadesOcultas(){return habilidadesOcultas;}
    public int getPS(){return ps;}
    public int getAtaque(){return ataque;}
    public int getDefensa(){return defensa;}
    public int getAtaqueEspecial(){return ataqueEspecial;}
    public int getDefensaEspecial(){return defensaEspecial;}
    public int getVelocidad(){return velocidad;}

    public int getBST(){
        return ps+ataque+defensa+ataqueEspecial+defensaEspecial+velocidad;
    }

    private static List<String> copiar(List<String> valores){
        if(valores==null||valores.isEmpty())return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(valores));
    }

    private static String limpiar(String texto){
        return texto==null?"":texto.trim();
    }
}