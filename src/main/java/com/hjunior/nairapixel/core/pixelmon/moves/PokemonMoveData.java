package com.hjunior.nairapixel.core.pixelmon.moves;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PokemonMoveData {
    private final String nombre;
    private final String tipo;
    private final String categoria;
    private final int potencia;
    private final int precision;
    private final int pp;
    private final List<MoveLearnSource> fuentes;

    public PokemonMoveData(
            String nombre,
            String tipo,
            String categoria,
            int potencia,
            int precision,
            int pp,
            List<MoveLearnSource> fuentes
    ){
        this.nombre=limpiar(nombre);
        this.tipo=limpiar(tipo);
        this.categoria=limpiar(categoria);
        this.potencia=potencia;
        this.precision=precision;
        this.pp=pp;
        this.fuentes=fuentes==null
                ?Collections.emptyList()
                :Collections.unmodifiableList(new ArrayList<>(fuentes));
    }

    public String getNombre(){return nombre;}
    public String getTipo(){return tipo;}
    public String getCategoria(){return categoria;}
    public int getPotencia(){return potencia;}
    public int getPrecision(){return precision;}
    public int getPP(){return pp;}
    public List<MoveLearnSource> getFuentes(){return fuentes;}

    private static String limpiar(String texto){
        return texto==null?"":texto.trim();
    }
}