package com.hjunior.nairapixel.core.pixelmon.spawn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PokemonSpawnRule {
    private final String pokemon;
    private final String forma;
    private final String spec;
    private final List<String> horarios;
    private final List<String> biomas;
    private final List<String> climas;
    private final List<String> bloquesBase;
    private final Integer minY;
    private final Integer maxY;
    private final Integer faseLunar;
    private final String origen;

    public PokemonSpawnRule(
            String pokemon,
            String forma,
            String spec,
            List<String> horarios,
            List<String> biomas,
            List<String> climas,
            Integer minY,
            Integer maxY,
            Integer faseLunar,
            List<String> bloquesBase,
            String origen
    ){
        this.pokemon=limpiar(pokemon);
        this.forma=limpiar(forma);
        this.spec=limpiar(spec);
        this.horarios=copiar(horarios);
        this.biomas=copiar(biomas);
        this.climas=copiar(climas);
        this.minY=minY;
        this.maxY=maxY;
        this.faseLunar=faseLunar;
        this.bloquesBase=copiar(bloquesBase);
        this.origen=limpiar(origen);
    }

    public String getPokemon(){return pokemon;}
    public String getForma(){return forma;}
    public String getSpec(){return spec;}
    public List<String> getHorarios(){return horarios;}
    public List<String> getBiomas(){return biomas;}
    public List<String> getClimas(){return climas;}
    public List<String> getBloquesBase(){return bloquesBase;}
    public Integer getMinY(){return minY;}
    public Integer getMaxY(){return maxY;}
    public Integer getFaseLunar(){return faseLunar;}
    public String getOrigen(){return origen;}

    public boolean tieneHorarios(){return !horarios.isEmpty();}
    public boolean tieneBiomas(){return !biomas.isEmpty();}
    public boolean tieneClima(){return !climas.isEmpty();}
    public boolean tieneMinY(){return minY!=null;}
    public boolean tieneMaxY(){return maxY!=null;}
    public boolean tieneFaseLunar(){return faseLunar!=null;}
    public boolean tieneBloquesBase(){return !bloquesBase.isEmpty();}

    private static List<String> copiar(List<String> valores){
        if(valores==null||valores.isEmpty())return Collections.emptyList();

        List<String> copia=new ArrayList<>();

        for(String valor:valores){
            String limpio=limpiar(valor);
            if(!limpio.isEmpty()&&!copia.contains(limpio))copia.add(limpio);
        }

        return Collections.unmodifiableList(copia);
    }

    private static String limpiar(String texto){
        return texto==null?"":texto.trim();
    }
}