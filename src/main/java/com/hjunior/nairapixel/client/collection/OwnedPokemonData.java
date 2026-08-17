package com.hjunior.nairapixel.client.collection;

import java.util.UUID;

public final class OwnedPokemonData {
    public enum Ubicacion {
        EQUIPO,
        PC
    }

    private final UUID uuid;
    private final String pokemon;
    private final String forma;
    private final int nivel;
    private final boolean shiny;
    private final Ubicacion ubicacion;
    private final int caja;
    private final int slot;

    public OwnedPokemonData(
            UUID uuid,
            String pokemon,
            String forma,
            int nivel,
            boolean shiny,
            Ubicacion ubicacion,
            int caja,
            int slot
    ){
        this.uuid=uuid;
        this.pokemon=limpiar(pokemon);
        this.forma=limpiar(forma);
        this.nivel=nivel;
        this.shiny=shiny;
        this.ubicacion=ubicacion;
        this.caja=caja;
        this.slot=slot;
    }

    public UUID getUuid(){return uuid;}
    public String getPokemon(){return pokemon;}
    public String getForma(){return forma;}
    public int getNivel(){return nivel;}
    public boolean isShiny(){return shiny;}
    public Ubicacion getUbicacion(){return ubicacion;}
    public int getCaja(){return caja;}
    public int getSlot(){return slot;}

    public boolean estaEnEquipo(){
        return ubicacion==Ubicacion.EQUIPO;
    }

    public boolean estaEnPC(){
        return ubicacion==Ubicacion.PC;
    }

    public boolean tieneForma(){
        return !forma.isEmpty();
    }

    private static String limpiar(String texto){
        return texto==null?"":texto.trim();
    }
}