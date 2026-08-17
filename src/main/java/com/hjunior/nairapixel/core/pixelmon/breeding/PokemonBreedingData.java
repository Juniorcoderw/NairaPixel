package com.hjunior.nairapixel.core.pixelmon.breeding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PokemonBreedingData {
    private final String pokemon;
    private final String forma;
    private final List<String> gruposHuevo;
    private final int ciclosHuevo;
    private final int ratioCaptura;

    private final int evPS;
    private final int evAtaque;
    private final int evDefensa;
    private final int evAtaqueEspecial;
    private final int evDefensaEspecial;
    private final int evVelocidad;

    public PokemonBreedingData(
            String pokemon,
            String forma,
            List<String> gruposHuevo,
            int ciclosHuevo,
            int ratioCaptura,
            int evPS,
            int evAtaque,
            int evDefensa,
            int evAtaqueEspecial,
            int evDefensaEspecial,
            int evVelocidad
    ){
        this.pokemon=limpiar(pokemon);
        this.forma=limpiar(forma);
        this.gruposHuevo=copiar(gruposHuevo);
        this.ciclosHuevo=ciclosHuevo;
        this.ratioCaptura=ratioCaptura;
        this.evPS=evPS;
        this.evAtaque=evAtaque;
        this.evDefensa=evDefensa;
        this.evAtaqueEspecial=evAtaqueEspecial;
        this.evDefensaEspecial=evDefensaEspecial;
        this.evVelocidad=evVelocidad;
    }

    public String getPokemon(){return pokemon;}
    public String getForma(){return forma;}
    public List<String> getGruposHuevo(){return gruposHuevo;}
    public int getCiclosHuevo(){return ciclosHuevo;}
    public int getRatioCaptura(){return ratioCaptura;}

    public int getEvPS(){return evPS;}
    public int getEvAtaque(){return evAtaque;}
    public int getEvDefensa(){return evDefensa;}
    public int getEvAtaqueEspecial(){return evAtaqueEspecial;}
    public int getEvDefensaEspecial(){return evDefensaEspecial;}
    public int getEvVelocidad(){return evVelocidad;}

    public int getEvTotal(){
        return evPS+
                evAtaque+
                evDefensa+
                evAtaqueEspecial+
                evDefensaEspecial+
                evVelocidad;
    }

    private static List<String> copiar(List<String> valores){
        if(valores==null||valores.isEmpty()){
            return Collections.emptyList();
        }

        List<String> copia=new ArrayList<>();

        for(String valor:valores){
            String limpio=limpiar(valor);

            if(!limpio.isEmpty()&&!copia.contains(limpio)){
                copia.add(limpio);
            }
        }

        return Collections.unmodifiableList(copia);
    }

    private static String limpiar(String texto){
        return texto==null?"":texto.trim();
    }
}